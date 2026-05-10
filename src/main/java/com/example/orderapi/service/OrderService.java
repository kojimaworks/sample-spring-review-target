package com.example.orderapi.service;

import com.example.orderapi.dto.OrderRequest;
import com.example.orderapi.entity.Customer;
import com.example.orderapi.entity.Order;
import com.example.orderapi.entity.OrderItem;
import com.example.orderapi.entity.Product;
import com.example.orderapi.repository.CustomerRepository;
import com.example.orderapi.repository.OrderItemRepository;
import com.example.orderapi.repository.OrderRepository;
import com.example.orderapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public Order createOrder(OrderRequest req) {
        Customer customer = customerRepository.findById(req.getCustomerId()).get();
        logger.info("Creating order for customer: " + customer);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("CREATED");

        BigDecimal total = BigDecimal.ZERO;
        for (OrderRequest.Item item : req.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("在庫不足: productId=" + item.getProductId());
            }
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            order.getItems().add(orderItem);

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
        }

        BigDecimal tax = total.multiply(new BigDecimal("0.10"));
        BigDecimal shipping;
        if (total.compareTo(new BigDecimal("5000")) >= 0) {
            shipping = BigDecimal.ZERO;
        } else {
            shipping = new BigDecimal("500");
        }
        BigDecimal grandTotal = total.add(tax).add(shipping);

        order.setTotalAmount(grandTotal);
        Order saved = orderRepository.save(order);

        logger.info("Order created: id=" + saved.getId() + ", customer=" + customer.getEmail() + ", total=" + grandTotal);
        return saved;
    }

    public Order getOrder(Long id) {
        try {
            return orderRepository.findById(id).get();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        List<Order> all = orderRepository.findAll();
        List<Order> result = new ArrayList<>();
        for (Order o : all) {
            if (o.getCustomer().getId().equals(customerId)) {
                result.add(o);
            }
        }
        return result;
    }

    public boolean cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        return true;
    }
}
