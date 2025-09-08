package com.beyond.ordersystem.ordering.controller;

import com.beyond.ordersystem.common.dto.CommonDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.service.OrderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderingController {

    private final OrderingService orderingService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrdering(@RequestBody List<OrderCreateDto> dtoList, @RequestHeader("X-User-Email")String email) {
        Long id = orderingService.createFeignKafka(dtoList, email);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id+"번 주문")
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("주문 완료")
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> orderList() {
        List<OrderListResDto> orderListResDtoList = orderingService.findAll();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(orderListResDtoList)
                        .status_code(HttpStatus.OK.value())
                        .status_message("주문목록조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders(@RequestHeader("X-User-Email")String email) {
        List<OrderListResDto> orderListResDtoList = orderingService.myOrders(email);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(orderListResDtoList)
                        .status_code(HttpStatus.OK.value())
                        .status_message("주문목록조회")
                        .build(),
                HttpStatus.OK
        );
    }

}
