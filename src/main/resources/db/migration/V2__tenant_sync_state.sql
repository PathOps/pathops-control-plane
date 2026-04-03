ALTER TABLE pathops_users
    ADD COLUMN keycloak_user_id VARCHAR(255);

ALTER TABLE tenants
    ADD COLUMN keycloak_sync_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN keycloak_sync_last_attempt_at TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN keycloak_sync_last_error VARCHAR(4000),
    ADD COLUMN keycloak_group_id VARCHAR(255),
    ADD COLUMN jenkins_sync_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN jenkins_sync_last_attempt_at TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN jenkins_sync_last_error VARCHAR(4000),
    ADD COLUMN jenkins_folder_path VARCHAR(1024);