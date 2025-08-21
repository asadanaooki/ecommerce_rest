drop view if exists vw_product_core;

alter table product
  rename column price to price_excl;
  
alter table cart_item
  rename column price to unit_price_excl;
  
alter table `order`
  rename column total_price to total_price_incl;
  
alter table order_item
  rename column price to unit_price_incl,
  rename column subtotal to subtotal_incl;
  
  
alter table cart_item
  add column unit_price_incl int
    generated always as ((unit_price_incl * 110) div 100)
    stored,
  add column subtotal_incl int
    generated always as (qty * unit_price_incl)
    virtual;
  
  
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
  (COALESCE(p.stock, 0) - COALESCE(p.reserved, 0)) AS available
FROM product p;