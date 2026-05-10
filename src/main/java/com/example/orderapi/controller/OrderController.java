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

/**
 * 注文管理用のRESTコントローラー。
 * 注文の作成、取得、キャンセルなどの操作を提供します。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * 注文サービスのインスタンス
     */
    @Autowired
    private OrderService orderService;

    /**
     * 新しい注文を作成します。
     *
     * @param req 注文リクエスト情報
     * @return 作成された注文オブジェクト
     */
    @PostMapping
    public Order create(@RequestBody OrderRequest req) {
        return orderService.createOrder(req);
    }

    /**
     * 指定されたIDで注文を取得します。
     *
     * @param id 注文ID
     * @return 注文オブジェクト。見つからない場合は404を返す
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        Order o = orderService.getOrder(id);
        if (o == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(o);
    }

    /**
     * 顧客IDで注文一覧を取得します。
     *
     * @param customerId 顧客ID
     * @return 顧客の注文一覧
     */
    @GetMapping("/customer/{customerId}")
    public Object byCustomer(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    /**
     * 指定されたIDの注文をキャンセルします。
     *
     * @param id 注文ID
     * @return キャンセル成功時は"OK"、失敗時は"NG"を返す
     */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        boolean ok = orderService.cancelOrder(id);
        return ok ? "OK" : "NG";
    }
}
