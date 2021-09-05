package com.baidu.amis.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals


internal class JSONHelperTest {
    // 测试 json 查找功能
    @Test
    fun testJSONFind() {
        val mapper = ObjectMapper()
        val amisSchema: JsonNode = mapper.readTree(
            """
{
  "type": "page",
  "body": [{
    "type": "form",
    "name": "myForm",
    "api": "https://3xsw4ap8wah59.cfc-execute.bj.baidubce.com/api/amis-mock/mock2/form/saveForm",
    "body": [
      {
        "type": "input-text",
        "name": "name",
        "label": "姓名："
      },
      {
        "name": "email",
        "type": "input-email",
        "label": "邮箱："
      }
    ]
  }]
}
        """.trimIndent()
        )
        val amisNode = JSONHelper.findObject(
            amisSchema
        ) { key, node, parentNode ->
            if (key != null && key.equals("name")) {
                val type: String = parentNode.get("type").asText()
                if (type == "form") {
                    if (node.asText().equals("myForm")) {
                        println("find")
                        return@findObject true
                    }
                }
            }
            return@findObject false
        }
        assertEquals(amisNode.get("type").asText(), "form")
    }
}