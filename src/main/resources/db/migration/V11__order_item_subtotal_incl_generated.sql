alter table cart_item
  modify column unit_price_incl int
    generated always as ((unit_price_excl * 110) div 100) stored
    after unit_price_excl,
  modify column subtotal_incl int
      generated always as (qty * unit_price_incl) stored
      after unit_price_incl;