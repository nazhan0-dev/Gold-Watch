-- Thai gold prices, one row per type per announcement round (Gold Traders Association).
-- All timestamps are stored in UTC; convert to Asia/Bangkok only for display.
CREATE TABLE gold_price (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    type         VARCHAR(16)   NOT NULL COMMENT 'BAR = gold bar, ORNAMENT = gold ornament (96.5%)',
    buy          DECIMAL(12, 2) NOT NULL COMMENT 'Buy-back price (THB per baht-weight)',
    sell         DECIMAL(12, 2) NOT NULL COMMENT 'Selling price (THB per baht-weight)',
    announced_at DATETIME      NOT NULL COMMENT 'Announcement time in UTC',
    source       VARCHAR(64)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_gold_price_type CHECK (type IN ('BAR', 'ORNAMENT')),
    INDEX idx_gold_price_type_announced_at (type, announced_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- World spot gold (USD per troy ounce) and the USD/THB exchange rate.
CREATE TABLE spot_price (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    xau_usd     DECIMAL(12, 4) NOT NULL COMMENT 'Gold spot price, USD per troy ounce',
    usd_thb     DECIMAL(10, 4) NULL COMMENT 'THB per 1 USD; NULL until an FX source has answered',
    recorded_at DATETIME       NOT NULL COMMENT 'Quote time in UTC',
    source      VARCHAR(64)    NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_spot_price_recorded_at (recorded_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
