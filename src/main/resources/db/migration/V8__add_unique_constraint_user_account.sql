ALTER TABLE user_account
ADD CONSTRAINT email_tenantId_unique_constraint UNIQUE (email, tenant_id);