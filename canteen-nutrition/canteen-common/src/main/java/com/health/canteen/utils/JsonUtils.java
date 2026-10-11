package com.health.canteen.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON序列化工具
 */
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("对象序列化为json失败", e);
        }
    }

    public static <T> T parseJson(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("json转对象失败", e);
        }
    }

    /**
     * 从模型返回文本中截取第一个JSON对象，兼容模型额外输出说明文字的情况
     */
    public static String extractJsonObject(String text) {
        if (text == null) {
            return null;
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return text.substring(start, end + 1);
    }

}
