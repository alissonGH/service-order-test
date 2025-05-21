-- Criação da tabela de pedidos
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    external_id VARCHAR(100) UNIQUE NOT NULL,
    total_value NUMERIC(10, 2),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criação da tabela de produtos
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    quantity INT NOT NULL
);

-- Tabela associativa entre pedidos e produtos
CREATE TABLE IF NOT EXISTS order_products (
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Inserção de exemplo na tabela de produtos (produtos pré-cadastrados)
INSERT INTO products (name, price, quantity) VALUES
    ('Produto A', 99.95, 100),
    ('Produto B', 49.95, 200);

-- Inserção de um pedido de exemplo
INSERT INTO orders (external_id, total_value, status) VALUES
    ('pedido-001', 199.85, 'PENDING');

-- Inserção da associação do pedido aos produtos (assumindo order_id = 1 e product_id 1 e 2)
INSERT INTO order_products (order_id, product_id) VALUES
    (1, 1),
    (1, 2);
