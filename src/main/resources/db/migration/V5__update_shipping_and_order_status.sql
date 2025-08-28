update `order`
set shipping_status = 'UNSHIPPED'
where shipping_status = 'NOT_SHIPPED';

alter table `order`
  modify column shipping_status varchar(20) not null default 'UNSHIPPED';
  
set @db := database();
set @col_exists := (
  select count(*)
  from information_schema.columns
  where table_schema = @db
    and table_name = 'order'
    and column_name = 'order_status'
);
set @ddl := if(
  @col_exists = 0,
  'alter table `order` add column order_status varchar(20) not null default ''OPEN''
    after total_price_incl',
  'select 1'
);
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

  
update `order`
set order_status = case
  when shipping_status = 'DELIVERED'
    and payment_status = 'PAID' then 'COMPLETED'
  else 'OPEN'
end;