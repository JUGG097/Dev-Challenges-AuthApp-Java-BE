CREATE SEQUENCE  IF NOT EXISTS hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE refresh_tokens (
   id BIGINT NOT NULL,
   user_id BIGINT,
   token VARCHAR(255) NOT NULL,
   expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
   CONSTRAINT pk_refreshtokens PRIMARY KEY (id)
);

ALTER TABLE refresh_tokens ADD CONSTRAINT uc_refreshtokens_token UNIQUE (token);

ALTER TABLE refresh_tokens ADD CONSTRAINT FK_REFRESHTOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);