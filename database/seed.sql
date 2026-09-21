-- =====================================================================
-- RashikMart seed.sql
-- ---------------------------------------------------------------------
-- Safe, idempotent seed exported from the LOCAL database.
--   Local H2 DB : ./data/rashikmart.mv.db  (URL: jdbc:h2:./data/rashikmart)
--   Backup      : database/rashikmart-local-backup.mv.db
--   H2 version  : 2.3.232 (see pom.xml)
--   Exported    : seller + 48 products for mohammed786rashik@gmail.com
--
-- The application schema (users/products/etc.) is created automatically by
-- DatabaseContextListener. This script only ADDS data using the existing
-- schema -- it never recreates or drops application tables.
--
-- Safe to run repeatedly: the seller is inserted only when the email is
-- missing, and products are inserted only when the same seller + product
-- name + category does not already exist. It never creates duplicates
-- and never overwrites existing rows.
--
-- The packaged application runs an identical copy of this seed from its
-- classpath (src/main/resources/seed_catalog.sql) so it works inside the
-- deployed WAR on Render. This file (database/seed.sql) is the committed,
-- auditable export of the local demo data.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. DEMO SELLER (created only if the email does not exist yet)
-- ---------------------------------------------------------------------
INSERT INTO users (name, email, password, role)
SELECT 'Mohammed Rashik',
       'mohammed786rashik@gmail.com',
       '$2a$12$29j9yI6m2fTWcgVnZ5TCzuAg95oWKJpDIZNT6L2rtvk57OMvJu4di',
       'SELLER'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'mohammed786rashik@gmail.com'
);

-- ---------------------------------------------------------------------
-- 2. PRODUCT STAGING TABLE (temporary helper, dropped below)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS seed_catalog;

CREATE TABLE seed_catalog (
    name        VARCHAR(150) NOT NULL,
    category    VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    price       DECIMAL(12,2) NOT NULL,
    quantity    INT NOT NULL,
    image_url   VARCHAR(255) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 3. LOCAL PRODUCT DATA (48 rows)
-- ---------------------------------------------------------------------
INSERT INTO seed_catalog (name, category, description, price, quantity, image_url, active, created_at) VALUES
('Wireless Bluetooth Headphones', 'Electronics', 'Comfortable wireless headphones with Bluetooth connectivity, deep bass and up to 20 hours of battery life.', 1499.00, 25, 'wireless-bluetooth-headphones.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Wireless Mouse', 'Electronics', 'Ergonomic 2.4GHz wireless mouse with smooth silent clicks and optical tracking, ideal for office and study.', 499.00, 40, 'wireless-mouse.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Mechanical Keyboard', 'Electronics', 'Mechanical keyboard with tactile switches, RGB backlight and durable keycaps for typing and gaming.', 2799.00, 20, 'mechanical-keyboard.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('USB-C Fast Charger', 'Electronics', 'Compact USB-C fast charger delivering quick, safe top-ups for smartphones and tablets.', 699.00, 50, 'usb-c-fast-charger.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Power Bank', 'Electronics', '10000mAh pocket power bank with dual output ports to keep your phone charged all day.', 1299.00, 35, 'power-bank.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Smart Watch', 'Electronics', 'Feature-packed smart watch with heart-rate monitoring, notifications, steps tracking and IP67 water resistance.', 2999.00, 30, 'smart-watch.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Bluetooth Speaker', 'Electronics', 'Portable Bluetooth speaker with punchy 360-degree sound, built-in mic and 12-hour playtime.', 1199.00, 28, 'bluetooth-speaker.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('USB-C Hub', 'Electronics', '7-in-1 USB-C hub with HDMI, USB 3.0, SD card reader and pass-through charging for laptops.', 1099.00, 22, 'usb-c-hub.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Phone Case', 'Mobiles & Accessories', 'Shock-absorbing phone case with raised edges and a slim, grip-friendly design.', 299.00, 100, 'phone-case.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Tempered Glass', 'Mobiles & Accessories', '9H hardness tempered glass screen protector with oleophobic coating and bubble-free installation.', 149.00, 150, 'tempered-glass.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Type-C Cable', 'Mobiles & Accessories', 'Durable USB Type-C fast-charging cable with braided nylon exterior and tangle-free design.', 199.00, 120, 'type-c-cable.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Lightning Cable', 'Mobiles & Accessories', 'MFi-certified Lightning cable for safe charging and data sync with Apple devices.', 249.00, 80, 'lightning-cable.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Mobile Stand', 'Mobiles & Accessories', 'Adjustable desktop mobile stand with a sturdy grip for hands-free viewing and video calls.', 349.00, 60, 'mobile-stand.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Car Phone Holder', 'Mobiles & Accessories', 'Dashboard car phone holder with strong clamping grip and 360-degree rotation.', 399.00, 45, 'car-phone-holder.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('20W Phone Charger', 'Mobiles & Accessories', '20W USB fast wall charger that powers both Android and Apple smartphones quickly.', 599.00, 70, 'phone-charger-20w.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Wireless Charging Pad', 'Mobiles & Accessories', 'Qi-enabled wireless charging pad with fast charging and over-charge protection.', 899.00, 40, 'wireless-charging-pad.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Laptop Backpack', 'Computers & Accessories', 'Water-resistant laptop backpack with a padded 15.6-inch sleeve and multiple organizer pockets.', 1199.00, 30, 'laptop-backpack.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Laptop Cooling Pad', 'Computers & Accessories', 'Laptop cooling pad with dual quiet fans and an ergonomic stand to prevent overheating.', 1099.00, 25, 'laptop-cooling-pad.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('1080p Webcam', 'Computers & Accessories', 'Plug-and-play 1080p webcam with built-in microphone, auto light correction and universal clip.', 1499.00, 20, 'hd-webcam.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('64GB USB Flash Drive', 'Computers & Accessories', 'High-speed 64GB USB 3.0 flash drive with a compact retractable connector for easy transfer.', 499.00, 90, 'usb-flash-drive-64gb.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Wireless Keyboard & Mouse Combo', 'Computers & Accessories', 'Full-size wireless keyboard and mouse combo with a single USB receiver, quiet keys and long battery life.', 1299.00, 35, 'wireless-keyboard-mouse-combo.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Laptop Stand', 'Computers & Accessories', 'Aluminium laptop stand with an adjustable height and tilt angle for better posture and airflow.', 699.00, 45, 'laptop-stand.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('HDMI Cable', 'Computers & Accessories', 'High-speed HDMI cable with crystal-clear 4K video and audio transmission.', 299.00, 110, 'hdmi-cable.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('External SSD', 'Computers & Accessories', 'Portable 480GB external SSD with blazing-fast USB 3.1 transfer speeds in a pocket-sized body.', 5499.00, 18, 'external-ssd.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Men''s Casual T-Shirt', 'Fashion', 'Soft, breathable cotton t-shirt in a regular fit - a wardrobe essential for everyday wear.', 399.00, 80, 'mens-casual-t-shirt.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Men''s Jeans', 'Fashion', 'Classic straight-fit denim jeans with a comfortable stretch blend for all-day wear.', 1099.00, 60, 'mens-jeans.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Men''s Hoodie', 'Fashion', 'Cozy pullover hoodie with a fleece lining, kangaroo pocket and adjustable drawstrings.', 999.00, 50, 'mens-hoodie.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Women''s Casual T-Shirt', 'Fashion', 'Lightweight casual t-shirt with a flattering regular fit, perfect for layering or on its own.', 349.00, 85, 'womens-casual-t-shirt.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Women''s Hoodie', 'Fashion', 'Soft brushed-fleece hoodie with a relaxed fit and a warm, comfortable hood.', 949.00, 55, 'womens-hoodie.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Casual Sneakers', 'Fashion', 'Lightweight everyday sneakers with cushioned soles and a sleek, versatile look.', 1599.00, 40, 'casual-sneakers.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Backpack', 'Fashion', 'Stylish everyday backpack with padded straps and a roomy main compartment.', 999.00, 65, 'fashion-backpack.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Baseball Cap', 'Fashion', 'Classic baseball cap with an adjustable strap and a curved brim for sun protection.', 249.00, 120, 'baseball-cap.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Electric Kettle', 'Home & Kitchen', 'Stainless steel electric kettle with a rapid boil, auto cut-off and 1.5L capacity.', 799.00, 45, 'electric-kettle.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Stainless Steel Water Bottle', 'Home & Kitchen', 'Double-wall insulated stainless steel bottle that keeps drinks cold for 24 hours or hot for 12.', 499.00, 75, 'stainless-steel-water-bottle.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Coffee Mug', 'Home & Kitchen', 'Large 350ml ceramic coffee mug with a comfortable handle, safe for dishwasher and microwave.', 199.00, 130, 'coffee-mug.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('LED Table Lamp', 'Home & Kitchen', 'Energy-efficient LED table lamp with a flexible arm and three brightness modes for reading.', 549.00, 55, 'led-table-lamp.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Storage Box', 'Home & Kitchen', 'Stackable storage box with a secure lid for keeping clothes, toys and household items organized.', 349.00, 85, 'storage-box.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Kitchen Knife Set', 'Home & Kitchen', 'Professional stainless steel knife set with ergonomic handles covering all everyday cutting needs.', 2199.00, 20, 'kitchen-knife-set.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Lunch Box', 'Home & Kitchen', '3-compartment insulated lunch box with a leak-proof lid, perfect for office and school.', 449.00, 70, 'lunch-box.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Non-Stick Frying Pan', 'Home & Kitchen', 'Non-stick frying pan with an even-heating base and a scratch-resistant coating.', 799.00, 35, 'non-stick-frying-pan.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Java Programming Book', 'Books', 'A hands-on guide to modern Java covering core language features, objects and real-world projects.', 649.00, 40, 'java-programming-book.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Python Programming Book', 'Books', 'A beginner-friendly introduction to Python with practical examples and exercises.', 599.00, 45, 'python-programming-book.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Data Structures & Algorithms Book', 'Books', 'A classic reference for data structures and algorithms with rigorous analysis and examples.', 749.00, 35, 'dsa-book.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('SQL & Database Book', 'Books', 'Master SQL queries, database design and optimization with clear, practical examples.', 549.00, 40, 'sql-database-book.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Web Development Book', 'Books', 'Build modern websites from scratch with HTML, CSS and JavaScript fundamentals.', 699.00, 38, 'web-development-book.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Computer Networks Book', 'Books', 'A top-down introduction to computer networking covering protocols, the internet and applications.', 649.00, 32, 'computer-networks-book.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Operating Systems Book', 'Books', 'An in-depth overview of operating system concepts including processes, memory and storage.', 699.00, 30, 'operating-systems-book.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393'),
('Software Engineering Book', 'Books', 'Core software engineering principles, agile methods and quality practices for building reliable systems.', 749.00, 28, 'software-engineering-book.jpg', true, TIMESTAMP '2026-09-20 22:47:20.476393')
;

-- ---------------------------------------------------------------------
-- 4. INSERT PRODUCTS (idempotent: skips existing seller+name+category)
-- ---------------------------------------------------------------------
INSERT INTO products (seller_id, name, description, category, price, quantity, image_url, active, created_at)
SELECT s.id,
       c.name,
       c.description,
       c.category,
       c.price,
       c.quantity,
       c.image_url,
       c.active,
       c.created_at
FROM seed_catalog c
JOIN users s ON s.email = 'mohammed786rashik@gmail.com'
WHERE NOT EXISTS (
    SELECT 1
    FROM products ex
    WHERE ex.seller_id = s.id
      AND LOWER(ex.name) = LOWER(c.name)
      AND LOWER(ex.category) = LOWER(c.category)
);

DROP TABLE seed_catalog;

-- ---------------------------------------------------------------------
-- 5. VERIFICATION QUERIES
-- ---------------------------------------------------------------------
SELECT 'SELLER' AS check_name, id, name, email, role FROM users WHERE email = 'mohammed786rashik@gmail.com';

SELECT 'CATEGORIES' AS check_name, category, COUNT(*) AS product_count
FROM products
WHERE seller_id = (SELECT id FROM users WHERE email = 'mohammed786rashik@gmail.com')
GROUP BY category
ORDER BY category;

SELECT 'TOTAL' AS check_name, COUNT(*) AS seeded_products
FROM products
WHERE seller_id = (SELECT id FROM users WHERE email = 'mohammed786rashik@gmail.com');
