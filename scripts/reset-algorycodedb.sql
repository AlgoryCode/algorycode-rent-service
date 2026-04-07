-- Sıfırdan kurulum: MySQL'de yetkili kullanıcıyla (ör. root) çalıştırın.
-- Uygulama `spring.jpa.hibernate.ddl-auto: update` ile tabloları entity'lere göre oluşturur/günceller.
--
-- Uygulama kullanıcısı (algorycode) DROP sonrası silinmez; gerekirse:
-- GRANT ALL PRIVILEGES ON algorycodedb.* TO 'algorycode'@'%';
-- FLUSH PRIVILEGES;

DROP DATABASE IF EXISTS algorycodedb;
CREATE DATABASE algorycodedb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
