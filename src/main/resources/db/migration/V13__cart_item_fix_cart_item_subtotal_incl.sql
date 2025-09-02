alter table cart_item
  drop column subtotal_incl;
  
alter table cart_item
  add column subtotal_incl int
    generated always as (qty * unit_price_incl) STORED
    after unit_price_incl;