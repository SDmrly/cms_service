-- V1__init_schema.sql

-- Projects Table
CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- CMS Keys Table
CREATE TABLE cms_keys (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    key VARCHAR(255) NOT NULL,
    value_type VARCHAR(50) NOT NULL,
    default_value TEXT,
    category VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_cms_keys_project_key UNIQUE (project_id, key)
);

-- Translations Table
CREATE TABLE translations (
    id UUID PRIMARY KEY,
    cms_key_id UUID NOT NULL REFERENCES cms_keys(id) ON DELETE CASCADE,
    locale VARCHAR(50) NOT NULL,
    translation_value TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_translations_key_locale UNIQUE (cms_key_id, locale)
);

-- Indexes for performance
CREATE INDEX idx_projects_code ON projects(code);
CREATE INDEX idx_cms_keys_project_id ON cms_keys(project_id);
CREATE INDEX idx_cms_keys_category ON cms_keys(category);
CREATE INDEX idx_translations_cms_key_id ON translations(cms_key_id);
CREATE INDEX idx_translations_locale ON translations(locale);

-- Insert dummy data for testing (optional, but good for local dev)
-- Uncomment these if you want initial data
-- INSERT INTO projects (id, name, code, description, active, created_at)
-- VALUES ('123e4567-e89b-12d3-a456-426614174000', 'Demo Website', 'DEMO_WEB', 'Demo project for CMS', true, NOW());
