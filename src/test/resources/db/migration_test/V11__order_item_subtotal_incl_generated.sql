alter table order_item
  drop column subtotal_incl;
  
alter table order_item
  add column subtotal_incl int
    generated always as (unit_price_incl * qty) stored
    after unit_price_incl;