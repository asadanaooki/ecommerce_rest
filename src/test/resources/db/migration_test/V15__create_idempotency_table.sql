-- TODO: 
-- 別トランザクションに分割時は設計再検討(ステータスやボディ必要？)
create table idempotency (
  idempotency_key    char(36) not null,
  created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP,
  primary key(idempotency_key)
);