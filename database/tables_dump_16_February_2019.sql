-- -------------------------------------------------------------
-- TablePlus 1.5(190)
--
-- https://tableplus.com/
--
-- Database: postgres
-- Generation Time: 2019-02-16 19:53:33.5430
-- -------------------------------------------------------------


INSERT INTO "public"."fulfillment" ("id", "completed_at", "order_id") VALUES ('1', '1549112972181', '780197560420');

INSERT INTO "public"."fulfillment_vouchers" ("fulfillment_id", "vouchers_id") VALUES ('1', '1');

INSERT INTO "public"."vouchers" ("id", "amount", "code", "created_at", "currency", "expires_at", "published", "redeemed", "sku", "sold", "wallet_id") VALUES ('1', '1000', '7411cbb5-3120-4905-a9ce-73bae2b93724', '1549112726282', 'BTC', '0', 't', 'f', 'galicja-vouchers-sku', 't', '1'),
('2', '1000', 'cb86d75c-1761-4186-91d1-91f04dd1708b', '1549112726290', 'BTC', '0', 'f', 'f', 'galicja-vouchers-sku', 'f', '1'),
('3', '1000', '4512c3ef-8f75-4aca-ac9f-fa26460296e8', '1549112726291', 'BTC', '0', 'f', 'f', 'galicja-vouchers-sku', 'f', '1'),
('4', '1000', '0e1a2767-2106-46c1-b49d-18dad0896505', '1549112726299', 'BTC', '0', 'f', 'f', 'galicja-vouchers-sku', 'f', '1'),
('5', '1000', '00420b3a-cd9f-4b88-b25a-8b87f453c7a0', '1549112726300', 'BTC', '0', 'f', 'f', 'galicja-vouchers-sku', 'f', '1'),
('6', '1000', 'f4cf7769-1575-4d17-bb7b-4e0c78690ef4', '1549112726302', 'BTC', '0', 'f', 'f', 'galicja-vouchers-sku', 'f', '1'),
('7', '1000', '7240983c-ad83-4e99-99d8-3f1d542261f9', '1549112726303', 'BTC', '0', 'f', 'f', 'galicja-vouchers-sku', 'f', '1'),
('8', '1000', '919b3e06-1c7e-4e71-adf7-5558b116f6bb', '1549112726309', 'BTC', '0', 'f', 'f', 'galicja-vouchers-sku', 'f', '1'),
('9', '1000', 'b729036b-539e-4a53-9859-fde51af0f161', '1549112726311', 'BTC', '0', 'f', 'f', 'galicja-vouchers-sku', 'f', '1'),
('10', '1000', '7144ee4b-2696-4409-9201-a966f75fe6de', '1549112726312', 'BTC', '0', 'f', 'f', 'galicja-vouchers-sku', 'f', '1');

INSERT INTO "public"."wallets" ("id", "address", "created_at", "currency", "mnemonic") VALUES ('1', 'mg1EP1oW4okjMFW4WDc8Npo7tTSacU6c7P', '1549109203854', 'BTC', 'anchor party dawn silver van unusual ridge video grain float engine ensure');


