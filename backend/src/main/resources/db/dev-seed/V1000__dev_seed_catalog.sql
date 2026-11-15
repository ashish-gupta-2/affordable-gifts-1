-- Placeholder catalogue for local development only (prices are not final).
insert into products (id, name, category, emoji, display_order) values
    ('almonds',       'Almonds',       'Dry Fruits', '🌰', 1),
    ('cashews',       'Cashews',       'Dry Fruits', '🥜', 2),
    ('raisins',       'Raisins',       'Dry Fruits', '🍇', 3),
    ('roasted-moong', 'Roasted Moong', 'Snacks',     '🫘', 4),
    ('roasted-chana', 'Roasted Chana', 'Snacks',     '🧆', 5);

insert into product_packs (product_id, weight_grams, price) values
    ('almonds',       100, 110), ('almonds',       200, 210),
    ('cashews',       100, 130), ('cashews',       200, 250),
    ('raisins',       100,  60), ('raisins',       200, 110),
    ('roasted-moong', 100,  40), ('roasted-moong', 200,  75),
    ('roasted-chana', 100,  30), ('roasted-chana', 200,  55);

insert into packaging_options
    (id, name, kind, description, price, slot_count, slot_capacity, display_order) values
    ('box-2',   '2 Partition Box', 'box', '2 partitions × 100g',     30, 2, 100, 1),
    ('box-3',   '3 Partition Box', 'box', '3 partitions × 100g',     45, 3, 100, 2),
    ('box-4',   '4 Partition Box', 'box', '4 partitions × 100g',     60, 4, 100, 3),
    ('jar-100', '100g Glass Jars', 'jar', 'Cover with 3 jars × 100g', 80, 3, 100, 4),
    ('jar-200', '200g Glass Jars', 'jar', 'Cover with 2 jars × 200g', 90, 2, 200, 5);
