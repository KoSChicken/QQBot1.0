package io.koschicken.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.Pixiv;
import org.apache.http.Consts;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.koschicken.constants.Constants.COMMON_CONFIG;

public class SetuUtils {
    private static final String YUBAN1073API = "http://api.yuban10703.xyz:2333/setu_v3";
    private static final String LOLICONAPI = "https://api.lolicon.app/setu/";
    private static final Map<Integer, String> CODE_MAP;
    private static final Logger LOGGER = LoggerFactory.getLogger(SetuUtils.class);

    static {
        CODE_MAP = new HashMap<>();
        CODE_MAP.put(-1, "接口发生内部错误");
        CODE_MAP.put(0, "成功");
        CODE_MAP.put(401, "APIKEY 不存在或被封禁");
        CODE_MAP.put(403, "由于不规范的操作而被拒绝调用");
        CODE_MAP.put(404, "找不到符合关键字的色图");
        CODE_MAP.put(429, "达到调用额度限制");
    }

    private SetuUtils() {

    }

    public static List<Pixiv> getSetu(String tag, int num, Boolean r18) throws IOException {
        List<Pixiv> pixivList = fetchFromLolicon(num, tag, r18);
        if ("0".equals(pixivList.get(0).getCode())) {
            // 请求lolicon的API成功则返回
            return pixivList;
        } else {
            // 否则请求yuban
            return fetchFromYuban1073(num, tag, r18);
        }
    }

    private static List<Pixiv> fetchFromYuban1073(int num, String tag, Boolean r18) throws IOException {
        List<Pixiv> pics = new ArrayList<>();
        String api = YUBAN1073API + "?num=" + num;
        int type;
        if (Boolean.TRUE.equals(r18)) {
            type = 2;
        } else {
            type = 3;
        }
        api += "&type=" + type;
        if (!StringUtils.isEmpty(tag)) {
            api += "&tag=" + tag;
        }
        LOGGER.info("这次请求的地址为： {}", api);
        ResponseHandler<String> myHandler = response -> EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        String response = Request.Get(api).execute().handleResponse(myHandler);
        JSONObject jsonObject = JSON.parseObject(response);
        if ("200".equals(jsonObject.getString("code"))) { // 请求成功
            JSONArray dataArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < dataArray.size(); i++) {
                Pixiv pixiv = new Pixiv();
                JSONObject data = dataArray.getJSONObject(i);
                fillPixiv(pixiv, data);
                pics.add(pixiv);
            }
        } else {
            Pixiv pixiv = new Pixiv();
            pixiv.setCode(jsonObject.getString("code"));
            pixiv.setMsg(jsonObject.getString("msg"));
            pics.add(pixiv);
        }
        return pics;
    }

    private static List<Pixiv> fetchFromLolicon(int num, String tag, Boolean r18) throws IOException {
        List<Pixiv> pics = new ArrayList<>();
        String loliconApi = LOLICONAPI + "?apikey=" + COMMON_CONFIG.getLoliconApiKey() + "&r18=2&size1200=true&num=" + num;
        if (!StringUtils.isEmpty(tag)) {
            loliconApi += "&keyword=" + tag;
        }
        if (r18 != null) {
            loliconApi += "&r18=" + (Boolean.TRUE.equals(r18) ? 1 : 2);
        }
        LOGGER.info("这次请求的Lolicon地址为： {}", loliconApi);
        ResponseHandler<String> myHandler = response -> EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        String response = Request.Get(loliconApi).execute().handleResponse(myHandler);
        JSONObject jsonObject = JSON.parseObject(response);
        Integer code = jsonObject.getInteger("code");
        if (code == 0) {
            JSONArray dataArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < dataArray.size(); i++) {
                Pixiv pixiv = new Pixiv();
                pixiv.setCode(code.toString());
                pixiv.setQuota(jsonObject.getInteger("quota"));
                JSONObject data = dataArray.getJSONObject(i);
                fillPixivLolicon(pixiv, data);
                pics.add(pixiv);
            }
        } else {
            Pixiv pixiv = new Pixiv();
            pixiv.setCode(code.toString());
            pixiv.setMsg(CODE_MAP.get(code));
            pics.add(pixiv);
        }
        return pics;
    }

    private static void fillPixiv(Pixiv pixiv, JSONObject data) {
        pixiv.setTitle(data.getString("title"));
        pixiv.setArtwork(data.getString("artwork"));
        pixiv.setAuthor(data.getString("author"));
        pixiv.setArtist(data.getString("artist"));
        pixiv.setTags(data.getString("tags").split(","));
        pixiv.setType(data.getString("type"));
        pixiv.setFileName(data.getString("filename"));
        pixiv.setOriginal(data.getString("original"));
        pixiv.setR18("porn".equals(data.getString("type")));
    }

    private static void fillPixivLolicon(Pixiv pixiv, JSONObject data) {
        pixiv.setTitle(data.getString("title"));
        pixiv.setArtwork(data.getString("pid"));
        pixiv.setAuthor(data.getString("author"));
        pixiv.setArtist(data.getString("uid"));
        pixiv.setTags(data.getString("tags").split(","));
        pixiv.setType(Boolean.TRUE.equals(data.getBoolean("r18")) ? "r18" : "normal");
        pixiv.setFileName(data.getString("url"));
        pixiv.setOriginal(data.getString("url"));
        pixiv.setR18(Boolean.parseBoolean(data.getString("r18")));
    }
}
