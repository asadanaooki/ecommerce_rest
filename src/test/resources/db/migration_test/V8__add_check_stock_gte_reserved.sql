alter table product
  add constraint chk_stock_reserved
  check (coalesce(stock, 0) >= coalesce(reserved, 0));