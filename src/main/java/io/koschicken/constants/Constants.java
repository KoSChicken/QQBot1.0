package io.koschicken.constants;

import io.koschicken.bean.CommonConfig;

public class Constants {

    public static final String CONFIG_DIR = "./config";
    public static final String CQ_AT = "[CQ:at,qq=";
    public static CommonConfig COMMON_CONFIG;

    private Constants() {
    }

    public interface cqType {
        String IMAGE = "image";
        String VOICE = "voice";
    }

    public interface cqPrefix {
        String FILE = "file=";
    }
}
