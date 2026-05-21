# 仕込んだNG点リスト

本リポジトリには、学習・検証目的で意図的に仕込んだNG点が31件あります。  
観点別の内訳は以下の通りです。

## 観点1: 可読性

| # | ファイル | 内容 |
|---|----------|------|
| 1 | `OrderController.java` | `byCustomer` の戻り値が `Object`。型が利用側にわからない |
| 2 | `OrderService.createOrder` | メソッドが約45行と長い。明細処理・税計算・送料計算が1メソッドに同居 |
| 3 | `Customer.java` | フィールド `phone_number` が snake_case（Java 命名規約違反） |

## 観点2: 一貫性

| # | ファイル | 内容 |
|---|----------|------|
| 1 | `Customer` vs `Product` | `Customer` は `@Table(name=...)` 指定、`Product` は未指定 |
| 2 | `OrderService` 全体 | フィールドインジェクション（`@Autowired` フィールド注入）。コンストラクタ注入が Spring 推奨 |

## 観点3: 保守性

| # | ファイル | 内容 |
|---|----------|------|
| 1 | `OrderController.create` | エンティティ `Order` を直接返却（DTO 化されていない、JPA の遅延ロード問題と密結合） |
| 2 | `OrderService.createOrder` / `ReportService` | 税率 `0.10`、送料閾値 `5000`、送料 `500` がハードコード（複数箇所に散在し変更追従漏れリスク。定数化または `application.properties` 化が必要） |
| 3 | `OrderService.cancelOrder` | キャンセル時に在庫を戻していない（`createOrder` と非対称な挙動） |
| 4 | `Order.status` / `OrderService` / `ReportService` | ステータスを `String` で表現、"CREATED"/"CANCELLED" のリテラルが散在（enum 化推奨） |
| 5 | `OrderService.createOrder` | `RuntimeException` を直接スロー。独自例外クラス未定義 |
| 6 | `application.properties` | H2 コンソール (`spring.h2.console.enabled=true`) がデフォルト有効。本番反映時の運用リスク |

## 観点4: 安全性

| # | ファイル | 内容 |
|---|----------|------|
| 1 | `OrderService.createOrder` | `customerRepository.findById(...).get()` を直接呼び出し。存在しない ID で `NoSuchElementException` |
| 2 | `OrderService.createOrder` | `productRepository.findById(...).orElse(null)` の直後 `product.getStock()` で NPE 可能性 |
| 3 | `OrderService.getOrder` | `catch (Exception e) { return null; }` で例外握りつぶし |
| 4 | `OrderController.create` | `@Valid` 未指定。`OrderRequest` のフィールドにも `@NotNull` 等のバリデーション制約なし |
| 5 | `OrderService.createOrder` | ログに顧客の `email` を出力（個人情報リーク） |
| 6 | `Customer.toString` | `email` と `phone_number` を含む。ログ出力経由で PII リーク |
| 7 | `Customer.java` | `equals` / `hashCode` 未実装。Set/Map で同一性判定が壊れる |
| 8 | `ReportController.salesByCustomer` | `from` / `to` の null チェックなし → `ReportService` で NPE |
| 9 | `OrderService.createOrder` | 注文数量 (`quantity`) の下限・負数チェック未実装。負数で在庫を増やせる業務ロジック上の脆弱性 |

## 観点5: 再利用性

| # | ファイル | 内容 |
|---|----------|------|
| 1 | `OrderService` / `ReportService` | 「単価 × 数量」の小計計算、税計算が両サービスに重複（共通ユーティリティ化されていない） |
| 2 | `OrderService.createOrder` | 税率・送料の計算ロジックが Service にインライン。ロジックが切り出されていない |
| 3 | `OrderController.cancel` | "OK"/"NG" のレスポンス文字列がハードコード。共通レスポンスフォーマットなし |

## 観点6: 性能効率性

| # | ファイル | 内容 |
|---|----------|------|
| 1 | `OrderService` | `@Transactional` がクラス全体に付与。読み取り専用処理 (`getOrder`、`getOrdersByCustomer`) も書き込みTXで動作 |
| 2 | `OrderService.getOrdersByCustomer` | `findAll()` で全件取得 → Java で絞り込み。DB 側で WHERE 句を使うべき |
| 3 | `OrderService.getOrdersByCustomer` | ループ内で `o.getCustomer().getId()` 参照 → N+1（Customer が LAZY） |
| 4 | `ReportService.getSalesByCustomer` | `findAll()` で期間絞り込みせず全件取得 |
| 5 | `ReportService.getSalesByCustomer` | `o.getCustomer().getId()`、`o.getItems()`、`it.getUnitPrice()` などループ内アクセスで複数 N+1 |
| 6 | `ReportService.getSalesByCustomer` | ソートを Java で実施。DB 側で ORDER BY すべき |
| 7 | `ReportService.getSalesByProduct` | OrderItem 全件取得 + Product への N+1。集計用クエリ（GROUP BY）にすべき |
| 8 | `OrderService.createOrder` | 在庫更新を `productRepository.save(product)` で 1件ずつ実行（バッチ化されていない） |
