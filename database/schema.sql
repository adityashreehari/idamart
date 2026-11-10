-- =========================================================
-- IdaMart database schema (MySQL 8)
-- Hibernate creates these tables automatically (ddl-auto=update).
-- This file documents the design and lets you create it by hand
-- in MySQL Workbench if you prefer.
-- =========================================================

CREATE DATABASE IF NOT EXISTS idamart_db;
USE idamart_db;

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(80)  NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)
);

CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)
);

CREATE TABLE IF NOT EXISTS products (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(150)   NOT NULL,
    description  VARCHAR(1000),
    anime_series VARCHAR(100)   NOT NULL,
    price        DECIMAL(10,2)  NOT NULL,
    stock        INT            NOT NULL,
    image_url    VARCHAR(500),
    active       BIT            NOT NULL DEFAULT 1,
    category_id  BIGINT         NOT NULL,
    created_at   DATETIME(6)    NOT NULL,
    updated_at   DATETIME(6),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_product_name (name),
    INDEX idx_product_anime (anime_series)
);

CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    quantity    INT    NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6),
    CONSTRAINT fk_cart_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT        NOT NULL,
    status           VARCHAR(20)   NOT NULL,
    total_amount     DECIMAL(12,2) NOT NULL,
    shipping_address VARCHAR(500)  NOT NULL,
    created_at       DATETIME(6)   NOT NULL,
    updated_at       DATETIME(6),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT        NOT NULL,
    product_id    BIGINT        NOT NULL,
    product_name  VARCHAR(150)  NOT NULL,
    unit_price    DECIMAL(10,2) NOT NULL,
    quantity      INT           NOT NULL,
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6),
    CONSTRAINT fk_item_order   FOREIGN KEY (order_id)   REFERENCES orders(id),
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products(id)
);
