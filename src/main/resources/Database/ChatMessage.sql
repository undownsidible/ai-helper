CREATE TABLE chat_message (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              session_id BIGINT NOT NULL,
                              role VARCHAR(20),
                              content TEXT,
                              create_time DATETIME
);