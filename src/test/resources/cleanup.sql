-- 外部キー無効化
SET FOREIGN_KEY_CHECKS = 0;

-- データ削除
DELETE FROM cart_item;
DELETE FROM favorite;
DELETE FROM review;
DELETE FROM order_item;
DELETE FROM `order`;
DELETE FROM pre_registration;
DELETE FROM password_reset_token;
DELETE FROM cart;
DELETE FROM product;
DELETE FROM `user`;

-- AUTO_INCREMENT リセット（自動採番があるテーブルのみ）
ALTER TABLE product       AUTO_INCREMENT = 1;

-- 外部キー再有効化
SET FOREIGN_KEY_CHECKS = 1;
