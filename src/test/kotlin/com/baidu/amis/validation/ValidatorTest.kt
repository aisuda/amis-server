package com.baidu.amis.validation

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ValidatorTest {
    @Test
    fun testSingleValidator() {
        val result1 = Validator.validate(
            """
            {
              "type": "page",
              "body": {
                "type": "form",
                "name": "myForm",
                "api": "/api/mock2/form/saveForm",
                "body": [
                  {
                    "type": "input-text",
                    "label": "文本",
                    "name": "text",
                    "validations": {
                      "isNumeric": true
                    },
                    "description": "请输入数字类型文本"
                  }
                ]
              }
            }
        """, "myForm", """
            {
                "text": "a"
            }
        """.trimIndent()
        )

        assertEquals(result1[0].message, ViolationMessage.isNumeric)
    }

    @Test
    fun testCustomMessage() {
        val result1 = Validator.validate(
            """
            {
              "type": "page",
              "body": {
                "type": "form",
                "name": "myForm",
                "api": "/api/mock2/form/saveForm",
                "body": [
                  {
                    "type": "input-text",
                    "label": "文本",
                    "name": "text",
                    "validations": {
                      "isNumeric": true
                    },
                    "validationErrors": {
                      "isNumeric": "同学，请输入数字哈"
                    },
                    "description": "请输入数字类型文本"
                  }
                ]
              }
            }
        """, "myForm", """
            {
                "text": "a"
            }
        """.trimIndent()
        )

        assertEquals(result1[0].message, "同学，请输入数字哈")
    }

}