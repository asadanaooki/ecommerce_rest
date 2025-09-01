alter table `order`
  change column total_price_incl items_subtotal_incl int not null;
  
alter table `order`
  add column shipping_fee_incl int null after items_subtotal_incl;

update `order`
set shipping_fee_incl = case
  when items_subtotal_incl >= 3000 then 500
  else 0
end;

alter table `order`
  modify column shipping_fee_incl int not null;
  
alter table `order`
  add column grand_total_incl int
    generated always as (items_subtotal_incl + shipping_fee_incl) stored;