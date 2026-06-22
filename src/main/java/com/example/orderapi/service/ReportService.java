package com.example.orderapi.service;

import com.example.orderapi.dto.ReportResponse;
import com.example.orderapi.entity.Order;
import com.example.orderapi.entity.OrderItem;
import com.example.orderapi.repository.OrderItemRepository;
import com.example.orderapi.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ReportService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * 顧客別の売上集計を返す。
     */
    public List<ReportResponse> getSalesByCustomer(LocalDateTime from, LocalDateTime to) {
        List<Order> all = orderRepository.findAll();
        Map<Long, BigDecimal> totalsByCustomer = new HashMap<>();
        Map<Long, String> namesByCustomer = new HashMap<>();

        for (Order o : all) {
            if (o.getOrderDate().isBefore(from) || o.getOrderDate().isAfter(to)) {
                continue;
            }
            if ("CANCELLED".equals(o.getStatus())) {
                continue;
            }

            Long cid = o.getCustomer().getId();
            String cname = o.getCustomer().getName();

            BigDecimal sum = BigDecimal.ZERO;
            for (OrderItem it : o.getItems()) {
                BigDecimal sub = it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity()));
                sum = sum.add(sub);
            }
            BigDecimal tax = sum.multiply(new BigDecimal("0.10"));
            BigDecimal grand = sum.add(tax);

            totalsByCustomer.merge(cid, grand, BigDecimal::add);
            namesByCustomer.put(cid, cname);
        }

        List<ReportResponse> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : totalsByCustomer.entrySet()) {
            ReportResponse r = new ReportResponse();
            r.setCustomerId(e.getKey());
            r.setCustomerName(namesByCustomer.get(e.getKey()));
            r.setTotalAmount(e.getValue());
            result.add(r);
        }
        result.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        return result;
    }

    /**
     * 商品名ごとの売上合計を返す。
     */
    public Map<String, BigDecimal> getSalesByProduct() {
        Map<String, BigDecimal> map = new HashMap<>();
        List<OrderItem> all = orderItemRepository.findAll();
        for (OrderItem it : all) {
            String name = it.getProduct().getName();
            BigDecimal sub = it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity()));
            map.merge(name, sub, BigDecimal::add);
        }
        return map;
    }
}
