create table products (
    id            varchar(64)  primary key,
    name          varchar(120) not null,
    category      varchar(60)  not null,
    emoji         varchar(16)  not null,
    active        boolean      not null default true,
    display_order integer      not null default 0,
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now()
);

-- One row per pack size a product is sold in (e.g. 100g and 200g).
create table product_packs (
    product_id   varchar(64)   not null references products (id) on delete cascade,
    weight_grams integer       not null check (weight_grams > 0),
    price        numeric(10,2) not null check (price >= 0),
    primary key (product_id, weight_grams)
);

-- A gift box (partitions) or a glass jar set. Every slot holds exactly one
-- product pack of slot_capacity grams.
create table packaging_options (
    id            varchar(64)   primary key,
    name          varchar(120)  not null,
    kind          varchar(10)   not null check (kind in ('box', 'jar')),
    description   varchar(200)  not null,
    price         numeric(10,2) not null check (price >= 0),
    slot_count    integer       not null check (slot_count > 0),
    slot_capacity integer       not null check (slot_capacity > 0),
    active        boolean       not null default true,
    display_order integer       not null default 0,
    created_at    timestamptz   not null default now(),
    updated_at    timestamptz   not null default now()
);
