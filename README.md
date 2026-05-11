# order-api (Sample for AI Code Review Benchmarking)

Java/Spring Boot で書かれた、注文・売上集計のシンプルな REST API です。

## 目的

このプロジェクトは、AI コードレビューツール（GitHub Copilot Code Review / CodeRabbit / Qodo Merge など）の指摘傾向を比較するための**ベンチマーク用ターゲット**として用意しました。  
意図的に複数の観点（可読性 / 一貫性 / 保守性 / 安全性 / 再利用性 / 性能効率性）にまたがる課題を含んでいます。

## 構成

- Spring Boot 3.2.x
- Java 17
- Spring Data JPA + H2 (in-memory)
- Maven

## ドメイン

- Customer（顧客）
- Product（商品）
- Order（注文）
- OrderItem（注文明細）

## 主なエンドポイント

| メソッド | パス | 説明 |
|----------|------|------|
| POST | `/api/orders` | 注文登録 |
| GET | `/api/orders/{id}` | 注文取得 |
| GET | `/api/orders/customer/{customerId}` | 顧客別注文一覧 |
| POST | `/api/orders/{id}/cancel` | 注文キャンセル |
| GET | `/api/reports/sales-by-customer?from=...&to=...` | 顧客別売上集計 |
| GET | `/api/reports/sales-by-product` | 商品別売上集計 |

## 起動

```bash
mvn spring-boot:run
```

H2 コンソール: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:orderdb`
- User: `sa`
- Password: (empty)

## サンプルリクエスト

注文登録:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 2, "quantity": 2}
    ]
  }'
```

顧客別売上集計:
```bash
curl "http://localhost:8080/api/reports/sales-by-customer?from=2026-01-01T00:00:00&to=2026-12-31T23:59:59"
```

## 注意

このコードは**意図的に多くの問題を含んだサンプル**です。実プロダクションでの参考にしないでください。
