INSERT IGNORE INTO cart(cart_id, user_id)
VALUES ('bbbbeeee-cccc-dddd-aaaa-111122223333','550e8400-e29b-41d4-a716-446655440000');

INSERT INTO cart_item (
    cart_id,
    product_id,
    qty,
    unit_price_excl
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