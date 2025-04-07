CREATE TABLE IF NOT EXISTS token
(
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    refresh_token VARCHAR(255) NOT NULL,
    user_id       INT          NOT NULL,       -- Ссылается на пользователя
    created_at    TIMESTAMP    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user (id) -- Связь с таблицей пользователей
);


CREATE TABLE if not exists user
(
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(255) NOT NULL
);
