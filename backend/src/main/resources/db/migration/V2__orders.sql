-- Single-row store configuration. The admin console will edit these later.
-- Values are the PRD's demonstration figures; confirm real ones before launch.
create table store_settings (
    id                    smallint      primary key check (id = 1),
    min_order_value       numeric(10,2) not null check (min_order_value >= 0),
    local_delivery_charge numeric(10,2) not null check (local_delivery_charge >= 0),
    india_delivery_charge numeric(10,2) not null check (india_delivery_charge >= 0),
    updated_at            timestamptz   not null default now()
);

insert into store_settings (id, min_order_value, local_delivery_charge, india_delivery_charge)
values (1, 299.00, 50.00, 110.00);

create sequence order_number_seq start with 1001;

-- Orders deliberately have no foreign keys to products / packaging: they are
-- a price snapshot and must stay intact if the catalogue changes later.
create table orders (
    id                   uuid          primary key,
    order_number         varchar(20)   not null unique,
    -- Client-generated id that makes a retried/double-clicked submit harmless.
    request_id           varchar(64)   not null unique,
    status               varchar(30)   not null,
    payment_status       varchar(20)   not null,

    packaging_id         varchar(64)   not null,
    packaging_name       varchar(120)  not null,
    packaging_unit_price numeric(10,2) not null,
    quantity             integer       not null check (quantity > 0),

    products_subtotal    numeric(10,2) not null,
    packaging_total      numeric(10,2) not null,
    delivery_charge      numeric(10,2) not null,
    total                numeric(10,2) not null,
    total_weight_grams   integer       not null,

    customer_name        varchar(120)  not null,
    customer_phone       varchar(15)   not null,
    customer_email       varchar(160),

    address_house        varchar(120)  not null,
    address_street       varchar(160)  not null,
    address_area         varchar(120)  not null,
    address_city         varchar(80)   not null,
    address_state        varchar(80)   not null,
    address_pincode      varchar(6)    not null,
    delivery_instructions varchar(300),

    created_at           timestamptz   not null default now(),
    updated_at           timestamptz   not null default now()
);

-- What went into ONE box / jar set (every copy is identical), one row per slot.
create table order_items (
    order_id     uuid          not null references orders (id) on delete cascade,
    slot_index   integer       not null check (slot_index >= 0),
    product_id   varchar(64)   not null,
    product_name varchar(120)  not null,
    weight_grams integer       not null check (weight_grams > 0),
    unit_price   numeric(10,2) not null check (unit_price >= 0),
    primary key (order_id, slot_index)
);
