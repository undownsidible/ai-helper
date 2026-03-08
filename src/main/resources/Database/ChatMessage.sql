CREATE TABLE chat_message (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              session_id BIGINT NOT NULL,
                              role VARCHAR(20) NOT NULL,
                              content TEXT NOT NULL,
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);