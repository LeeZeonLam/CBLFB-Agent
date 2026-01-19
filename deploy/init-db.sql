-- FBA LogiAI 数据库初始化脚本
-- 营销域表结构

-- 活动表
CREATE TABLE IF NOT EXISTS raffle_activity (
    id BIGSERIAL PRIMARY KEY,
    activity_id BIGINT NOT NULL UNIQUE,
    activity_name VARCHAR(64) NOT NULL,
    activity_desc VARCHAR(128),
    begin_date_time TIMESTAMP NOT NULL,
    end_date_time TIMESTAMP NOT NULL,
    stock_count INT NOT NULL DEFAULT 0,
    stock_count_surplus INT NOT NULL DEFAULT 0,
    state VARCHAR(8) NOT NULL DEFAULT 'create',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_raffle_activity_state ON raffle_activity(state);

-- 策略表
CREATE TABLE IF NOT EXISTS strategy (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL UNIQUE,
    strategy_desc VARCHAR(128),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 策略奖品表
CREATE TABLE IF NOT EXISTS strategy_award (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    award_id INT NOT NULL,
    award_title VARCHAR(128) NOT NULL,
    award_subtitle VARCHAR(128),
    award_count INT NOT NULL DEFAULT 0,
    award_count_surplus INT NOT NULL DEFAULT 0,
    award_rate DECIMAL(6,4) NOT NULL,
    rule_models VARCHAR(256),
    sort INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(strategy_id, award_id)
);

CREATE INDEX IF NOT EXISTS idx_strategy_award_strategy_id ON strategy_award(strategy_id);

-- 奖品表
CREATE TABLE IF NOT EXISTS award (
    id BIGSERIAL PRIMARY KEY,
    award_id INT NOT NULL UNIQUE,
    award_key VARCHAR(32) NOT NULL,
    award_config VARCHAR(1024),
    award_desc VARCHAR(128),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 策略规则表
CREATE TABLE IF NOT EXISTS strategy_rule (
    id BIGSERIAL PRIMARY KEY,
    strategy_id BIGINT NOT NULL,
    award_id INT,
    rule_type VARCHAR(16) NOT NULL,
    rule_model VARCHAR(32) NOT NULL,
    rule_value VARCHAR(1024),
    rule_desc VARCHAR(128),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_strategy_rule_strategy_id ON strategy_rule(strategy_id);

-- 规则树表
CREATE TABLE IF NOT EXISTS rule_tree (
    id BIGSERIAL PRIMARY KEY,
    tree_id VARCHAR(32) NOT NULL UNIQUE,
    tree_name VARCHAR(64) NOT NULL,
    tree_desc VARCHAR(128),
    tree_root_rule_key VARCHAR(32) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 规则树节点表
CREATE TABLE IF NOT EXISTS rule_tree_node (
    id BIGSERIAL PRIMARY KEY,
    tree_id VARCHAR(32) NOT NULL,
    rule_key VARCHAR(32) NOT NULL,
    rule_desc VARCHAR(128),
    rule_value VARCHAR(1024),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rule_tree_node_tree_id ON rule_tree_node(tree_id);

-- 规则树连线表
CREATE TABLE IF NOT EXISTS rule_tree_node_line (
    id BIGSERIAL PRIMARY KEY,
    tree_id VARCHAR(32) NOT NULL,
    rule_node_from VARCHAR(32) NOT NULL,
    rule_node_to VARCHAR(32) NOT NULL,
    rule_limit_type VARCHAR(8),
    rule_limit_value VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rule_tree_node_line_tree_id ON rule_tree_node_line(tree_id);

-- 订单域表结构

-- 发货订单表（扩展版）
CREATE TABLE IF NOT EXISTS shipment_order (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id VARCHAR(32) NOT NULL,
    -- 客户关联
    customer_id BIGINT,
    customer_no VARCHAR(32),
    customer_name VARCHAR(128),
    customer_address_id BIGINT,
    -- 基础信息
    order_type VARCHAR(16),
    origin_address VARCHAR(256),
    dest_country VARCHAR(8),
    fba_warehouse_code VARCHAR(16),
    shipping_method VARCHAR(16),
    -- 渠道和包装
    channel_code VARCHAR(32),
    channel_name VARCHAR(64),
    packaging_type VARCHAR(16),
    -- 派送地址
    delivery_address_type VARCHAR(16),
    delivery_address_detail TEXT,
    delivery_warehouse_code VARCHAR(32),
    -- 拼柜仓库（国内拼柜仓库）
    consolidation_warehouse_code VARCHAR(32),
    -- 员工关联
    customer_service_id BIGINT,
    customer_service_name VARCHAR(64),
    operator_id BIGINT,
    operator_name VARCHAR(64),
    finance_id BIGINT,
    finance_name VARCHAR(64),
    -- 货物信息
    total_weight DECIMAL(10,2),
    total_volume DECIMAL(10,4),
    total_pieces INT DEFAULT 0,
    estimated_cost DECIMAL(12,2),
    -- 柜子关联
    container_id BIGINT,
    container_no VARCHAR(32),
    -- 状态
    state VARCHAR(32) NOT NULL DEFAULT 'draft',
    reject_reason VARCHAR(256),
    -- 时间节点
    received_time TIMESTAMP,
    dimension_recorded_time TIMESTAMP,
    loaded_time TIMESTAMP,
    actual_departure_time TIMESTAMP,
    actual_arrival_port_time TIMESTAMP,
    picked_time TIMESTAMP,
    delivery_start_time TIMESTAMP,
    completed_time TIMESTAMP,
    -- 通用字段
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shipment_order_user_id ON shipment_order(user_id);
CREATE INDEX IF NOT EXISTS idx_shipment_order_customer_id ON shipment_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_shipment_order_state ON shipment_order(state);
CREATE INDEX IF NOT EXISTS idx_shipment_order_container_id ON shipment_order(container_id);

-- 产品表（产品主数据）
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    sku VARCHAR(64) NOT NULL UNIQUE,
    asin VARCHAR(32),
    fnsku VARCHAR(32),
    product_name VARCHAR(256) NOT NULL,
    product_name_en VARCHAR(256),
    description TEXT,
    hs_code VARCHAR(32),
    -- 尺寸重量
    unit_weight DECIMAL(10,3),
    length DECIMAL(10,2),
    width DECIMAL(10,2),
    height DECIMAL(10,2),
    -- 价格
    unit_price DECIMAL(12,2),
    currency VARCHAR(8) DEFAULT 'USD',
    -- 敏感货标识
    has_battery BOOLEAN DEFAULT FALSE,
    battery_type VARCHAR(32),
    is_liquid BOOLEAN DEFAULT FALSE,
    is_powder BOOLEAN DEFAULT FALSE,
    is_magnetic BOOLEAN DEFAULT FALSE,
    is_dangerous BOOLEAN DEFAULT FALSE,
    -- 其他
    origin_country VARCHAR(8),
    image_url VARCHAR(512),
    status VARCHAR(16) NOT NULL DEFAULT 'active',
    remark VARCHAR(256),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_asin ON product(asin);
CREATE INDEX IF NOT EXISTS idx_product_fnsku ON product(fnsku);
CREATE INDEX IF NOT EXISTS idx_product_status ON product(status);

-- 订单产品关联表（订单中的产品明细）
CREATE TABLE IF NOT EXISTS order_product (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(64) NOT NULL,
    product_name VARCHAR(256),
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2),
    currency VARCHAR(8) DEFAULT 'USD',
    -- 冗余尺寸重量（下单时快照）
    unit_weight DECIMAL(10,3),
    length DECIMAL(10,2),
    width DECIMAL(10,2),
    height DECIMAL(10,2),
    -- 敏感货标识
    has_battery BOOLEAN DEFAULT FALSE,
    is_liquid BOOLEAN DEFAULT FALSE,
    is_powder BOOLEAN DEFAULT FALSE,
    is_magnetic BOOLEAN DEFAULT FALSE,
    -- 其他
    hs_code VARCHAR(32),
    remark VARCHAR(256),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_product_order_id ON order_product(order_id);
CREATE INDEX IF NOT EXISTS idx_order_product_product_id ON order_product(product_id);
CREATE INDEX IF NOT EXISTS idx_order_product_sku ON order_product(sku);

-- 仓储域表结构

-- 托盘表
CREATE TABLE IF NOT EXISTS pallet (
    id BIGSERIAL PRIMARY KEY,
    pallet_id VARCHAR(32) NOT NULL UNIQUE,
    order_id VARCHAR(32) NOT NULL,
    length DECIMAL(10,2),
    width DECIMAL(10,2),
    height DECIMAL(10,2),
    weight DECIMAL(10,2),
    label_verified BOOLEAN DEFAULT FALSE,
    state VARCHAR(16) NOT NULL DEFAULT 'pending',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pallet_order_id ON pallet(order_id);

-- 纸箱表
CREATE TABLE IF NOT EXISTS carton (
    id BIGSERIAL PRIMARY KEY,
    carton_id BIGINT NOT NULL UNIQUE,
    carton_no VARCHAR(32) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    pallet_id BIGINT,
    fba_label VARCHAR(64),
    length DECIMAL(10,2),
    width DECIMAL(10,2),
    height DECIMAL(10,2),
    weight DECIMAL(10,2),
    sku VARCHAR(64),
    quantity INT DEFAULT 1,
    status VARCHAR(16) NOT NULL DEFAULT 'pending',
    has_sensitive BOOLEAN DEFAULT FALSE,
    need_special_handle BOOLEAN DEFAULT FALSE,
    special_handle_note VARCHAR(256),
    remark VARCHAR(256),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_carton_order_id ON carton(order_id);
CREATE INDEX IF NOT EXISTS idx_carton_pallet_id ON carton(pallet_id);

-- 库位表
CREATE TABLE IF NOT EXISTS warehouse_location (
    id BIGSERIAL PRIMARY KEY,
    location_id BIGINT NOT NULL UNIQUE,
    location_code VARCHAR(32) NOT NULL UNIQUE,
    warehouse_code VARCHAR(32) NOT NULL,
    zone VARCHAR(8),
    row VARCHAR(8),
    col VARCHAR(8),
    level VARCHAR(8),
    location_type VARCHAR(16) NOT NULL DEFAULT 'PALLET',
    status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
    current_pallet_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_warehouse_location_warehouse_code ON warehouse_location(warehouse_code);
CREATE INDEX IF NOT EXISTS idx_warehouse_location_status ON warehouse_location(status);

-- 材积记录表
CREATE TABLE IF NOT EXISTS dimension_record (
    id BIGSERIAL PRIMARY KEY,
    record_id BIGINT NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    carton_id BIGINT,
    measure_sequence INT DEFAULT 1,
    length DECIMAL(10,2),
    width DECIMAL(10,2),
    height DECIMAL(10,2),
    actual_weight DECIMAL(10,2),
    volume DECIMAL(10,4),
    volumetric_weight DECIMAL(10,2),
    chargeable_weight DECIMAL(10,2),
    oversized BOOLEAN DEFAULT FALSE,
    overweight BOOLEAN DEFAULT FALSE,
    measured_by VARCHAR(64),
    measured_time TIMESTAMP,
    is_final BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_dimension_record_order_id ON dimension_record(order_id);

-- 船运域表结构

-- 集装箱表
CREATE TABLE IF NOT EXISTS container (
    id BIGSERIAL PRIMARY KEY,
    container_id BIGINT NOT NULL UNIQUE,
    container_no VARCHAR(32) NOT NULL UNIQUE,
    container_type VARCHAR(8) NOT NULL,
    seal_no VARCHAR(32),
    max_capacity DECIMAL(10,2),
    max_weight DECIMAL(10,2),
    current_capacity DECIMAL(10,2) DEFAULT 0,
    current_weight DECIMAL(10,2) DEFAULT 0,
    voyage_id BIGINT,
    voyage_no VARCHAR(32),
    status VARCHAR(16) NOT NULL DEFAULT 'EMPTY',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_container_voyage_id ON container(voyage_id);
CREATE INDEX IF NOT EXISTS idx_container_status ON container(status);

-- 航次表
CREATE TABLE IF NOT EXISTS voyage (
    id BIGSERIAL PRIMARY KEY,
    voyage_id BIGINT NOT NULL UNIQUE,
    voyage_no VARCHAR(32) NOT NULL UNIQUE,
    vessel_name VARCHAR(64),
    carrier VARCHAR(64),
    channel_code VARCHAR(32),
    channel_name VARCHAR(64),
    departure_port VARCHAR(64),
    arrival_port VARCHAR(64),
    estimated_departure TIMESTAMP,
    actual_departure TIMESTAMP,
    estimated_arrival TIMESTAMP,
    actual_arrival TIMESTAMP,
    status VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_voyage_status ON voyage(status);
CREATE INDEX IF NOT EXISTS idx_voyage_estimated_departure ON voyage(estimated_departure);

-- 报关单表
CREATE TABLE IF NOT EXISTS customs_declaration (
    id BIGSERIAL PRIMARY KEY,
    declaration_id BIGINT NOT NULL UNIQUE,
    declaration_no VARCHAR(32) NOT NULL UNIQUE,
    -- 关联信息
    container_id BIGINT,
    container_no VARCHAR(32),
    voyage_id BIGINT,
    -- 报关信息
    declaration_type VARCHAR(16) NOT NULL DEFAULT 'EXPORT',
    customs_port VARCHAR(64),
    broker_name VARCHAR(128),
    broker_contact VARCHAR(64),
    broker_phone VARCHAR(32),
    -- 状态
    status VARCHAR(16) NOT NULL DEFAULT 'pending',
    -- 申报信息
    declared_value DECIMAL(12,2),
    currency VARCHAR(8) DEFAULT 'USD',
    declared_weight DECIMAL(10,2),
    declared_pieces INT,
    hs_codes VARCHAR(512),
    goods_description TEXT,
    -- 文件和备注
    document_urls TEXT,
    inspection_reason VARCHAR(256),
    reject_reason VARCHAR(256),
    remark VARCHAR(256),
    -- 时间节点
    declared_time TIMESTAMP,
    cleared_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customs_declaration_container_id ON customs_declaration(container_id);
CREATE INDEX IF NOT EXISTS idx_customs_declaration_voyage_id ON customs_declaration(voyage_id);
CREATE INDEX IF NOT EXISTS idx_customs_declaration_status ON customs_declaration(status);

-- 客户域表结构

-- 客户表
CREATE TABLE IF NOT EXISTS customer (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE,
    customer_no VARCHAR(32) NOT NULL UNIQUE,
    company_name VARCHAR(128) NOT NULL,
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    email VARCHAR(128),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customer_status ON customer(status);

-- 客户地址表
CREATE TABLE IF NOT EXISTS customer_address (
    id BIGSERIAL PRIMARY KEY,
    address_id BIGINT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    address_type VARCHAR(16) NOT NULL,
    address_name VARCHAR(64),
    recipient_name VARCHAR(64) NOT NULL,
    company_name VARCHAR(128),
    address_line1 VARCHAR(256) NOT NULL,
    address_line2 VARCHAR(256),
    city VARCHAR(64) NOT NULL,
    state VARCHAR(64),
    zip_code VARCHAR(16),
    country_code VARCHAR(8) NOT NULL,
    phone VARCHAR(32),
    is_default BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customer_address_customer_id ON customer_address(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_address_type ON customer_address(address_type);

-- 基础数据域表结构

-- FBA/AWD 仓库表
CREATE TABLE IF NOT EXISTS fba_warehouse (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL UNIQUE,
    warehouse_code VARCHAR(16) NOT NULL UNIQUE,
    warehouse_name VARCHAR(128),
    warehouse_type VARCHAR(8) NOT NULL DEFAULT 'FBA',
    country VARCHAR(8) NOT NULL,
    state VARCHAR(64),
    city VARCHAR(64),
    address_line1 VARCHAR(256),
    address_line2 VARCHAR(256),
    zip_code VARCHAR(16),
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    accepting_shipments BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fba_warehouse_country ON fba_warehouse(country);
CREATE INDEX IF NOT EXISTS idx_fba_warehouse_type ON fba_warehouse(warehouse_type);

-- 第三方海外仓表
CREATE TABLE IF NOT EXISTS third_party_warehouse (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL UNIQUE,
    warehouse_code VARCHAR(32) NOT NULL UNIQUE,
    warehouse_name VARCHAR(128) NOT NULL,
    provider_name VARCHAR(128),
    country VARCHAR(8) NOT NULL,
    state VARCHAR(64),
    city VARCHAR(64),
    address VARCHAR(256),
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    service_types VARCHAR(256),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_third_party_warehouse_country ON third_party_warehouse(country);

-- 交货仓库表（国内仓库）
CREATE TABLE IF NOT EXISTS delivery_warehouse (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL UNIQUE,
    warehouse_code VARCHAR(32) NOT NULL UNIQUE,
    warehouse_name VARCHAR(128) NOT NULL,
    province VARCHAR(32),
    city VARCHAR(32),
    district VARCHAR(32),
    address VARCHAR(256),
    contact_name VARCHAR(64),
    contact_phone VARCHAR(32),
    working_hours VARCHAR(64),
    supported_channels VARCHAR(256),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 员工表
CREATE TABLE IF NOT EXISTS employee (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL UNIQUE,
    employee_no VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    phone VARCHAR(32),
    email VARCHAR(128),
    status VARCHAR(16) NOT NULL DEFAULT 'active',
    remark VARCHAR(256),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_employee_role ON employee(role);
CREATE INDEX IF NOT EXISTS idx_employee_department ON employee(department);
CREATE INDEX IF NOT EXISTS idx_employee_status ON employee(status);

-- 渠道表
CREATE TABLE IF NOT EXISTS shipping_channel (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL UNIQUE,
    channel_code VARCHAR(32) NOT NULL UNIQUE,
    channel_name VARCHAR(64) NOT NULL,
    carrier VARCHAR(64),
    transport_type VARCHAR(16),
    departure_port VARCHAR(64),
    arrival_ports TEXT,
    transit_days INT,
    price_per_cbm DECIMAL(10,2),
    price_per_kg DECIMAL(10,2),
    min_weight DECIMAL(10,2),
    max_weight DECIMAL(10,2),
    supported_container_types VARCHAR(64),
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化测试数据
INSERT INTO award (award_id, award_key, award_config, award_desc)
VALUES (1, 'coupon_10', '{"amount": 10}', '10元优惠券')
ON CONFLICT (award_id) DO NOTHING;

INSERT INTO award (award_id, award_key, award_config, award_desc)
VALUES (2, 'coupon_50', '{"amount": 50}', '50元优惠券')
ON CONFLICT (award_id) DO NOTHING;

INSERT INTO award (award_id, award_key, award_config, award_desc)
VALUES (3, 'free_shipping', '{"type": "free_shipping"}', '免运费')
ON CONFLICT (award_id) DO NOTHING;

COMMENT ON TABLE raffle_activity IS '抽奖活动表';
COMMENT ON TABLE strategy IS '抽奖策略表';
COMMENT ON TABLE strategy_award IS '策略奖品表';
COMMENT ON TABLE award IS '奖品表';
COMMENT ON TABLE strategy_rule IS '策略规则表';
COMMENT ON TABLE shipment_order IS '发货订单表';
COMMENT ON TABLE pallet IS '托盘表';
COMMENT ON TABLE carton IS '纸箱表';
COMMENT ON TABLE warehouse_location IS '库位表';
COMMENT ON TABLE dimension_record IS '材积记录表';
COMMENT ON TABLE container IS '集装箱表';
COMMENT ON TABLE voyage IS '航次表';
COMMENT ON TABLE customer IS '客户表';
COMMENT ON TABLE customer_address IS '客户地址表';
COMMENT ON TABLE fba_warehouse IS 'FBA/AWD仓库表';
COMMENT ON TABLE third_party_warehouse IS '第三方海外仓表';
COMMENT ON TABLE delivery_warehouse IS '交货仓库表';
COMMENT ON TABLE shipping_channel IS '渠道表';
COMMENT ON TABLE employee IS '员工表';
COMMENT ON TABLE customs_declaration IS '报关单表';
COMMENT ON TABLE product IS '产品表';
COMMENT ON TABLE order_product IS '订单产品关联表';

-- ==================== 初始化基础数据 ====================

-- FBA 仓库数据（美国主要仓库）
INSERT INTO fba_warehouse (warehouse_id, warehouse_code, warehouse_name, warehouse_type, country, state, city, address_line1, zip_code, accepting_shipments, enabled)
VALUES
    (1, 'ONT8', 'ONT8 - Ontario', 'FBA', 'US', 'CA', 'Moreno Valley', '24208 San Michele Rd', '92551', TRUE, TRUE),
    (2, 'PHX6', 'PHX6 - Phoenix', 'FBA', 'US', 'AZ', 'Phoenix', '4750 W Mohave St', '85043', TRUE, TRUE),
    (3, 'LAS1', 'LAS1 - Las Vegas', 'FBA', 'US', 'NV', 'Las Vegas', '4550 W Tropicana Ave', '89103', TRUE, TRUE),
    (4, 'LAX9', 'LAX9 - Los Angeles', 'FBA', 'US', 'CA', 'San Bernardino', '2020 E Central Ave', '92408', TRUE, TRUE),
    (5, 'SMF3', 'SMF3 - Sacramento', 'FBA', 'US', 'CA', 'West Sacramento', '3923 Seaport Blvd', '95691', TRUE, TRUE),
    (6, 'SJC7', 'SJC7 - San Jose', 'FBA', 'US', 'CA', 'Tracy', '5089 Walnut Ave', '95377', TRUE, TRUE),
    (7, 'DFW7', 'DFW7 - Dallas', 'FBA', 'US', 'TX', 'Dallas', '700 Westport Pkwy', '76177', TRUE, TRUE),
    (8, 'HOU2', 'HOU2 - Houston', 'FBA', 'US', 'TX', 'Houston', '10550 Ella Blvd', '77067', TRUE, TRUE),
    (9, 'MDW2', 'MDW2 - Chicago', 'FBA', 'US', 'IL', 'Chicago', '250 Emerald Dr', '60446', TRUE, TRUE),
    (10, 'AVP1', 'AVP1 - Pennsylvania', 'FBA', 'US', 'PA', 'Hazleton', '550 Oak Ridge Rd', '18202', TRUE, TRUE)
ON CONFLICT (warehouse_code) DO NOTHING;

-- AWD 仓库数据
INSERT INTO fba_warehouse (warehouse_id, warehouse_code, warehouse_name, warehouse_type, country, state, city, address_line1, zip_code, accepting_shipments, enabled)
VALUES
    (101, 'AWD-ONT', 'AWD Ontario', 'AWD', 'US', 'CA', 'Ontario', '1234 AWD Way', '91761', TRUE, TRUE),
    (102, 'AWD-DFW', 'AWD Dallas', 'AWD', 'US', 'TX', 'Dallas', '5678 AWD Blvd', '75201', TRUE, TRUE),
    (103, 'AWD-MDW', 'AWD Chicago', 'AWD', 'US', 'IL', 'Chicago', '9012 AWD St', '60601', TRUE, TRUE)
ON CONFLICT (warehouse_code) DO NOTHING;

-- 第三方海外仓数据
INSERT INTO third_party_warehouse (warehouse_id, warehouse_code, warehouse_name, provider_name, country, state, city, address, contact_name, contact_phone, service_types, enabled)
VALUES
    (1, 'WAN-LA', '万邦国际-洛杉矶仓', '万邦国际', 'US', 'CA', 'Los Angeles', '1000 E Artesia Blvd, Carson, CA 90746', '王经理', '+1-310-555-0001', 'FBA转运,一件代发,仓储', TRUE),
    (2, 'YC-LA', '谷仓海外仓-洛杉矶', '谷仓海外仓', 'US', 'CA', 'Los Angeles', '2000 S Santa Fe Ave, Los Angeles, CA 90058', '李经理', '+1-310-555-0002', 'FBA转运,一件代发', TRUE),
    (3, 'SF-NJ', '顺丰海外仓-新泽西', '顺丰国际', 'US', 'NJ', 'Newark', '3000 Newark Turnpike, Newark, NJ 07114', '张经理', '+1-201-555-0003', 'FBA转运,退货处理', TRUE)
ON CONFLICT (warehouse_code) DO NOTHING;

-- 交货仓库数据（国内仓库）
INSERT INTO delivery_warehouse (warehouse_id, warehouse_code, warehouse_name, province, city, district, address, contact_name, contact_phone, working_hours, supported_channels, enabled)
VALUES
    (1, 'SZ-YT', '深圳盐田仓', '广东省', '深圳市', '盐田区', '盐田港保税物流园区', '陈主管', '0755-12345678', '09:00-18:00', 'MATSON,ZIM,YANTIAN', TRUE),
    (2, 'GZ-NX', '广州南沙仓', '广东省', '广州市', '南沙区', '南沙港物流园', '黄主管', '020-87654321', '08:30-17:30', 'MATSON,ZIM,YANTIAN', TRUE),
    (3, 'NB-BL', '宁波北仑仓', '浙江省', '宁波市', '北仑区', '北仑港物流中心', '刘主管', '0574-11112222', '09:00-18:00', 'MATSON,ZIM', TRUE),
    (4, 'SH-WG', '上海外高桥仓', '上海市', '浦东新区', '外高桥', '外高桥保税区', '周主管', '021-33334444', '08:00-17:00', 'ZIM,YANTIAN', TRUE)
ON CONFLICT (warehouse_code) DO NOTHING;

-- 渠道数据
INSERT INTO shipping_channel (channel_id, channel_code, channel_name, carrier, transport_type, departure_port, arrival_ports, transit_days, price_per_cbm, price_per_kg, min_weight, max_weight, supported_container_types, enabled)
VALUES
    (1, 'MATSON', '美森快船', 'Matson', 'SEA_EXPRESS', '深圳盐田/宁波', '洛杉矶(LAX),长滩(LGB)', 12, 680.00, 3.40, 21, 30000, '40GP,40HQ,45HQ', TRUE),
    (2, 'ZIM', '以星快船', 'ZIM', 'SEA_EXPRESS', '深圳盐田/上海', '洛杉矶(LAX),萨凡纳(SAV)', 14, 580.00, 2.90, 21, 30000, '20GP,40GP,40HQ,45HQ', TRUE),
    (3, 'YANTIAN', '盐田普船', 'COSCO/EMC', 'SEA_STANDARD', '深圳盐田', '洛杉矶(LAX),长滩(LGB),纽约(NYC)', 25, 380.00, 1.90, 21, 30000, '20GP,40GP,40HQ', TRUE)
ON CONFLICT (channel_code) DO NOTHING;

-- 示例客户数据
INSERT INTO customer (customer_id, customer_no, company_name, contact_name, contact_phone, email, status)
VALUES
    (1, 'C20240001', '深圳跨境电商有限公司', '张三', '13800138001', 'zhangsan@example.com', 'ACTIVE'),
    (2, 'C20240002', '广州贸易有限公司', '李四', '13800138002', 'lisi@example.com', 'ACTIVE'),
    (3, 'C20240003', '杭州电子科技有限公司', '王五', '13800138003', 'wangwu@example.com', 'ACTIVE')
ON CONFLICT (customer_no) DO NOTHING;

-- 示例客户地址数据
INSERT INTO customer_address (address_id, customer_id, address_type, address_name, recipient_name, company_name, address_line1, address_line2, city, state, zip_code, country_code, phone, is_default)
VALUES
    (1, 1, 'COMMERCIAL', '洛杉矶办公室', 'John Zhang', 'SZ Cross-border E-commerce Inc.', '1234 Commerce Dr', 'Suite 100', 'Los Angeles', 'CA', '90001', 'US', '+1-310-555-1234', TRUE),
    (2, 1, 'RESIDENTIAL', '洛杉矶住宅', 'John Zhang', NULL, '5678 Residential Ave', 'Apt 2B', 'Los Angeles', 'CA', '90002', 'US', '+1-310-555-5678', TRUE),
    (3, 2, 'COMMERCIAL', '纽约办公室', 'Mike Li', 'GZ Trading Inc.', '9012 Business Blvd', NULL, 'New York', 'NY', '10001', 'US', '+1-212-555-9012', TRUE)
ON CONFLICT (address_id) DO NOTHING;

-- 示例员工数据
INSERT INTO employee (employee_id, employee_no, name, role, department, phone, email, status, remark)
VALUES
    (1, 'E20240001', '陈小明', 'customer_service', 'customer_service_dept', '13900139001', 'chenxm@example.com', 'active', '资深客服'),
    (2, 'E20240002', '林小红', 'customer_service', 'customer_service_dept', '13900139002', 'linxh@example.com', 'active', '客服组长'),
    (3, 'E20240003', '王大力', 'operator', 'operations_dept', '13900139003', 'wangdl@example.com', 'active', '操作主管'),
    (4, 'E20240004', '李小华', 'operator', 'operations_dept', '13900139004', 'lixh@example.com', 'active', '操作员'),
    (5, 'E20240005', '张财务', 'finance', 'finance_dept', '13900139005', 'zhangcw@example.com', 'active', '财务主管'),
    (6, 'E20240006', '刘会计', 'finance', 'finance_dept', '13900139006', 'liukj@example.com', 'active', '会计'),
    (7, 'E20240007', '赵仓管', 'warehouse_staff', 'warehouse_dept', '13900139007', 'zhaocg@example.com', 'active', '仓库主管'),
    (8, 'E20240008', '钱仓员', 'warehouse_staff', 'warehouse_dept', '13900139008', 'qiancy@example.com', 'active', '仓库员工'),
    (9, 'E20240009', '孙经理', 'manager', 'management', '13900139009', 'sunjl@example.com', 'active', '运营经理'),
    (10, 'E20240010', '周总监', 'manager', 'management', '13900139010', 'zhouzj@example.com', 'active', '物流总监')
ON CONFLICT (employee_no) DO NOTHING;
