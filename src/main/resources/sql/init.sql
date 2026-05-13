-- 1. investigations
IF OBJECT_ID('investigations', 'U') IS NULL
BEGIN
    CREATE TABLE investigations (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(255) NOT NULL,
        description NVARCHAR(MAX),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );
END;

-- 2. entities
IF OBJECT_ID('entities', 'U') IS NULL
BEGIN
    CREATE TABLE entities (
        id INT IDENTITY(1,1) PRIMARY KEY,
        investigation_id INT NOT NULL FOREIGN KEY REFERENCES investigations(id),
        type VARCHAR(50) NOT NULL,
        value NVARCHAR(MAX) NOT NULL,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );
END;

-- 3. entity_properties
IF OBJECT_ID('entity_properties', 'U') IS NULL
BEGIN
    CREATE TABLE entity_properties (
        entity_id INT NOT NULL FOREIGN KEY REFERENCES entities(id) ON DELETE CASCADE,
        property_key VARCHAR(100) NOT NULL,
        property_value NVARCHAR(MAX),
        PRIMARY KEY (entity_id, property_key)
    );
END;

-- 4. relationships
IF OBJECT_ID('relationships', 'U') IS NULL
BEGIN
    CREATE TABLE relationships (
        id INT IDENTITY(1,1) PRIMARY KEY,
        investigation_id INT NOT NULL FOREIGN KEY REFERENCES investigations(id),
        source_id INT NOT NULL FOREIGN KEY REFERENCES entities(id),
        target_id INT NOT NULL FOREIGN KEY REFERENCES entities(id),
        label VARCHAR(100) NOT NULL,
        created_at DATETIME DEFAULT GETDATE()
    );
END;

-- 5. relationship_properties
IF OBJECT_ID('relationship_properties', 'U') IS NULL
BEGIN
    CREATE TABLE relationship_properties (
        relationship_id INT NOT NULL FOREIGN KEY REFERENCES relationships(id) ON DELETE CASCADE,
        property_key VARCHAR(100) NOT NULL,
        property_value NVARCHAR(MAX),
        PRIMARY KEY (relationship_id, property_key)
    );
END;

-- 6. transform_history
IF OBJECT_ID('transform_history', 'U') IS NULL
BEGIN
    CREATE TABLE transform_history (
        id INT IDENTITY(1,1) PRIMARY KEY,
        investigation_id INT NOT NULL FOREIGN KEY REFERENCES investigations(id),
        entity_id INT NOT NULL FOREIGN KEY REFERENCES entities(id),
        transform_name VARCHAR(255) NOT NULL,
        status VARCHAR(50) NOT NULL,
        run_at DATETIME DEFAULT GETDATE()
    );
END;
