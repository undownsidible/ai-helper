CREATE TABLE schedule (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,

                          name VARCHAR(100) NOT NULL,
                          start_time DATETIME NOT NULL,
                          end_time DATETIME,

                          remark VARCHAR(500),

                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0未删除 1已删除',

                          create_time DATETIME,
                          update_time DATETIME
);

CREATE INDEX idx_user_time ON schedule(user_id, start_time);