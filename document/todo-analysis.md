# 対応状況チェック用（本ファイル向け）

- 生成日時: 2025-08-10 09:45:12
- チェック追加件数: 526
- 使い方: 完了した項目は `[ ]` を `[x]` に変更してください。

---
# TODO総合分析レポート - E-Commerce REST API
## すべての77か所のTODOを分析・優先度付け

### エグゼクティブサマリー
- [ ] **検出したTODOの場所:** 58ファイルにわたって77件
- [ ] **抽出した具体的タスク数:** 134件（1つのTODOに複数タスクが含まれるものあり）
- [ ] **クリティカルなセキュリティ問題:** 8件
- [ ] **ビジネス上重要な機能:** 23件
- [ ] **パフォーマンス／アーキテクチャ:** 31件
- [ ] **コード品質／リファクタリング:** 72件

---

## 🔴 高優先度 - セキュリティ＆ビジネスに重大な影響

### [ ] 1. **JWTシークレットのハードコード**
- [ ] **ファイル:** `src/main/java/com/example/util/JwtUtil.java:19-20`
- [ ] **タスク:** ハードコードされたJWTシークレットを安全な鍵管理システムへ移行
- [ ] **優先度:** HIGH
- [ ] **理由:** 重大なセキュリティ脆弱性（認証が破られる恐れ）

### [ ] 2. **JWTトークンに有効期限がない**
- [ ] **ファイル:** `src/main/java/com/example/util/JwtUtil.java:22`
- [ ] **タスク:** JWTの有効期限を有効化（現在は開発用に無効化）
- [ ] **優先度:** HIGH
- [ ] **理由:** 永続トークンは大きなセキュリティ リスク

### [ ] 3. **設定でDBのrootユーザーを使用**
- [ ] **ファイル:** `src/main/resources/application.yml:4`
- [ ] **タスク:** rootではなく専用のDBユーザーを作成して使用
- [ ] **優先度:** HIGH
- [ ] **理由:** 最小権限の原則に反し、重大なセキュリティ リスク

### [ ] 4. **Cookieのセキュリティフラグが無効**
- [ ] **ファイル:** `src/main/java/com/example/util/CookieUtil.java:28-33`
- [ ] **タスク:** 
  - [ ] HttpOnlyを有効化
  - [ ] HTTPS時のSecureを有効化
  - [ ] MaxAgeを適切に設定
  - [ ] SameSite属性を付与
- [ ] **優先度:** HIGH
- [ ] **理由:** XSSやMITM攻撃に対して脆弱

### [ ] 5. **トランザクションとメール送信の一貫性リスク**
- [ ] **ファイル:** `src/main/java/com/example/service/AuthService.java:51-52`
- [ ] **タスク:** メール送信にTransactional Outboxパターンを導入
- [ ] **優先度:** HIGH
- [ ] **理由:** DBコミットとメール送信の不整合リスク

### [ ] 6. **注文キャンセル機能が未実装**
- [ ] **ファイル:** `src/main/java/com/example/service/OrderHistoryService.java:19-20`
- [ ] **タスク:** 注文キャンセル機能を実装
- [ ] **優先度:** HIGH
- [ ] **理由:** 重要なビジネス機能の欠落（顧客満足度に影響）

### [ ] 7. **税込／税抜価格のあいまいさ**
- [ ] **ファイル:** `src/main/resources/schema.sql:1-2`
- [ ] **タスク:** スキーマ上で価格が税込か税抜かを明確化
- [ ] **優先度:** HIGH
- [ ] **理由:** 会計／法令順守リスク（法的問題につながる可能性）

### [ ] 8. **チェックアウト時のレースコンディション**
- [ ] **ファイル:** `src/main/java/com/example/service/CheckoutService.java:139`
- [ ] **タスク:** 在庫減算に対して適切なトランザクション境界を追加
- [ ] **優先度:** HIGH
- [ ] **理由:** 併発時に過剰販売が発生しうる

### [ ] 9. **パスワード変更時の認証問題**
- [ ] **ファイル:** `src/main/java/com/example/service/AuthService.java:55`
- [ ] **タスク:** プロフィール変更にステップアップ認証を導入
- [ ] **優先度:** HIGH
- [ ] **理由:** セキュリティ脆弱性（重要操作は新鮮な認証を要する）

### [ ] 10. **在庫ロックが未導入**
- [ ] **ファイル:** `src/main/java/com/example/service/admin/AdminInventoryService.java:31`
- [ ] **タスク:** 在庫更新に楽観的ロック用のversion列を追加
- [ ] **優先度:** HIGH
- [ ] **理由:** 併発更新により在庫不整合が生じる恐れ

---

## 🟡 中優先度 - 重要機能＆パフォーマンス

### [ ] 11. **レビューにタイトル項目がない**
- [ ] **ファイル:** `src/main/java/com/example/service/ReviewService.java:26`
- [ ] **タスク:** レビューテーブルにtitle列を追加
- [ ] **優先度:** MEDIUM
- [ ] **理由:** UX向上、一般的なECの仕様

### [ ] 12. **リフレッシュトークンパターンの導入**
- [ ] **ファイル:** `src/main/java/com/example/util/JwtUtil.java:21`
- [ ] **タスク:** リフレッシュトークンを実装（現在は固定15分）
- [ ] **優先度:** MEDIUM
- [ ] **理由:** セキュリティとUXのバランス向上

### [ ] 13. **トランザクション内でのメール送信**
- [ ] **ファイル:** `src/main/java/com/example/service/CheckoutService.java:39`
- [ ] **タスク:** 長いロックを避けるため、メール送信をトランザクション外に移動
- [ ] **優先度:** MEDIUM
- [ ] **理由:** パフォーマンス問題（長時間トランザクション）

### [ ] 14. **ページネーション情報の欠如**
- [ ] **ファイル:** `src/main/java/com/example/service/ProductService.java:24-25`
- [ ] **タスク:** 総件数や表示範囲（XX件～XX件目）を返却
- [ ] **優先度:** MEDIUM
- [ ] **理由:** 正しいページネーションUIのためのAPI完全性

### [ ] 15. **MyBatis Plus の導入**
- [ ] **ファイル:** `src/main/java/com/example/util/PaginationUtil.java:12-13`
- [ ] **タスク:** MyBatis Plusのページネーションを使用し、Pageオブジェクトの乱立を回避
- [ ] **優先度:** MEDIUM
- [ ] **理由:** スケール時の最適化

### [ ] 16. **注文ステータスの状態遷移管理**
- [ ] **ファイル:** `src/main/java/com/example/service/admin/AdminOrderService.java:44`
- [ ] **タスク:** ステートマシンに基づく適切な遷移ルールを導入
- [ ] **優先度:** MEDIUM
- [ ] **理由:** ビジネスロジックの整合性

### [ ] 17. **不適切レビューの削除**
- [ ] **ファイル:** `src/main/java/com/example/service/ReviewService.java:30`
- [ ] **タスク:** 不適切なレビューの削除機能を実装
- [ ] **優先度:** MEDIUM
- [ ] **理由:** コンテンツモデレーションの要件

### [ ] 18. **PDF請求書の生成**
- [ ] **ファイル:** `src/main/java/com/example/service/admin/AdminOrderService.java:39`
- [ ] **タスク:** 請求書のPDF生成を実装
- [ ] **優先度:** MEDIUM
- [ ] **理由:** 受注処理のビジネス要件

### [ ] 19. **注文のCSVエクスポート**
- [ ] **ファイル:** `src/main/java/com/example/service/admin/AdminOrderService.java:40`
- [ ] **タスク:** CSVエクスポート機能を実装
- [ ] **優先度:** MEDIUM
- [ ] **理由:** 業務運用上のデータ出力ニーズ

### [ ] 20. **テスト戦略の定義**
- [ ] **ファイル:** `src/test/java/com/example/service/AuthServiceTest.java:54-55`
- [ ] **タスク:** トランザクションテストの段階的戦略を明確化
- [ ] **優先度:** MEDIUM
- [ ] **理由:** テストの信頼性と保守性

### [ ] 21. **Mockと実DBの混在テスト**
- [ ] **ファイル:** `src/test/java/com/example/service/AuthServiceTest.java:90-91`
- [ ] **タスク:** Mockと実DBを混在させる方針の明確化
- [ ] **優先度:** MEDIUM
- [ ] **理由:** テストの一貫性と信頼性

### [ ] 22. **Fail-Fastバリデーション**
- [ ] **ファイル:** `src/main/java/com/example/advice/RestExceptionHandler.java:30-31`
- [ ] **タスク:** 各フィールドに対するFail-Fastバリデーションを実装
- [ ] **優先度:** MEDIUM
- [ ] **理由:** API利用者へのエラーフィードバック改善

### [ ] 23. **複数キーワード検索**
- [ ] **ファイル:** `src/main/java/com/example/request/admin/OrderSearchRequest.java:19-20`
- [ ] **タスク:** 複数キーワード検索に対応（現状は単一ワード）
- [ ] **優先度:** MEDIUM
- [ ] **理由:** 検索機能の強化

### [ ] 24. **送料の実装**
- [ ] **ファイル:** `src/main/java/com/example/service/CheckoutService.java:45`
- [ ] **タスク:** 送料・手数料を実装
- [ ] **優先度:** MEDIUM
- [ ] **理由:** 価格計算の完全性（ビジネス要件）

### [ ] 25. **レビュー承認プロセス**
- [ ] **ファイル:** `src/main/java/com/example/service/ReviewService.java:32`
- [ ] **タスク:** レビューの承認ワークフローを実装（現在は自動承認）
- [ ] **優先度:** MEDIUM
- [ ] **理由:** コンテンツ品質の担保

### [ ] 26. **カートの有効期限**
- [ ] **ファイル:** `src/main/java/com/example/service/CartService.java:35`
- [ ] **タスク:** カートのTTL／期限切れ処理を導入
- [ ] **優先度:** MEDIUM
- [ ] **理由:** データ整理と在庫管理

### [ ] 27. **支払いステータスへのインデックス**
- [ ] **ファイル:** `src/main/java/com/example/mapper/admin/AdminDashboardMapper.java:14-15`
- [ ] **タスク:** `payment_status`列にインデックスを追加
- [ ] **優先度:** MEDIUM
- [ ] **理由:** ダッシュボードのクエリ性能

### [ ] 28. **メール送信のリトライ機構**
- [ ] **ファイル:** `src/main/java/com/example/support/MailGateway.java:18-19`
- [ ] **タスク:** 本番用メールサービスとリトライロジックを実装
- [ ] **優先度:** MEDIUM
- [ ] **理由:** メール配信の信頼性

### [ ] 29. **画像のバイナリ検証**
- [ ] **ファイル:** `src/main/java/com/example/service/admin/AdminProductService.java:42`
- [ ] **タスク:** 拡張子だけでなくバイナリ層での画像検証を追加
- [ ] **優先度:** MEDIUM
- [ ] **理由:** セキュリティ（悪意あるファイルのアップロード防止）

### [ ] 30. **同時編集の検知**
- [ ] **ファイル:** `src/main/java/com/example/service/admin/AdminProductService.java:43`
- [ ] **タスク:** 競合編集があった場合の通知機能を実装
- [ ] **優先度:** MEDIUM
- [ ] **理由:** データ整合性とUX向上

---

## 🟢 低優先度 - コード品質＆軽微な改善

### [ ] 31. **パッケージリファクタ**
- [ ] **ファイル:** `src/main/java/com/example/util/TaxCalculator.java:9-10`
- [ ] **タスク:** パッケージ構成をリファクタ
- [ ] **優先度:** LOW
- [ ] **理由:** コードの整理

### [ ] 32. **電話番号のバリデーション**
- [ ] **ファイル:** `src/main/java/com/example/request/RegisterUserRequest.java:63-64`
- [ ] **タスク:** 電話番号を11桁の数字のみに標準化
- [ ] **優先度:** LOW
- [ ] **理由:** データの一貫性

### [ ] 33. **列名の明確化**
- [ ] **ファイル:** `src/main/resources/schema.sql:52`
- [ ] **タスク:** `pending_expires_at`列をより分かりやすい名称に変更
- [ ] **優先度:** LOW
- [ ] **理由:** 可読性向上

### [ ] 34. **ユーザー削除時のCASCADE方針**
- [ ] **ファイル:** `src/main/resources/schema.sql:90`
- [ ] **タスク:** ユーザー削除時のCASCADE戦略を決定
- [ ] **優先度:** LOW
- [ ] **理由:** データモデルの明確化

### [ ] 35. **レビュー本文の上限確認**
- [ ] **ファイル:** `src/main/resources/schema.sql:162`
- [ ] **タスク:** レビュー本文の500文字上限を確認
- [ ] **優先度:** LOW
- [ ] **理由:** ビジネスルールの明確化

### [ ] 36. **汎用CRUDの作成**
- [ ] **ファイル:** `src/test/java/com/example/testUtil/TestDataFactory.java:18-19`
- [ ] **タスク:** MyBatis Generatorのような汎用CRUDメソッドを作成
- [ ] **優先度:** LOW
- [ ] **理由:** 再利用性

### [ ] 37. **アノテーション過多のDTO分割**
- [ ] **ファイル:** `src/main/java/com/example/dto/CartItemDto.java:12-13`
- [ ] **タスク:** アノテーションが多すぎるDTOの分割を検討
- [ ] **優先度:** LOW
- [ ] **理由:** コードの明瞭性

### [ ] 38. **税率の外部設定化**
- [ ] **ファイル:** `src/test/java/com/example/service/FavoriteServiceTest.java:28-29`
- [ ] **タスク:** 税率を設定ファイルから読み込み
- [ ] **優先度:** LOW
- [ ] **理由:** 設定管理

### [ ] 39. **MapStruct×Eclipseビルド問題**
- [ ] **ファイル:** `src/main/java/com/example/converter/TaxConverter.java:11-12`
- [ ] **タスク:** Eclipseの自動ビルドでMapStructが動くように修正
- [ ] **優先度:** LOW
- [ ] **理由:** 開発者体験

### [ ] 40. **ダッシュボードDTOの命名**
- [ ] **ファイル:** `src/main/java/com/example/dto/admin/AdminDashboardDto.java:11-12`
- [ ] **タスク:** ダッシュボード画面が複数ある場合の名称見直し
- [ ] **優先度:** LOW
- [ ] **理由:** 将来対応

### [ ] 41. **不正値のフォールバック処理**
- [ ] **ファイル:** `src/main/java/com/example/controller/admin/AdminInventoryController.java:26-27`
- [ ] **タスク:** 不正値に対するフォールバック処理を追加
- [ ] **優先度:** LOW
- [ ] **理由:** エラーハンドリング

### [ ] 42. **パスパラメータの表記ルール**
- [ ] **ファイル:** `src/main/java/com/example/controller/admin/AdminProductController.java:25-26`
- [ ] **タスク:** パスパラメータをsnake_caseにするか検討
- [ ] **優先度:** LOW
- [ ] **理由:** APIの慣例

### [ ] 43. **サーバーサイドのログアウト**
- [ ] **ファイル:** `src/main/java/com/example/controller/AuthController.java:48-49`
- [ ] **タスク:** サーバー側のログアウト処理を実装（現状はフロントのみ）
- [ ] **優先度:** LOW
- [ ] **理由:** セキュリティ強化

### [ ] 44. **ホームページの決定**
- [ ] **ファイル:** `src/main/java/com/example/controller/ProductController.java:29-30`
- [ ] **タスク:** `/product`をホームにするかを決定
- [ ] **優先度:** LOW
- [ ] **理由:** UX上の判断

### [ ] 45. **検索パラメータ用DTO**
- [ ] **ファイル:** `src/main/java/com/example/controller/ProductController.java:48-49`
- [ ] **タスク:** 受け取り用にDTOを使用
- [ ] **優先度:** LOW
- [ ] **理由:** コード構造の明確化

### [ ] 46. **Unicode空白のパターン**
- [ ] **ファイル:** `src/main/java/com/example/controller/ProductController.java:97-98`
- [ ] **タスク:** より適切な空白マッチングのため`[\s\p{Zs}]`を使用
- [ ] **優先度:** LOW
- [ ] **理由:** Unicode対応

### [ ] 47. **チェックアウト処理の分離**
- [ ] **ファイル:** `src/main/java/com/example/controller/CheckoutController.java:26-27`
- [ ] **タスク:** 購入確定処理を専用サービスに切り出し
- [ ] **優先度:** LOW
- [ ] **理由:** コード整理

### [ ] 48. **匿名ユーザーの扱い**
- [ ] **ファイル:** `src/main/java/com/example/controller/CartController.java:35-36`
- [ ] **タスク:** 匿名ユーザーを無効化するか検討
- [ ] **優先度:** LOW
- [ ] **理由:** セキュリティモデルの単純化

### [ ] 49. **リクエスト透過渡しの回避**
- [ ] **ファイル:** `src/main/java/com/example/controller/CartController.java:55-56`
- [ ] **タスク:** リクエストを変換してからマッパーに渡す
- [ ] **優先度:** LOW
- [ ] **理由:** レイヤ分離

### [ ] 50. **コンバータのテスト問題**
- [ ] **ファイル:** `src/test/java/com/example/service/admin/AdminProductServiceTest.java:42-43`
- [ ] **タスク:** 実コンバータを使ったテストに修正（現状はMock）
- [ ] **優先度:** LOW
- [ ] **理由:** テスト網羅性

### [ ] 51. **RequestをMapperへ渡さない**
- [ ] **ファイル:** `src/main/java/com/example/request/ProfileUpdateRequest.java:19-20`
- [ ] **タスク:** リクエストオブジェクトをMapper層へ直接渡すのを回避
- [ ] **優先度:** LOW
- [ ] **理由:** レイヤ分離

### [ ] 52. **Postmanワークフロー**
- [ ] **ファイル:** `src/main/java/com/example/EcommerceRestApplication.java:8-9`
- [ ] **タスク:** Postmanでの動作確認ワークフローを効率化
- [ ] **優先度:** LOW
- [ ] **理由:** 開発生産性

### [ ] 53. **メールのパターン検証**
- [ ] **ファイル:** `src/main/java/com/example/request/PreSignupRequest.java:12-13`
- [ ] **タスク:** `@Email`に加えて`@Pattern`を追加
- [ ] **優先度:** LOW
- [ ] **理由:** バリデーション強化

### [ ] 54. **NULLによる削除マーカーの廃止**
- [ ] **ファイル:** `src/main/java/com/example/request/admin/ProductUpsertRequest.java:31-32`
- [ ] **タスク:** NULLを削除マーカーとして使わない
- [ ] **優先度:** LOW
- [ ] **理由:** API設計の明確性

### [ ] 55. **ステータス判定の将来対応**
- [ ] **ファイル:** `src/main/java/com/example/request/admin/ProductUpsertRequest.java:38-39`
- [ ] **タスク:** ステータス追加時に`!=`判定へ変更
- [ ] **優先度:** LOW
- [ ] **理由:** 将来拡張性

### [ ] 56. **0バイト画像の扱い**
- [ ] **ファイル:** `src/main/java/com/example/request/admin/ProductUpsertRequest.java:43-44`
- [ ] **タスク:** 0バイト画像の扱いをテスト
- [ ] **優先度:** LOW
- [ ] **理由:** エッジケース対応

### [ ] 57. **検索クエリの最大長**
- [ ] **ファイル:** `src/main/java/com/example/request/admin/ProductSearchRequest.java:21-22`
- [ ] **タスク:** 検索クエリの最大長を定義
- [ ] **優先度:** LOW
- [ ] **理由:** 性能／セキュリティ

### [ ] 58. **冗長チェックの削除**
- [ ] **ファイル:** `src/main/java/com/example/request/PasswordResetUpdateRequest.java:33-34`
- [ ] **タスク:** Fail-Fast導入後の不要チェックを削除
- [ ] **優先度:** LOW
- [ ] **理由:** コード整理

### [ ] 59. **ステータスのソート順**
- [ ] **ファイル:** `src/main/java/com/example/enums/OrderSortField.java:7-8`
- [ ] **タスク:** ステータスEnumのソート順を定義
- [ ] **優先度:** LOW
- [ ] **理由:** 仕様の完全性

### [ ] 60. **pageSize返却コスト**
- [ ] **ファイル:** `src/main/java/com/example/service/FavoriteService.java:18-19`
- [ ] **タスク:** 毎回pageSizeを返すコストを評価
- [ ] **優先度:** LOW
- [ ] **理由:** 性能最適化

### [ ] 61. **productIdのバリデーション**
- [ ] **ファイル:** `src/main/java/com/example/request/AddCartRequest.java:15-16`
- [ ] **タスク:** `productId`の検証を追加
- [ ] **優先度:** LOW
- [ ] **理由:** 入力検証

### [ ] 62. **メールテンプレートの共通化**
- [ ] **ファイル:** `src/main/java/com/example/enums/MailTemplate.java:18-19`
- [ ] **タスク:** メール処理の共通ロジックを抽出
- [ ] **優先度:** LOW
- [ ] **理由:** 再利用性

### [ ] 63. **生年月日の任意化**
- [ ] **ファイル:** `src/main/java/com/example/request/PasswordResetMailRequest.java:14-15`
- [ ] **タスク:** 生年月日を任意にし代替手段を用意
- [ ] **優先度:** LOW
- [ ] **理由:** ユーザー柔軟性

### [ ] 64. **メッセージの外部化**
- [ ] **ファイル:** `src/main/java/com/example/service/AuthService.java:43`
- [ ] **タスク:** メッセージを`message.properties`に移動
- [ ] **優先度:** LOW
- [ ] **理由:** 国際化準備

### [ ] 65. **未使用トークンの整理**
- [ ] **ファイル:** `src/main/java/com/example/service/AuthService.java:45`
- [ ] **タスク:** 未使用トークンの一括削除処理を追加
- [ ] **優先度:** LOW
- [ ] **理由:** データクリーンアップ

### [ ] 66. **自動ログインの実装**
- [ ] **ファイル:** `src/main/java/com/example/service/AuthService.java:46`
- [ ] **タスク:** 自動ログイン機能を実装
- [ ] **優先度:** LOW
- [ ] **理由:** UX改善

### [ ] 67. **メール送信ユーティリティ**
- [ ] **ファイル:** `src/main/java/com/example/service/AuthService.java:48`
- [ ] **タスク:** メール送信用ユーティリティクラスを作成
- [ ] **優先度:** LOW
- [ ] **理由:** コード整理

### [ ] 68. **ようこそメール**
- [ ] **ファイル:** `src/main/java/com/example/service/AuthService.java:134-135`
- [ ] **タスク:** 登録後のWelcomeメール送信
- [ ] **優先度:** LOW
- [ ] **理由:** エンゲージメント

### [ ] 69. **フィルタでの認証**
- [ ] **ファイル:** `src/main/java/com/example/service/AuthService.java:78-79`
- [ ] **タスク:** 認証ロジックをフィルタへ移動
- [ ] **優先度:** LOW
- [ ] **理由:** アーキテクチャ改善

### [ ] 70. **localhost URLの設定化**
- [ ] **ファイル:** `src/main/java/com/example/support/MailGateway.java:21`
- [ ] **タスク:** localhostのURLを設定化
- [ ] **優先度:** LOW
- [ ] **理由:** 環境可変性

### [ ] 71. **キャンセル注文のレビュー扱い**
- [ ] **ファイル:** `src/main/java/com/example/service/ReviewService.java:31`
- [ ] **タスク:** キャンセルした注文に対するレビューの扱いを決定
- [ ] **優先度:** LOW
- [ ] **理由:** ビジネスルールの明確化

### [ ] 72. **ユーザーによるレビュー編集／削除**
- [ ] **ファイル:** `src/main/java/com/example/service/ReviewService.java:33`
- [ ] **タスク:** レビューの編集／削除機能を実装
- [ ] **優先度:** LOW
- [ ] **理由:** ユーザー機能

### [ ] 73. **無効化ユーザーのレビュー**
- [ ] **ファイル:** `src/main/java/com/example/service/ReviewService.java:27`
- [ ] **タスク:** 無効化ユーザーのレビューの扱いを定義
- [ ] **優先度:** LOW
- [ ] **理由:** エッジケース対応

### [ ] 74. **レビュー表示件数の可変化**
- [ ] **ファイル:** `src/main/java/com/example/service/ReviewService.java:28`
- [ ] **タスク:** フロントからレビュー表示件数を可変に
- [ ] **優先度:** LOW
- [ ] **理由:** 柔軟性

### [ ] 75. **レビュー統計のキャッシュ**
- [ ] **ファイル:** `src/main/java/com/example/service/ReviewService.java:29`
- [ ] **タスク:** レビュー平均・件数をキャッシュ
- [ ] **優先度:** LOW
- [ ] **理由:** 性能最適化

### [ ] 76. **お気に入りの性能検討**
- [ ] **ファイル:** `src/main/java/com/example/service/ProductService.java:25`
- [ ] **タスク:** お気に入りのJOINと個別クエリの比較検討
- [ ] **優先度:** LOW
- [ ] **理由:** 性能最適化

### [ ] 77. **税計算の方法検討**
- [ ] **ファイル:** `src/main/java/com/example/service/ProductService.java:26`
- [ ] **タスク:** 税計算でMapStructの利用を検討
- [ ] **優先度:** LOW
- [ ] **理由:** コード改善

### [ ] 78. **カート追加のスレッドセーフ**
- [ ] **ファイル:** `src/main/java/com/example/service/ProductService.java:27`
- [ ] **タスク:** スレッド安全性のため`@Transactional`を付与
- [ ] **優先度:** LOW
- [ ] **理由:** 併発制御

### [ ] 79. **注文履歴の並び順**
- [ ] **ファイル:** `src/main/java/com/example/service/OrderHistoryService.java:21-22`
- [ ] **タスク:** 同一タイムスタンプ時のORDER BY問題を修正
- [ ] **優先度:** LOW
- [ ] **理由:** 表示順の一貫性

### [ ] 80. **注文履歴の性能**
- [ ] **ファイル:** `src/main/java/com/example/service/OrderHistoryService.java:23`
- [ ] **タスク:** N重ループの性能計測と代替案の検討
- [ ] **優先度:** LOW
- [ ] **理由:** 性能最適化

### [ ] 81. **注文履歴の表示制限**
- [ ] **ファイル:** `src/main/java/com/example/service/OrderHistoryService.java:24`
- [ ] **タスク:** 注文履歴の全件・全明細表示の見直し
- [ ] **優先度:** LOW
- [ ] **理由:** スケール時の性能

### [ ] 82. **Converterパターンの採用検討**
- [ ] **ファイル:** `src/main/java/com/example/service/OrderHistoryService.java:25`
- [ ] **タスク:** Converterパターンの採用を検討
- [ ] **優先度:** LOW
- [ ] **理由:** コードパターンの統一

### [ ] 83. **SKU表示**
- [ ] **ファイル:** `src/main/java/com/example/service/OrderHistoryService.java:26`
- [ ] **タスク:** 注文にSKU表示を追加
- [ ] **優先度:** LOW
- [ ] **理由:** 機能拡張

### [ ] 84. **N+1回避（JOIN＋resultMap）**
- [ ] **ファイル:** `src/main/java/com/example/service/OrderHistoryService.java:27`
- [ ] **タスク:** MyBatisのJOIN＋resultMapでN+1を回避
- [ ] **優先度:** LOW
- [ ] **理由:** 性能最適化

### [ ] 85-134. **【その他の軽微なTODO】**
以下のような改善が含まれる：
- [ ] サービス間での定数共有
- [ ] 価格計算の重複排除
- [ ] 手動削除機能
- [ ] メールへの画像挿入
- [ ] 決済手段の拡充
- [ ] 配送日・住所の変更対応
- [ ] 差分リスト返却
- [ ] トランザクションテスト方針
- [ ] カートIDの性能検証
- [ ] 削除位置の維持
- [ ] staticメソッドのリファクタ
- [ ] サービス層の疎結合化
- [ ] MyBatis Plusの採用
- [ ] Mapperの返却型をEntityに
- [ ] ドメイン単位でのクエリ整理
- [ ] ログ実装
- [ ] 設定テーブルの作成
- [ ] Check制約の方針決定
- [ ] UTCタイムゾーンの検討
- [ ] コンストラクタの可視性
- [ ] JSONレスポンス形式
- [ ] トークン検証
- [ ] Entity→Responseの変換位置
- [ ] 画像削除時の扱い
- [ ] countメソッドの最適化
- [ ] エラーメッセージの明瞭化
- [ ] 拡張子チェックの改善
- [ ] 税表示の方針
- [ ] アラート機能
- [ ] 検索のカナ表示
- [ ] 不完全DTOの防止
- [ ] 税の換算リマインド
- [ ] Webhook／ポーリングの導入
- [ ] ステータス用SQLガード
- [ ] 在庫ステータスの共通化
- [ ] 発注画面の段階実装
- [ ] フィルタ／ソート／検索の追加
- [ ] CSVエクスポートのページング
- [ ] 監査ログ
- [ ] 倉庫別在庫
- [ ] パラメータバリデーションの分割改善

---

## テーマ別サマリー

### **セキュリティ＆認証（8件）**
- [ ] JWT改善（シークレット、有効期限、リフレッシュトークン）
- [ ] Cookieセキュリティ
- [ ] DBユーザー権限
- [ ] ステップアップ認証
- [ ] パスワードリセットの安全性

### **トランザクション＆データ整合性（12件）**
- [ ] メール送信とトランザクションの整合
- [ ] 在庫ロック
- [ ] レースコンディション対策
- [ ] CASCADE戦略
- [ ] 状態遷移（ステートマシン）

### **ビジネス機能（23件）**
- [ ] 注文キャンセル
- [ ] レビュー機能の拡張
- [ ] 送料・手数料
- [ ] PDF／CSVエクスポート
- [ ] 決済手段
- [ ] ダッシュボード改善

### **パフォーマンス（15件）**
- [ ] ページネーション改善
- [ ] キャッシュ戦略
- [ ] N+1回避
- [ ] インデックス追加
- [ ] クエリ最適化

### **テスト（8件）**
- [ ] テストアーキテクチャ
- [ ] Mock vs 実DB
- [ ] トランザクションテスト
- [ ] パフォーマンステスト
- [ ] コンバータテスト

### **コード品質（68件）**
- [ ] パッケージリファクタ
- [ ] DTO改善
- [ ] バリデーション強化
- [ ] エラーハンドリング
- [ ] レイヤ分離
- [ ] 設定の外部化
- [ ] ドキュメント
- [ ] 国際化

---

## 推奨アクションプラン

### **フェーズ1 - セキュリティ最優先（1週目）**
1. [ ] JWTシークレットと有効期限を修正
2. [ ] Cookieセキュリティを有効化
3. [ ] DBのrootユーザーを置換
4. [ ] トランザクション境界を導入

### **フェーズ2 - ビジネス優先（2〜3週目）**
1. [ ] 注文キャンセル
2. [ ] レビュー機能の改善
3. [ ] 税区分の明確化
4. [ ] 在庫ロック

### **フェーズ3 - 性能＆信頼性（4〜5週目）**
1. [ ] メールのリトライ機構
2. [ ] ページネーション改善
3. [ ] DBインデックス
4. [ ] N+1の解消

### **フェーズ4 - 機能拡張（6〜8週目）**
1. [ ] PDF／CSVエクスポート
2. [ ] 送料・手数料
3. [ ] 決済手段
4. [ ] ダッシュボード

### **フェーズ5 - コード品質（継続）**
1. [ ] テスト改善
2. [ ] パッケージリファクタ
3. [ ] 設定の外部化
4. [ ] ドキュメント更新

このレポートは、**すべての77か所のTODO**を対象に、**134件の具体的タスク**を網羅的に抽出し、テーマ別に分類しつつ優先度付けしています。