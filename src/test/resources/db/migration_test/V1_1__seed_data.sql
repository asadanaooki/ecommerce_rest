INSERT INTO `user` (
    user_id,
    email,
    password_hash,
    last_name,
    first_name,
    last_name_kana,
    first_name_kana,
    postal_code,
    address_pref_city,
    address_area,
    address_block,
    address_building,
    phone_number,
    birthday,
    gender,
    nickname,
    role
) VALUES
-- 1件目 (sample)
(
    '550e8400-e29b-41d4-a716-446655440000',
    'sample@example.com',
    '$2a$10$KhWFAYeUAWf2qX6h5XJrcuWMipwdwB5lCmDbfFJcURSpZKsj7AKk.', -- 'sample1234'
    '山田',  '太郎',
    'ヤマダ', 'タロウ',
    '1500041',
    '東京都渋谷区',
    '神南一丁目',
    '1-19-11',
    'パークウェイビル3F 10号室',
    '0312345678',
    '1990-04-15',
    'M',
    'yamarou',
    'USER'
),
-- 2件目 (bob2)
(
    '111e8400-e29b-41d4-a716-446655440111',
    'bob2@example.com',
    '$2a$10$WOxF8bY/WEUJGfmvXsK.uuugkUaWCd8XNjdnpZmYxNEHeC4PlWTzu', -- 'bob2test'
    '佐藤',  '花子',
    'サトウ', 'ハナコ',
    '0600000',
    '北海道札幌市中央区',
    '北一条西',
    '2-3-10',
    NULL,
    '01187654321',
    '1992-08-23',
    'F',
    'sato',
    'USER'
),
-- 3件目 (ADMIN)
(
    '222e8400-e29b-41d4-a716-446655440222',
    'admin@example.com',
    '$2a$10$KhWFAYeUAWf2qX6h5XJrcuWMipwdwB5lCmDbfFJcURSpZKsj7AKk.', -- 'sample1234'
    '管理',  '太郎',
    'カンリ', 'タロウ',
    '1000001',
    '東京都千代田区',
    '丸の内一丁目',
    '1-1-1',
    '東京駅ビル',
    '0311112222',
    '1985-12-01',
    'M',
    'kanri',
    'ADMIN'
);


INSERT INTO product (
    product_id,
    product_name,
    price,
    product_description,
    stock,
    reserved,
    status,
    created_at,
    updated_at
) VALUES(
    '97113c2c-719a-490c-9979-144d92905c33',
    'Item4',
    2900,
    'Item4の商品説明です。',
    10,
    NULL,  -- reserved
    'UNPUBLISHED',
    '2017-04-12 10:15:00',
    '2017-04-12 10:15:00'
),
(
    '09d5a43a-d24c-41c7-af2b-9fb7b0c9e049',
    'Item5',
    2500,
    'Item5の商品説明です。',
    5,
    NULL,  -- reserved
    'PUBLISHED',
    '2019-08-25 14:40:30',
    '2019-08-25 14:40:30'
),
(
    '6e1a12d8-71ab-43e6-b2fc-6ab0e5e813fd',
    'Item6',
    250,
    'Item6の商品説明です。',
    20,
    NULL,  -- reserved
    'PUBLISHED',
    '2021-11-03 09:05:12',
    '2021-11-03 09:05:12'
),
(
    '4a2a9e1e-4503-4cfa-ae03-3c1a5a4f2d07',
    'Item7',
    1800,
    'Item7の商品説明です。',
    0,
    NULL,  -- reserved
    'PUBLISHED',
    '2022-06-30 18:23:45',
    '2022-06-30 18:23:45'
),
(
    'f9c9cfb2-0893-4f1c-b508-f9e909ba5274',
    'Item18',
    3200,
    'Item18の商品説明です。',
    15,
    5,     -- reserved に値を設定
    'PUBLISHED',
    '2023-09-10 11:11:11',
    '2023-09-10 11:11:11'
),
(
    '1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68',
    'Item19',
    750,
    'Item19の商品説明です。',
    20,
    NULL,  -- reserved
    'PUBLISHED',
    '2025-04-05 08:02:14',
    '2025-04-05 08:02:14'
);

insert into favorite (
    user_id,
    product_id
) values(
    '550e8400-e29b-41d4-a716-446655440000',
    '1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68'
);

INSERT INTO cart(cart_id, user_id)
VALUES ('bbbbeeee-cccc-dddd-aaaa-111122223333','550e8400-e29b-41d4-a716-446655440000');

INSERT INTO cart_item (
    cart_id,
    product_id,
    qty,
    price
) VALUES (
    'bbbbeeee-cccc-dddd-aaaa-111122223333',      -- cart_id
    '1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68',      -- Item19
    2,                                           -- 数量
    750                                          
),
(
    'bbbbeeee-cccc-dddd-aaaa-111122223333',   -- cart_id
    '6e1a12d8-71ab-43e6-b2fc-6ab0e5e813fd',   -- Item6 の product_id
    1,                                         -- 数量
    250                                        
),
(
    'bbbbeeee-cccc-dddd-aaaa-111122223333',  -- cart_id（既存）
    '09d5a43a-d24c-41c7-af2b-9fb7b0c9e049', -- Item5 の product_id
    1,                                       -- 数量
    2500                                     -- 税抜価格
);

INSERT INTO review (
    user_id,
    product_id,
    rating,
    title,
    review_text,
    created_at,
    updated_at
)  VALUES(
    '550e8400-e29b-41d4-a716-446655440000',     -- ユーザー ID
    '09d5a43a-d24c-41c7-af2b-9fb7b0c9e049',     -- Item5
    4,                                          -- ★4
    'good',
    '程よい甘さで家族にも好評でした！',
    '2025-06-05 09:20:15',
    '2025-06-05 09:20:15'
),
(
    '550e8400-e29b-41d4-a716-446655440000',     -- ユーザー ID
    '6e1a12d8-71ab-43e6-b2fc-6ab0e5e813fd',     -- Item6
    3,                                          -- ★3
    'normal',
    'サイズが思ったより小さいですが味は満足。',
    '2024-06-10 18:42:05',
    '2024-06-10 18:42:05'
);
-- 1) order テーブルに 2 件追加（total_qty と total_price を修正）
INSERT INTO `order` (
    order_id,
    user_id,
    name,
    postal_code,
    address,
    total_qty,
    total_price,
    shipping_status,
    payment_status,
    created_at,
    updated_at
) VALUES
-- 注文1：商品1種 × 2 個 → 税込価格 2,750 円 × 2 = 5,500 円
(
    '3fa85f64-5717-4562-b3fc-2c963f66afa6',
    '550e8400-e29b-41d4-a716-446655440000',
    '山田 太郎',
    '1500041',
    '東京都渋谷区神南一丁目1-19-11 パークウェイビル3F 10号室',
    2,      -- total_qty を 2 に
    5500,   -- total_price = 2,750 × 2
    'SHIPPED',
    'PAID',
    '2025-04-20 21:00:00',
    '2025-04-20 21:00:00'
),
-- 注文2：商品2種 × 各1 個 → 税込価格 275 円 + 825 円 = 1,100 円
(
    '4fa85f64-5717-4562-b3fc-2c963f66afa7',
    '550e8400-e29b-41d4-a716-446655440000',
    '山田 太郎',
    '1500041',
    '東京都渋谷区神南一丁目1-19-11 パークウェイビル3F 10号室',
    4,      -- total_qty は明細の合計と合わせて 2 のまま
    1650,   -- total_price = 825 + 825
    'NOT_SHIPPED',
    'UNPAID',
    '2025-07-18 21:05:00',
    '2025-07-18 21:05:00'
);

-- 2) order_item テーブルに明細を追加（price と subtotal を税込、最初の明細 qty=2 に）
INSERT INTO order_item (
    order_id,
    product_id,
    product_name,
    qty,
    price,
    subtotal,
    created_at,
    updated_at
) VALUES
-- 注文1 の明細（Item5 を 2 個、単価 2,750 → 小計 5,500）
(
    '3fa85f64-5717-4562-b3fc-2c963f66afa6',
    '09d5a43a-d24c-41c7-af2b-9fb7b0c9e049',
    'Item5',
    2,      -- qty を 2 に
    2750,   -- price = 2,500 × 1.1
    5500,   -- subtotal = 2,750 × 2
    '2025-07-18 21:00:00',
    '2025-07-18 21:00:00'
),
-- 注文2 の明細1（Item6 を 1 個、単価 275 → 小計 275）
(
    '4fa85f64-5717-4562-b3fc-2c963f66afa7',
    '6e1a12d8-71ab-43e6-b2fc-6ab0e5e813fd',
    'Item6',
    3,
    275,    -- 250 × 1.1
    825,    -- 275 × 3
    '2025-07-18 21:05:00',
    '2025-07-18 21:05:00'
),
-- 注文2 の明細2（Item19 を 1 個、単価 825 → 小計 825）
(
    '4fa85f64-5717-4562-b3fc-2c963f66afa7',
    '1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68',
    'Item19',
    1,
    825,    -- 750 × 1.1
    825,    -- 825 × 1
    '2025-07-18 21:05:00',
    '2025-07-18 21:05:00'
);

