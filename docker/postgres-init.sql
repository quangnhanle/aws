-- ============================================================
-- Init script cho Postgres (chạy 1 lần khi container khởi tạo lần đầu)
-- Được mount vào /docker-entrypoint-initdb.d/ trong docker-compose.yml
-- Mục tiêu: 1 instance Postgres chứa 2 DB riêng cho 2 service
--   - products_db  -> product-service
--   - orders_db    -> order-service
--
-- Lưu ý: script này CHỈ chạy khi thư mục data còn trống (lần đầu).
-- Nếu đã chạy compose trước đó, cần xoá volume để init lại:
--   docker compose down -v && docker compose up -d
-- ============================================================

-- CREATE DATABASE không cho phép IF NOT EXISTS, nên dùng cách kiểm tra
-- qua pg_database rồi tạo động để script chạy lại không lỗi.

SELECT 'CREATE DATABASE products_db OWNER app'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'products_db')\gexec

SELECT 'CREATE DATABASE orders_db OWNER app'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'orders_db')\gexec

-- Cấp toàn quyền cho user app trên 2 DB
GRANT ALL PRIVILEGES ON DATABASE products_db TO app;
GRANT ALL PRIVILEGES ON DATABASE orders_db  TO app;
