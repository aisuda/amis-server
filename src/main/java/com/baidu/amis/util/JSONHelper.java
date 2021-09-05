package com.baidu.amis.util;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * amis 中有大量 JSON 操作，这个类主要用于
 */
public class JSONHelper {
    /**
     * 在 json 节点中查找对象
     * @param node JSON 节点
     * @param finder 查找 lambda
     * @return
     */
    public static JsonNode findObject(JsonNode node, JSONFind finder) {
        return findObject(node, finder, null , "", 0);
    }

    /**
     * 将字符串转成 JSON 节点
     * @param str
     * @return
     * @throws JsonProcessingException
     */
    public static JsonNode toJSONNode(String str) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        JsonNode amisSchema = mapper.readTree(str);
        return amisSchema;
    }

    // 查找 JSON 对象的内部实现
    private static JsonNode findObject(JsonNode node, JSONFind finder, JsonNode parent, String key, int index) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                JsonNode result = findObject(entry.getValue(), finder, node, entry.getKey(), 0);
                if (result != null) {
                    return result;
                }
            }
        } else if (node.isArray()) {
            Iterator<JsonNode> it = node.iterator();
            int i = 0;
            while (it.hasNext()) {
                JsonNode result = findObject(it.next(), finder, node, "[]", i);
                if (result != null) {
                    return result;
                }
                i = i + 1;
            }
        } else if (node.isValueNode()) {
            if (parent != null) {
                if (parent.isObject()) {
                    if (finder.find(key, node, parent)) {
                        return parent;
                    }
                }
            }
        }
        return null;
    }
}
