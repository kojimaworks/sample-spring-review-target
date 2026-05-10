package com.example.orderapi.controller;

import com.example.orderapi.dto.OrderRequest;
import com.example.orderapi.entity.Order;
import com.example.orderapi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Order create(@RequestBody OrderRequest req) {
        return orderService.createOrder(req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        Order o = orderService.getOrder(id);
        if (o == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(o);
    }

    @GetMapping("/customer/{customerId}")
    public Object byCustomer(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        boolean ok = orderService.cancelOrder(id);
        return ok ? "OK" : "NG";
    }
}
