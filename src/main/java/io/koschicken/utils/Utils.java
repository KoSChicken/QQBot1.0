package io.koschicken.utils;

public class Utils {

    private Utils() {
    }

    public static String dealCard(String card) {
        return card.replace("怪物猎人辱华", "屏蔽字");
    }
}
