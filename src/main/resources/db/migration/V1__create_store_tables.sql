-- Product (legacy - walking skeleton)
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE PRECISION NOT NULL
);

-- Store commerce
CREATE TABLE store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    address VARCHAR(255),
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    status VARCHAR(20) NOT NULL,
    pickup_expiry_delay_min INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT store_status_valid CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT store_latitude_range CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT store_longitude_range CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180)),
    CONSTRAINT store_coords_both_null CHECK ((latitude IS NULL) = (longitude IS NULL)),
    CONSTRAINT store_pickup_delay_positive CHECK (pickup_expiry_delay_min > 0)
);

CREATE INDEX idx_store_owner_id ON store(owner_id);

-- Store opening hours
CREATE TABLE store_opening_hours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT opening_hours_day_range CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT opening_hours_time_order CHECK (close_time > open_time)
);

CREATE INDEX idx_store_opening_hours_store_id ON store_opening_hours(store_id);

-- Store exceptional hours
CREATE TABLE store_exceptional_hours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_closed BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT exceptional_hours_date_order CHECK (end_date >= start_date)
);

CREATE INDEX idx_store_exceptional_hours_store_id ON store_exceptional_hours(store_id);

-- Article (product or service)
CREATE TABLE article (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL REFERENCES store(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price_cents BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT article_price_non_negative CHECK (price_cents >= 0),
    CONSTRAINT article_type_valid CHECK (type IN ('PRODUCT', 'SERVICE')),
    CONSTRAINT article_name_unique_per_store UNIQUE (store_id, name)
);

CREATE INDEX idx_article_store_id ON article(store_id);

-- Stock quantities
CREATE TABLE stock (
    article_id UUID PRIMARY KEY REFERENCES article(id) ON DELETE CASCADE,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT stock_quantities_non_negative CHECK (available_quantity >= 0 AND reserved_quantity >= 0)
);

-- Stock reservations
CREATE TABLE stock_reservation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    article_id UUID NOT NULL REFERENCES article(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT reservation_quantity_positive CHECK (quantity > 0),
    CONSTRAINT reservation_status_valid CHECK (status IN ('RESERVED', 'CONSUMED', 'RELEASED')),
    CONSTRAINT reservation_order_article_unique UNIQUE (order_id, article_id)
);

CREATE INDEX idx_stock_reservation_article_id ON stock_reservation(article_id);
CREATE INDEX idx_stock_reservation_order_id ON stock_reservation(order_id);
