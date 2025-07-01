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
    nickname
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
    'yamarou'
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
    'sato'
);


INSERT INTO product (
    product_id,
    product_name,
    price,
    product_description,
    stock,
    status,
    created_at,
    updated_at
) VALUES(
    '97113c2c-719a-490c-9979-144d92905c33',
    'Item4',
    2900,
    'Item4の商品説明です。',
    10,
    '0',
    '2017-04-12 10:15:00',
    '2017-04-12 10:15:00'
),
(
    '09d5a43a-d24c-41c7-af2b-9fb7b0c9e049',
    'Item5',
    2500,
    'Item5の商品説明です。',
    5,
    '1',
    '2019-08-25 14:40:30',
    '2019-08-25 14:40:30'
),
(
    '6e1a12d8-71ab-43e6-b2fc-6ab0e5e813fd',
    'Item6',
    250,
    'Item6の商品説明です。',
    20,
    '1',
    '2021-11-03 09:05:12',
    '2021-11-03 09:05:12'
),
(
    '4a2a9e1e-4503-4cfa-ae03-3c1a5a4f2d07',
    'Item7',
    1800,
    'Item7の商品説明です。',
    0,
    '1',
    '2022-06-30 18:23:45',
    '2022-06-30 18:23:45'
),
(
    'f9c9cfb2-0893-4f1c-b508-f9e909ba5274',
    'Item18',
    3200,
    'Item18の商品説明です。',
    15,
    '1',
    '2023-09-10 11:11:11',
    '2023-09-10 11:11:11'
),
(
    '1e7b4cd6-79cf-4c6f-8a8f-be1f4eda7d68',
    'Item19',
    750,
    'Item19の商品説明です。',
    20,
    '1',
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
    review_text,
    created_at,
    updated_at
)  VALUES(
    '550e8400-e29b-41d4-a716-446655440000',     -- ユーザー ID
    '09d5a43a-d24c-41c7-af2b-9fb7b0c9e049',     -- Item5
    4,                                          -- ★4
    '程よい甘さで家族にも好評でした！',
    '2025-06-05 09:20:15',
    '2025-06-05 09:20:15'
),
(
    '550e8400-e29b-41d4-a716-446655440000',     -- ユーザー ID
    '6e1a12d8-71ab-43e6-b2fc-6ab0e5e813fd',     -- Item6
    3,                                          -- ★3
    'サイズが思ったより小さいですが味は満足。',
    '2024-06-10 18:42:05',
    '2024-06-10 18:42:05'
);