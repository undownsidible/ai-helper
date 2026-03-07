CREATE TABLE chat_session  (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              user_id BIGINT NOT NULL,
                              title VARCHAR(255),
                              create_time DATETIME,
                              update_time DATETIME
);