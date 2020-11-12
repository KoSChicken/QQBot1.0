package io.koschicken;

import io.koschicken.database.InitDatabase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

@SpringBootApplication
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        // 检查Bot配置，主要是用户名和密码
        checkBotConfig();
        // 初始化数据库文件
        InitDatabase initDatabase = new InitDatabase();
        initDatabase.initDB();
        InitConfig.initConfigs();
        // 启动
        SpringApplication.run(Application.class, args);
    }

    private static void checkBotConfig() {
        Properties properties = new Properties();
        try {
            InputStream inputStream = Application.class.getClassLoader().getResourceAsStream("application.properties");
            properties.load(inputStream);
            String botProperty = properties.getProperty("simbot.core.bots");
            if (StringUtils.isEmpty(botProperty)) {
                writeBotConfig(properties);
            }
            String bilibiliCookie = properties.getProperty("bilibili.cookie");
            if (StringUtils.isEmpty(bilibiliCookie)) {
                writeBilibiliCookie(properties);
            }
            String loliconApiKey = properties.getProperty("lolicon.apiKey");
            if (StringUtils.isEmpty(loliconApiKey)) {
                writeLoliconApiKey(properties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeBotConfig(Properties properties) {
        Scanner scanner = new Scanner(System.in);
        LOGGER.info("未检测到账户文件，请输入所要登陆的 qq 账号");
        LOGGER.info("qq号:  ");
        String code = scanner.next();
        LOGGER.info("密码:  ");
        String path = scanner.next();
        URL url = Application.class.getClassLoader().getResource("application.properties");
        try (OutputStream fos = new FileOutputStream(Objects.requireNonNull(url).getFile())){
            properties.setProperty("simbot.core.bots", code + ":" + path);
            properties.store(fos, "bot的账号和密码");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeBilibiliCookie(Properties properties) {
        Scanner scanner = new Scanner(System.in);
        LOGGER.info("未检测到 B 站 Cookie，会导致 B 站 API 无法调用，请填写（Enter 键跳过）：");
        String cookie = scanner.nextLine();
        URL url = Application.class.getClassLoader().getResource("application.properties");
        try (OutputStream fos = new FileOutputStream(Objects.requireNonNull(url).getFile())){
            properties.setProperty("bilibili.cookie", cookie);
            properties.store(fos, "B 站 Cookie，用于调用 B 站 API");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLoliconApiKey(Properties properties) {
        Scanner scanner = new Scanner(System.in);
        LOGGER.info("未检测到 LoliconAPIKey，会导致 API 无法调用，请填写（Enter 键跳过）：");
        String apiKey = scanner.nextLine();
        URL url = Application.class.getClassLoader().getResource("application.properties");
        try (OutputStream fos = new FileOutputStream(Objects.requireNonNull(url).getFile())){
            properties.setProperty("lolicon.apiKey", apiKey);
            properties.store(fos, "Lolicon 的 APIKey，用于调用 setuAPI");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
