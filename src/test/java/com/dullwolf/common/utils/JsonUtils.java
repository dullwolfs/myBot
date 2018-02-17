package com.dullwolf.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Set;

public class JsonUtils {

    /**
     * 解析json字符串 转换指定的对象
     * @param json json字符串
     * @param clazz 对象类型
     * @param <T> 泛型
     * @return
     */
    public static <T> T getObjectByJson(String json, Class<T> clazz){
        return JSON.parseObject(json, clazz);
    }

    /**
     * 解析json字符串 转换指定的列表对象
     * @param json json字符串
     * @param clazz 列表对象
     * @param <T> 泛型
     * @return
     */
    public static <T> List<T> getArrayByJson(String json, Class<T> clazz){
        return JSON.parseArray(json, clazz);
    }

    /**
     * 解析对象转换为json字符串
     * @param obj 对象
     * @return json字符串
     */
    public static String getJsonString(Object obj){
        return JSON.toJSONString(obj);
    }

    /**
     * json字符串中下划线转为驼峰
     * @param json json字符串
     * @return 驼峰对象
     */
    public static Object convert(String json) {
        Object obj = JSON.parse(json);
        convert(obj);
        return obj;
    }

    private static void convert(Object json) {
        if (json instanceof JSONArray) {
            JSONArray arr = (JSONArray) json;
            for (Object obj : arr) {
                convert(obj);
            }
        } else if (json instanceof JSONObject) {
            JSONObject jo = (JSONObject) json;
            Set<String> keys = jo.keySet();
            String[] array = keys.toArray(new String[keys.size()]);
            for (String key : array) {
                Object value = jo.get(key);
                String[] key_strs = key.split("_");
                if (key_strs.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < key_strs.length; i++) {
                        String ks = key_strs[i];
                        if (!"".equals(ks)) {
                            if (i == 0) {
                                sb.append(ks);
                            } else {
                                int c = ks.charAt(0);
                                if (c >= 97 && c <= 122) {
                                    int v = c - 32;
                                    sb.append((char) v);
                                    if (ks.length() > 1) {
                                        sb.append(ks.substring(1));
                                    }
                                } else {
                                    sb.append(ks);
                                }
                            }
                        }
                    }
                    jo.remove(key);
                    jo.put(sb.toString(), value);
                }
                convert(value);
            }
        }
    }
}
