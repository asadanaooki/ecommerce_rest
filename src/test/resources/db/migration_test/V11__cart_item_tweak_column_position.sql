alter table cart_item
  drop column subtotal_incl,
  drop column unit_price_incl;
  
alter table cart_item
  add column unit_price_incl int
    generated always as ((unit_price_excl * 110) DIV 100) STORED
    after unit_price_excl,
  add column subtotal_incl int
    generated always as ((qty * unit_price_incl) DIV 100) STORED
    after unit_price_incl;