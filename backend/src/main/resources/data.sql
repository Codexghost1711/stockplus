-- StockPulse · Seed Data
-- Adapt table/column names to your schema

INSERT INTO products (id, sku, name, category, current_price, stock_level, reorder_threshold, demand_velocity, lifecycle_status) VALUES
  (1, 'SKU-ELEC-001', 'Wireless Earbuds Pro',     'ELECTRONICS', 79.99,  45,  20, 3,  'ACTIVE'),
  (2, 'SKU-ELEC-002', 'USB-C Hub 7-Port',           'ELECTRONICS', 34.99,  120, 30, 1,  'ACTIVE'),
  (3, 'SKU-APP-001',  'Organic Cotton T-Shirt',     'APPAREL',     24.99,  8,   15, 12, 'PRICE_REVIEW_PENDING'),
  (4, 'SKU-APP-002',  'Running Shorts — Navy',      'APPAREL',     39.99,  55,  20, 2,  'ACTIVE'),
  (5, 'SKU-HOME-001', 'Ceramic Pour-Over Set',      'HOME',        49.99,  22,  10, 4,  'ACTIVE'),
  (6, 'SKU-HOME-002', 'LED Desk Lamp — Dimmable',   'HOME',        59.99,  0,   15, 0,  'OUT_OF_STOCK'),
  (7, 'SKU-ELEC-003', 'Portable Charger 20K',       'ELECTRONICS', 44.99,  18,  25, 8,  'ACTIVE'),
  (8, 'SKU-APP-003',  'Hoodie — Heather Grey',      'APPAREL',     54.99,  11,  12, 15, 'ACTIVE');

ALTER TABLE products ALTER COLUMN id RESTART WITH 9;

-- PRD-003 (T-Shirt): stock 8, threshold 15 — already low, good for immediate demo
-- PRD-008 (Hoodie): stock 11, threshold 12, velocity 15 — demand spike demo:
--   POST /products/8/orders simulates viral sale