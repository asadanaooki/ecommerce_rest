alter table cart drop column version;

drop trigger if exists trg_cart_item_ai;
drop trigger if exists trg_cart_item_au;
drop trigger if exists trg_cart_item_ad;