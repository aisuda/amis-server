package com.baidu.amis.util;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface JSONFind {
    /**
     * 在 JSON 查找的 lambda
     * @param node 当前节点
     * @param parentNode 父级节点
     * @return 是否找到
     */
    boolean find(String key, JsonNode node, JsonNode parentNode);
}
