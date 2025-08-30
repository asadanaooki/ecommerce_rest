alter table review
  add column `status` varchar(20) null after review_text,
  add column reject_reason varchar(20) null after `status`,
  add column reject_note varchar(255) null after reject_reason;
  

update review
set `status` = 'APPROVED'
where `status` is null;


alter table review
  modify column `status` varchar(20) not null default 'PENDING';