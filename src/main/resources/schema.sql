-- ======================


-- 1) テーブル削除
-- ======================
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS pre_registration;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS order_item;
SET FOREIGN_KEY_CHECKS=1;

-- user
CREATE TABLE `user` (
    user_id            CHAR(36)       NOT NULL,
    email              VARCHAR(255)   NOT NULL UNIQUE,
    password_hash      CHAR(60)       NOT NULL,

    -- ===== 氏名 =====
    last_name          VARCHAR(50)    NOT NULL,
    first_name         VARCHAR(50)    NOT NULL,
    last_name_kana     VARCHAR(50)    NOT NULL,
    first_name_kana    VARCHAR(50)    NOT NULL,

    -- ===== 住所 =====
    postal_code        CHAR(7)        NOT NULL,            -- 例: 1500041
    address_pref_city  VARCHAR(100)   NOT NULL,            -- 都道府県 + 市区町村
    address_area       VARCHAR(100)   NOT NULL,            -- それ以降（大字・町）
    address_block      VARCHAR(100)   NOT NULL,            -- 丁目・番地
    address_building   VARCHAR(100)   NULL,                -- 建物名 任意

    -- ===== 連絡先・個人情報 =====
    phone_number       VARCHAR(11)    NOT NULL,            -- 0始まり 10–11桁
    birthday           DATE           NOT NULL,
    gender             CHAR(1)        NOT NULL,            -- M / F 

    -- ===== 監査 =====
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id)
);

-- product
CREATE TABLE product (
    product_id          CHAR(36)       NOT NULL,
    product_name        VARCHAR(100)   NOT NULL,
    price               INT            NOT NULL,
    product_description VARCHAR(1000)  NOT NULL,
    stock               INT            NOT NULL,
    status              CHAR(1)        NOT NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id)
);

-- favorite
CREATE TABLE favorite (
    user_id             CHAR(36)       NOT NULL,
    product_id          CHAR(36)   NOT NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, product_id),
    CONSTRAINT fk_favorite_user
      FOREIGN KEY(user_id)  REFERENCES user(user_id),
    CONSTRAINT fk_favorite_product
      FOREIGN KEY(product_id)  REFERENCES product(product_id)
);

CREATE TABLE review (
    user_id             CHAR(36)       NOT NULL,
    product_id          CHAR(36)   NOT NULL,
    rating           INT           NOT NULL,
    comment          VARCHAR(500),
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, product_id),
    CONSTRAINT fk_review_user
      FOREIGN KEY(user_id)  REFERENCES user(user_id),
    CONSTRAINT fk_review_product
      FOREIGN KEY(product_id)  REFERENCES product(product_id)
);

 -- TODO: ユーザー削除時にCascadeするか　
CREATE TABLE cart (
    cart_id             CHAR(36)       NOT NULL,
    user_id             CHAR(36)       UNIQUE,
    version             INT            NOT NULL DEFAULT 0,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (cart_id),
    CONSTRAINT fk_cart_user
      FOREIGN KEY (user_id)
      REFERENCES `user`(user_id)
);

CREATE TABLE cart_item (
    cart_id             CHAR(36)       NOT NULL,
    product_id          CHAR(36)       NOT NULL,
    qty                 INT             NOT NULL,
    price               INT             NOT NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (cart_id, product_id),
    CONSTRAINT fk_cart_item_cart
      FOREIGN KEY(cart_id)  REFERENCES cart(cart_id)
    ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product
      FOREIGN KEY(product_id)  REFERENCES product(product_id)
    
);

CREATE TABLE pre_registration (
    token             CHAR(22)       NOT NULL,
    email          VARCHAR(255)       NOT NULL,
    expires_at          DATETIME             NOT NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (token) 
);

CREATE TABLE `order` (
  order_id          CHAR(36)        NOT NULL,
  user_id           CHAR(36)        NOT NULL,
  name              VARCHAR(100)    NOT NULL,
  postal_code       CHAR(7)         NOT NULL,
  address           VARCHAR(400)    NOT NULL,
  total_qty         INT             NOT NULL,
  total_price       INT             NOT NULL,
  created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (order_id),
  CONSTRAINT fk_order_user
    FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);

CREATE TABLE order_item (
  order_id          CHAR(36)        NOT NULL,
  product_id        CHAR(36)        NOT NULL,
  product_name      VARCHAR(100)   NOT NULL,
  qty               INT             NOT NULL,
  price             INT             NOT NULL,
  subtotal          INT             NOT NULL,
  created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (order_id, product_id),
  CONSTRAINT fk_order_item_order
    FOREIGN KEY (order_id) REFERENCES `order` (order_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_order_item_product
  FOREIGN KEY (product_id) REFERENCES product (product_id)
);

-- ======================
-- トリガー
-- ======================
-- 追加時
CREATE TRIGGER trg_cart_item_ai
AFTER INSERT ON cart_item
FOR EACH ROW
  UPDATE cart
    SET version = version + 1
  WHERE cart_id = NEW.cart_id;

-- 更新時
CREATE TRIGGER trg_cart_item_au
AFTER UPDATE ON cart_item
FOR EACH ROW
  UPDATE cart
    SET version = version + 1
  WHERE cart_id = NEW.cart_id;

-- 削除時
CREATE TRIGGER trg_cart_item_ad
AFTER DELETE ON cart_item
FOR EACH ROW
  UPDATE cart
    SET version = version + 1
  WHERE cart_id = OLD.cart_id;

