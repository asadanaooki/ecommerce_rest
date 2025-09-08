alter table `order`
  add column cod_fee_incl int not null default 330
    after shipping_fee_incl,
  add column shipped_at timestamp null
    after shipping_status,
  modify column grand_total_incl int
    generated always as (
      items_subtotal_incl + shipping_fee_incl + cod_fee_incl
    ) stored
    after cod_fee_incl;
    
update `order`
set shipped_at = updated_at
where shipping_status = 'DELIVERED'
  and shipped_at is null;