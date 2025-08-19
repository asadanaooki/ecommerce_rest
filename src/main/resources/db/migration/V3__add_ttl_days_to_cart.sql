alter table cart
  add column ttl_days int;
  
update cart
set ttl_days = case 
  when user_id is null then 14
  else 60
end;

alter table cart
  modify column ttl_days int not null;
