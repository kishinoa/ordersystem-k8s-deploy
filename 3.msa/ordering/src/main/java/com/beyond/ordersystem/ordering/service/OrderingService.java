package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.dto.CommonDto;
import com.beyond.ordersystem.common.service.SseAlarmService;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderDetailsResDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.ProductDto;
import com.beyond.ordersystem.ordering.feignclient.ProductFeignClient;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final SseAlarmService sseAlarmService;
    private final RestTemplate  restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Long save(List<OrderCreateDto> dtoList, String email) {
        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
        for(OrderCreateDto o : dtoList) {
//            상품조회
            String productDetailUrl = "http://product-service/product/detail/" + o.getProductId();
            HttpHeaders headers = new HttpHeaders();
//            HttpEntity : httpbody 와 httpheader를 세팅하기 위한 객체
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<CommonDto> responseEntity = restTemplate.exchange(productDetailUrl, HttpMethod.GET, httpEntity, CommonDto.class);
            CommonDto commonDto = responseEntity.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
//            readValue : String -> 클래스변환, convertValue : Object클래스 -> 클래스 변환
            ProductDto product = objectMapper.convertValue(commonDto.getResult(), ProductDto.class);

            if(product.getStockQuantity() < o.getProductCount()) {
                throw new IllegalArgumentException("재고 부족");
            }
//            주문발생
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .ordering(ordering)
                    .quantity(o.getProductCount())
                    .build();
            ordering.getOrderDetailList().add(orderDetail);

//            동기적 재고감소 요청
            String productUpdateStockUrl = "http://product-service/product/updatestock";
            HttpHeaders stockHeaders = new HttpHeaders();
            stockHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OrderCreateDto> updateStockEntity = new HttpEntity<>(o, stockHeaders);
            restTemplate.exchange(productUpdateStockUrl, HttpMethod.PUT, updateStockEntity, void.class);
        }
        orderingRepository.save(ordering);

        sseAlarmService.publishMessage("admin@naver.com", email, ordering.getId());

        return ordering.getId();
    }

//    fallback메서드는 원본 메서드의 매개변수와 정확히 일치해야함
    public void fallbackProductServiceCircuit(List<OrderCreateDto> dtoList, String email, Throwable t) {
        throw new RuntimeException("상품 서버 응답없음. 나중에 다시 시도해주세요");
    }

//    테스트 : 4~5번의 정상요청 -> 5번 중에 2번의 지연발생 -> circuit open -> 그 다음요청은 바로 fallback
    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "fallbackProductServiceCircuit")
    public Long createFeignKafka(List<OrderCreateDto> dtoList, String email) {
        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
        for(OrderCreateDto o : dtoList) {
//            feign 클라이언트를 사용한 동기적 상품조회
            CommonDto commonDto = productFeignClient.getProductById(o.getProductId());
            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto product = objectMapper.convertValue(commonDto.getResult(), ProductDto.class);

            if(product.getStockQuantity() < o.getProductCount()) {
                throw new IllegalArgumentException("재고 부족");
            }
//            주문발생
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .ordering(ordering)
                    .quantity(o.getProductCount())
                    .build();
            ordering.getOrderDetailList().add(orderDetail);

//            feign을 통한 동기적 재고감소 요청
//            productFeignClient.updateProductStockQuantity(o);

//            kafka를 활용한 비동기적 재고감소 요청
            kafkaTemplate.send("stock-update-topic", o);
        }
        orderingRepository.save(ordering);

        sseAlarmService.publishMessage("admin@naver.com", email, ordering.getId());

        return ordering.getId();
    }

    public List<OrderListResDto> findAll() {
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        for(Ordering o : orderings) {
            List<OrderDetailsResDto> orderDetailsResDtoList = o.getOrderDetailList().stream().map(a-> OrderDetailsResDto.fromEntity(a)).toList();
            orderListResDtoList.add(OrderListResDto.fromEntity(o, orderDetailsResDtoList));
        }
        return orderListResDtoList;
    }

    public List<OrderListResDto> myOrders(String email) {
        List<Ordering> orderings = orderingRepository.findByMemberEmail(email);
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        for(Ordering o : orderings) {
            List<OrderDetailsResDto> orderDetailsResDtoList = o.getOrderDetailList().stream().map(a-> OrderDetailsResDto.fromEntity(a)).toList();
            orderListResDtoList.add(OrderListResDto.fromEntity(o, orderDetailsResDtoList));
        }
        return orderListResDtoList;
    }
}
