DROP TABLE IF EXISTS metadata;
DROP TABLE IF EXISTS exports;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS typeSignatures;

CREATE TABLE IF NOT EXISTS metadata (
    hash TEXT PRIMARY KEY,
    sizeBytes INTEGER,
    instructionCount INTEGER,
    processors TEXT,
    languages TEXT,
    inferredSourceLanguages TEXT
);

CREATE TABLE IF NOT EXISTS files (
    hash TEXT,
    absolutePath TEXT,
    collectionMethod TEXT,
    PRIMARY KEY (hash, absolutePath),
    FOREIGN KEY (hash)
        REFERENCES metadata (hash)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS typeSignatures (
    id INTEGER PRIMARY KEY,
    label TEXT,
    param TEXT,
    result TEXT,
    UNIQUE (param,result)
);

CREATE TABLE IF NOT EXISTS exports (
    hash TEXT,
    exportedAs TEXT,
    label TEXT,
    sigId INTEGER,
    PRIMARY KEY (hash, exportedAs),
    FOREIGN KEY (sigId)
        REFERENCES typeSignatures (id)
            ON DELETE NO ACTION
            ON UPDATE CASCADE,
    FOREIGN KEY (hash)
        REFERENCES metadata (hash)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);
