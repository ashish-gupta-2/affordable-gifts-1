# Database guide

The backend (`backend/`) stores everything in PostgreSQL. This file explains how to
connect to it locally, what is in it, and how it is managed.

## Local setup (what exists on this machine)

| Item | Value |
|---|---|
| Server | PostgreSQL 14, installed with Homebrew (`postgresql@14`), already running |
| Host / port | `localhost` / `5432` |
| Dev database | `affordable_gifts` (used when running the app) |
| Test database | `affordable_gifts_test` (used by `./mvnw test`; wiped before each test) |
| App user | `affordable_gifts_app` (owns both project databases, not a superuser) |
| Password | **required** for both project databases; stored in `backend/.env.local` |

No Docker container is involved. The data lives in Homebrew's data directory
(`/opt/homebrew/var/postgresql@14`) and survives restarts of the app.

## Quick start

Run these from the project root. The password is required, so load the credentials
from `backend/.env.local` first:

```bash
cd backend
set -a; . ./.env.local; set +a      # load credentials into the shell
./mvnw spring-boot:run              # or ./mvnw test

PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USERNAME affordable_gifts
```

What each step does:

| Step | Purpose |
|---|---|
| `set -a; . ./.env.local; set +a` | Exports `DB_USERNAME`, `DB_PASSWORD` and `ADMIN_API_KEY` into the current shell. They last only for that terminal window, so repeat this in each new one. |
| `./mvnw spring-boot:run` | Starts the backend on port 8080. Flyway applies any new migrations on startup. |
| `./mvnw test` | Runs the 34 backend tests against `affordable_gifts_test`. |
| `PGPASSWORD=$DB_PASSWORD psql ...` | Opens a `psql` prompt on the dev database. Type `\dt` to list the tables and `\q` to quit. |

If `psql` or the backend says "no password was provided" or "password authentication
failed", the credentials were not loaded in that terminal. Run the `set -a; . ./.env.local; set +a`
line again.

## Password

Password login is enforced for the two project databases (`affordable_gifts` and
`affordable_gifts_test`) through rules at the top of
`/opt/homebrew/var/postgresql@14/pg_hba.conf` (`scram-sha-256`, over the socket and
`localhost`). Other databases on this server, such as `postgres`, were left alone.

- **User:** `affordable_gifts_app`
- **Password:** a random value saved in **`backend/.env.local`** (git-ignored, mode 600).
  Never commit it. To see it: `cat backend/.env.local`
- A backup of the original config is next to it:
  `pg_hba.conf.bak-before-affordable-gifts`

### Load the credentials into your shell
`.env.local` holds `DB_USERNAME`, `DB_PASSWORD` and `ADMIN_API_KEY`. Load it before
running the backend, the tests or `psql`:
```bash
cd backend
set -a; . ./.env.local; set +a
./mvnw spring-boot:run        # or ./mvnw test
```
Without it the backend refuses to start (`The server requested SCRAM-based
authentication, but no password was provided`).

### Change the password
```bash
NEW=$(openssl rand -hex 16)
psql postgres -c "alter role affordable_gifts_app password '$NEW'"
sed -i '' "s/^DB_PASSWORD=.*/DB_PASSWORD=$NEW/" backend/.env.local
```

### Turn password login off again
Restore the backup and reload:
```bash
cp /opt/homebrew/var/postgresql@14/pg_hba.conf.bak-before-affordable-gifts \
   /opt/homebrew/var/postgresql@14/pg_hba.conf
psql postgres -c "select pg_reload_conf()"
```

### Production
Never reuse this setup. Use a managed secret store, a strong unique password, TLS,
and set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` as environment variables.

## Connecting

### Command line
```bash
cd backend && set -a; . ./.env.local; set +a    # loads DB_USERNAME / DB_PASSWORD
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USERNAME affordable_gifts        # dev data
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USERNAME affordable_gifts_test   # test data
```
Or omit `PGPASSWORD` and type the password when `psql` asks.

Admin tasks (creating or dropping databases, changing roles) still work without a
password as `psql postgres`, which uses your macOS superuser.

### Useful `psql` commands
| Command | What it does |
|---|---|
| `\dt` | List all tables |
| `\d products` | Show a table's columns and constraints |
| `\d+ orders` | Same, with more detail |
| `\x` | Toggle vertical output for wide rows |
| `\q` | Quit |

One-liners without opening `psql`:
```bash
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USERNAME affordable_gifts -c '\dt'
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USERNAME affordable_gifts -c 'select * from orders'
```

### GUI tools (DBeaver, TablePlus, pgAdmin)
| Field | Value |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `affordable_gifts` |
| User | `affordable_gifts_app` |
| Password | the `DB_PASSWORD` value in `backend/.env.local` |

### How the app connects
Defaults are in `backend/src/main/resources/application.properties` and can be
overridden with environment variables:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/affordable_gifts` |
| `DB_USERNAME` | your OS user name (set to `affordable_gifts_app` in `.env.local`) |
| `DB_PASSWORD` | empty (set in `.env.local`) |
| `TEST_DB_URL` | `jdbc:postgresql://localhost:5432/affordable_gifts_test` (tests only) |

The tests (`TEST_DB_URL`) use the same `DB_USERNAME` and `DB_PASSWORD`.

## Tables

| Table | Purpose |
|---|---|
| `products` | Catalogue items (name, category, emoji, active, display order) |
| `product_packs` | One row per pack size of a product (100g / 200g) and its price |
| `packaging_options` | Gift boxes and glass jar sets (slot count, slot size, price) |
| `store_settings` | Single row: minimum order value and delivery charges (editable in the admin console) |
| `orders` | Placed orders: customer, address, totals, status (a price snapshot) |
| `order_items` | What was in one box/jar set of an order, one row per slot |
| `flyway_schema_history` | Flyway's record of which migrations have run |

`orders` and `order_items` have no foreign keys to the catalogue on purpose, so
changing a product or price later never alters an old order.

`order_number_seq` is a sequence that produces order numbers (`AG-1001`, `AG-1002`, ...).

### Handy queries
```sql
select * from products;
select * from product_packs order by product_id, weight_grams;
select * from packaging_options;
select * from store_settings;
select order_number, status, payment_status, total, customer_name, created_at
  from orders order by created_at desc;
select * from order_items;
select version, description, success from flyway_schema_history;
```

## Schema changes (Flyway)

The schema is owned by Flyway, not Hibernate (`ddl-auto=validate`). Migrations run
automatically when the backend starts.

- `backend/src/main/resources/db/migration/` holds real migrations (`V1`, `V2`, ...).
  Add a new numbered file for every schema change. Never edit one that has already run.
- `backend/src/main/resources/db/dev-seed/` holds placeholder catalogue data
  (`V1000`). It is applied only in the default `dev` profile.
- The `dev` profile sets `spring.flyway.out-of-order=true`, because the seed is
  version 1000 and would otherwise block newer migrations on existing dev databases.
- Production: run with `SPRING_PROFILES_ACTIVE=prod` so the seed is skipped.

## Orders and the admin console

Order status and payment status are changed from the admin console (`/admin`), which
updates `orders.status` and `orders.payment_status`. Status values are
`ORDER_RECEIVED`, `PAYMENT_CONFIRMED`, `PREPARING`, `PACKED`, `DISPATCHED`,
`OUT_FOR_DELIVERY`, `DELIVERED` and `CANCELLED`.

**Be careful with deletes:** orders are real customer data. Never run a bare
`delete from orders`. Delete a single test order by its number instead:
```sql
delete from orders where order_number = 'AG-1002';   -- order_items rows go with it
```

## Reset or manage

```bash
# Start over (the next backend start recreates the schema and seed data)
dropdb affordable_gifts && createdb -O affordable_gifts_app affordable_gifts

# Wipe ALL orders and restart numbering at AG-1001 (only on a database with no real orders)
PGPASSWORD=$DB_PASSWORD psql -h localhost -U $DB_USERNAME affordable_gifts -c "delete from orders" -c "alter sequence order_number_seq restart with 1001"

# Stop / start the Postgres server
brew services stop postgresql@14
brew services start postgresql@14
```

Prices in the dev seed are placeholders, not real prices.
