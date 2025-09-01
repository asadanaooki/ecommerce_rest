ALTER TABLE `order`
  MODIFY COLUMN grand_total_incl INT
    GENERATED ALWAYS AS (items_subtotal_incl + shipping_fee_incl) STORED
    AFTER shipping_fee_incl;
