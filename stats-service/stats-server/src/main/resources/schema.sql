CREATE TABLE IF NOT EXISTS stats
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    app_name VARCHAR(500) NOT NULL,
    uri      VARCHAR(1500) NOT NULL,
    user_ip  VARCHAR(200)  NOT NULL,
    created  TIMESTAMP    NOT NULL
);