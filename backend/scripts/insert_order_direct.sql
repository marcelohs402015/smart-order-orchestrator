INSERT INTO orders (id, order_number, status, customer_id, customer_name, customer_email, total_amount, payment_id, risk_level, created_at, updated_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'ORD-20241211001', 'PENDING', '660e8400-e29b-41d4-a716-446655440001', 'João Silva', 'joao.silva@example.com', 1550.00, NULL, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price)
VALUES (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000', gen_random_uuid(), 'Notebook Dell Inspiron 15', 1, 1200.00);

INSERT INTO order_items (id, order_id, product_id, product_name, quantity, unit_price)
VALUES (gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440000', gen_random_uuid(), 'Mouse Logitech MX Master 3', 1, 350.00);

