package io.koschicken.utils;

import com.alibaba.fastjson.JSON;
import io.koschicken.bean.GroupPower;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static io.koschicken.constants.Constants.CONFIG_DIR;

public class JSONUtils {

    private JSONUtils(){
    }

    public static synchronized void setJson(Map<String, GroupPower> map) {
        String jsonObject = JSON.toJSONString(map);
        try {
            File file = new File(CONFIG_DIR + "/config.txt");
            if (!file.exists() || !file.isFile()) {
                FileUtils.touch(file);
            }
            FileUtils.write(file, jsonObject, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
