package io.koschicken.config;

import com.forte.qqrobot.SimpleRobotApplication;
import com.forte.qqrobot.depend.DependGetter;
import com.forte.qqrobot.sender.MsgSender;
import com.forte.qqrobot.utils.CQCodeUtil;
import com.simbot.component.mirai.MiraiApp;
import com.simbot.component.mirai.MiraiConfiguration;
import com.simplerobot.core.springboot.configuration.SpringBootDependGetter;
import io.koschicken.InitConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Scanner;

@SimpleRobotApplication(resources = "./conf.properties")
public class MiraiSpringBootBeanFactoryApp implements MiraiApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(MiraiSpringBootBeanFactoryApp.class);
    private final DependGetter dependGetter;

    public MiraiSpringBootBeanFactoryApp(SpringBootDependGetter dependGetter) {
        this.dependGetter = dependGetter;
    }

    @Override
    public void before(MiraiConfiguration configuration) {
        // 整合Spring的DependGetter
        configuration.setDependGetter(dependGetter);
        // 检查Bot配置，主要是用户名和密码
        checkBotConfig(configuration);
    }

    private static void checkBotConfig(MiraiConfiguration configuration) {
        Properties properties = new Properties();
        writeBotConfig(properties, configuration);
    }

    private static void writeBotConfig(Properties properties, MiraiConfiguration configuration) {
        String configDir = null;
        try {
            configDir = createConfigDir();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(configDir + "/qq.txt");
        if (!file.exists()) {
            try (OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                readQQAndPassword(properties, op, configuration);
                Files.createFile(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                properties.load(in);
                String code = properties.getProperty("qq账号");
                String path = properties.getProperty("密码");
                if (code == null || path == null) {
                    try (OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                        readQQAndPassword(properties, op, configuration);
                    }
                } else {
                    configuration.registerBot(code, path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readQQAndPassword(Properties properties, OutputStreamWriter op,
                                          MiraiConfiguration configuration) throws IOException {
        Scanner scanner = new Scanner(System.in);
        LOGGER.info("请输入所要登陆的qq账号");
        LOGGER.info("qq号:  ");
        String code = scanner.next();
        LOGGER.info("密码:  ");
        String path = scanner.next();
        properties.setProperty("qq账号", code);
        properties.setProperty("密码", path);
        properties.store(op, "config");
        configuration.registerBot(code, path);
    }

    @Override
    public void after(CQCodeUtil cqCodeUtil, MsgSender sender) {
        InitConfig.initConfigs();
    }

    private static String createConfigDir() throws IOException {
        File file = new File("config");
        if (!file.exists()) {
            FileUtils.forceMkdir(file);
        }
        return file.getAbsolutePath();
    }
}
