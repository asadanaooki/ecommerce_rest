alter table product
  add column `version` bigint not null default 0
  after updated_at;
  
drop trigger if exists product_version_bu;

delimiter //
create trigger product_version_bu
before update on product
for each row
begin
  if not (
    NEW.product_name        <=> OLD.product_name        AND
    NEW.price_excl          <=> OLD.price_excl          AND
    NEW.product_description <=> OLD.product_description AND
    NEW.stock               <=> OLD.stock               AND
    NEW.reserved            <=> OLD.reserved            AND
    NEW.status              <=> OLD.status
  ) then
    set NEW.version = OLD.version + 1;
  end if;
end//
delimiter ;