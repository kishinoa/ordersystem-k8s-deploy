package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.service.SseAlarmService;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderDetailsResDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;

    public Long save(List<OrderCreateDto> dtoList) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        for(OrderCreateDto o : dtoList) {
            Product product = productRepository.findById(o.getProductId()).orElseThrow(() -> new EntityNotFoundException("제품이 존재하지 않습니다."));
            if(product.getStockQuantity() < o.getProductCount()) {
//                예외를 강제 발생시킴으로서, 모든 임시저장 사항들을 rollback 처리
                throw new IllegalArgumentException("재고 부족");
            }

//            1. 동시에 접근하는 상황에서 update값의 정합성이 깨지고 갱신이상이 발생
//            2. spring버전이나 mysql버전에 따라 jpa에서 강제에러(deadlock)를 유발시켜 대부분의 요청실패 발생
            product.decreaseQuantity(o.getProductCount());
            ordering.getOrderDetailList().add(o.toEntity(product, ordering));
        }
        orderingRepository.save(ordering);

//        주문 성공시 admin 유저에게 알림메시지 전송
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

    public List<OrderListResDto> myOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("멤버가 존재하지 않습니다."));
        List<Ordering> orderings = orderingRepository.findByMember(member);
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        for(Ordering o : orderings) {
            List<OrderDetailsResDto> orderDetailsResDtoList = o.getOrderDetailList().stream().map(a-> OrderDetailsResDto.fromEntity(a)).toList();
            orderListResDtoList.add(OrderListResDto.fromEntity(o, orderDetailsResDtoList));
        }
        return orderListResDtoList;
    }

    public Ordering cancel(Long id) {
//        Ordering DB에 상태값변경 CANCELED
        Ordering ordering = orderingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("주문이 없습니다"));
        ordering.updateState(OrderStatus.CANCELED);

        for(OrderDetail o : ordering.getOrderDetailList()) {
//            rdb에 재고 업데이트
            o.getProduct().increaseQuantity(o.getQuantity());
        }

        return ordering;
    }
}
