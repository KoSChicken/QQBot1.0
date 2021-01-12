package io.koschicken;

import io.koschicken.database.InitDatabase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // 初始化数据库文件
        InitDatabase initDatabase = new InitDatabase();
        initDatabase.initDB();
        SpringApplication.run(Application.class, args);
    }
}
