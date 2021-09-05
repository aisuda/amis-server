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

    @Test
    fun testRequireOn() {
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
                    "requireOn": "this.a == 1"
                  }
                ]
              }
            }
        """, "myForm", """
            {
                "a": 1
            }
        """.trimIndent()
        )

        assertEquals(result1[0].message, "这是必填项")
    }

    @Test
    fun testHiddenOn() {
        val formSchema = """
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
                    "hiddenOn": "this.a == 1"
                  }
                ]
              }
            }
        """;
        val result1 = Validator.validate(
            formSchema, "myForm", """
            {
                "a": 1
            }
        """.trimIndent()
        )

        assertEquals(result1.size, 0)

        val result2 = Validator.validate(
            formSchema, "myForm", """
        """.trimIndent()
        )

        assertEquals(result2[0].message, "请输入数字")
    }

    @Test
    fun testFormRules() {
        val formSchema = """
            {
              "type": "page",
              "body": {
                "type": "form",
                "name": "myForm",
                "api": "/api/form/saveForm",
                "rules": [
                  {
                    "rule": "!(data.a && data.b)",
                    "message": "a 和 b 不能同时有值"
                  }
                ],
                "body": [
                  {
                    "type": "input-text",
                    "name": "a",
                    "label": "A"
                  },
                  {
                    "type": "input-text",
                    "name": "b",
                    "label": "B"
                  }
                ]
              }
            }
        """.trimIndent()
        val result1 = Validator.validate(
            formSchema, "myForm", """
            {
                "a": "a",
                "b": "b"
            }
        """.trimIndent()
        )

        assertEquals(result1[0].message, "a 和 b 不能同时有值")

        val result2 = Validator.validate(
            formSchema, "myForm", """
            {
                "a": "a"
            }
        """.trimIndent()
        )

        assertEquals(result2.size, 0)
    }
}