CREATE TABLE orders (
    id         UUID         PRIMARY KEY,
    product_id UUID         NOT NULL,
    quantity   INTEGER      NOT NULL,
    status     VARCHAR(20)  NOT NULL
);
