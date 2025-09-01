create or replace view vw_product_core as
SELECT
  p.product_id,
  LPAD(p.sku, 4, '0')                           AS sku,
  p.product_name,
  p.price_excl,                                  -- 税抜
  (p.price_excl * 110) DIV 100                   AS price_incl, -- 税込（切り捨て）
  p.product_description,
  COALESCE(p.stock, 0)                           AS stock,
  COALESCE(p.reserved, 0)                        AS reserved,
  p.status,
  p.created_at,
  p.updated_at,
  p.version,
  (COALESCE(p.stock, 0) - COALESCE(p.reserved, 0)) AS available
FROM product p;