package com.baidu.amis.validation

import org.junit.jupiter.api.Test
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

        // 1.1.x 版本的格式
        val result2 = Validator.validate(
            """
            {
              "type": "page",
              "body": {
                "type": "form",
                "name": "myForm",
                "api": "/api/mock2/form/saveForm",
                "controls": [
                  {
                    "type": "text",
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
        assertEquals(result2[0].message, ViolationMessage.isNumeric)

        // 都没有的话就不检查
        val result3 = Validator.validate(
            """
            {
              "type": "page",
              "body": {
                "type": "form",
                "name": "myForm",
                "api": "/api/mock2/form/saveForm"
              }
            }
        """, "myForm", """
            {
                "text": "a"
            }
        """.trimIndent()
        )
        assertEquals(result3.size, 0)

        // 只有一个表单项的情况
        val result4 = Validator.validate(
            """
            {
              "type": "page",
              "body": {
                "type": "form",
                "name": "myForm",
                "api": "/api/mock2/form/saveForm",
                "body": {
                    "type": "input-text",
                    "label": "文本",
                    "name": "text",
                    "validations": {
                      "isNumeric": true
                    },
                    "description": "请输入数字类型文本"
                }
              }
            }
        """, "myForm", """
            {
                "text": "a"
            }
        """.trimIndent()
        )

        assertEquals(result4[0].message, ViolationMessage.isNumeric)
    }

    @Test
    fun testRequire() {
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
                    "required": true,
                    "description": "请输入数字类型文本"
                  }
                ]
              }
            }
        """, "myForm", """
            {
                "b": "a"
            }
        """.trimIndent()
        )
        assertEquals(result1[0].message, ViolationMessage.isRequired)

        val result2 = Validator.validate(
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
                    "required": true,
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
                "b": "a"
            }
        """.trimIndent()
        )

        assertEquals(result2.size, 2)
    }

    @Test
    fun testStringValidator() {
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
                    "validations": "isNumeric,maximum:10",
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

        val result2 = Validator.validate(
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
                    "validations": "isNumeric,maximum:10",
                    "description": "请输入数字类型文本"
                  }
                ]
              }
            }
        """, "myForm", """
            {
                "text": 12
            }
        """.trimIndent()
        )
        assertEquals(result2[0].message, "当前输入值超出最大值 10")
    }

    @Test
    fun testHidden() {
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
                    "hidden": true,
                    "validations": "isNumeric",
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
        assertEquals(result1.size, 0)

        val result2 = Validator.validate(
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
                    "hidden": false,
                    "validations": "isNumeric",
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
        assertEquals(result2.size, 1)

        val result3 = Validator.validate(
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
                    "visible": false,
                    "validations": "isNumeric",
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

        assertEquals(result3.size, 0)

        val result4 = Validator.validate(
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
                    "visible": true,
                    "validations": "isNumeric",
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

        assertEquals(result4.size, 1)
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
    fun testVisibleOn() {
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
                    "visibleOn": "this.a == 1"
                  }
                ]
              }
            }
        """;
        val result1 = Validator.validate(
            formSchema, "myForm", """
            {
                "a": 2
            }
        """.trimIndent()
        )

        assertEquals(result1.size, 0)

        val result2 = Validator.validate(
            formSchema, "myForm", """
             {
                "a": 1
            }
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

    @Test
    fun testformItems() {
        val isNumeric = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isNumeric": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": "a"
            }
        """.trimIndent()
        )
        assertEquals(isNumeric[0].message, "请输入数字")

        val isExisty = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isExisty": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "b": "a"
            }
        """.trimIndent()
        )
        assertEquals(isExisty[0].message, "不存在这个值")

        val isEmail = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isEmail": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": "a"
            }
        """.trimIndent()
        )
        assertEquals(isEmail[0].message, "Email 格式不正确")

        val isUrl = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isUrl": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": "a"
            }
        """.trimIndent()
        )
        assertEquals(isUrl[0].message, "URL 格式不正确")

        val isAlpha = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isAlpha": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1
            }
        """.trimIndent()
        )
        assertEquals(isAlpha[0].message, "请输入字母")

        val isAlphanumeric = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isAlphanumeric": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": "#$"
            }
        """.trimIndent()
        )
        assertEquals(isAlphanumeric[0].message, "请输入字母或者数字")

        val isInt = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isInt": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1.2
            }
        """.trimIndent()
        )
        assertEquals(isInt[0].message, "请输入整型数字")

        val isFloat = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isFloat": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": "ff"
            }
        """.trimIndent()
        )
        assertEquals(isFloat[0].message, "请输入浮点型数值")

        val isWords = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isWords": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1
            }
        """.trimIndent()
        )
        assertEquals(isWords[0].message, "请输入单词")

        val isUrlPath = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isUrlPath": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": " d"
            }
        """.trimIndent()
        )
        assertEquals(isUrlPath[0].message, "只能输入字母、数字、`-` 和 `_`.")

        val matchRegexp = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "matchRegexp": "a"
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1
            }
        """.trimIndent()
        )
        assertEquals(matchRegexp[0].message, "格式不正确, 请输入符合规则为 a 的内容")

        val minLength = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "minLength": 3
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": "dd"
            }
        """.trimIndent()
        )
        assertEquals(minLength[0].message, "请输入更多的内容，至少输入 3 个字符")

        val maxLength = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "maxLength": 1
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": "dd"
            }
        """.trimIndent()
        )
        assertEquals(maxLength[0].message, "请控制内容长度, 不要输入 1 个以上字符")

        val maximum = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "maximum": 20
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 21
            }
        """.trimIndent()
        )

        assertEquals(maximum[0].message, "当前输入值超出最大值 20")

        val lt = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "lt": 20
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 21
            }
        """.trimIndent()
        )

        assertEquals(lt[0].message, "请输入小于 20 的值")

        val minimum = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "minimum": 10
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 9
            }
        """.trimIndent()
        )
        assertEquals(minimum[0].message, "当前输入值低于最小值 10")

        val gt = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "gt": 10
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1
            }
        """.trimIndent()
        )
        assertEquals(gt[0].message, "请输入大于 10 的值")

        val isJson = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isJson": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": "a"
            }
        """.trimIndent()
        )
        assertEquals(isJson[0].message, "JSON 格式不正确")

        val isLength = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isLength": 2
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": "b"
            }
        """.trimIndent()
        )
        assertEquals(isLength[0].message, "请输入长度为 2 的内容")

        val notEmptyString = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "notEmptyString": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": ""
            }
        """.trimIndent()
        )
        assertEquals(notEmptyString[0].message, "请不要全输入空白字符")

        val equalsField = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "equalsField": "b"
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1,
              "b": 2
            }
        """.trimIndent()
        )
        assertEquals(equalsField[0].message, "输入的数据与 b 值不一致")

        val equals = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "equals": 2
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1
            }
        """.trimIndent()
        )
        assertEquals(equals[0].message, "输入的数据与 2 不一致")

        val isPhoneNumber = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isPhoneNumber": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1
            }
        """.trimIndent()
        )
        assertEquals(isPhoneNumber[0].message, "请输入合法的手机号码")


        val isTelNumber = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isTelNumber": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1
            }
        """.trimIndent()
        )
        assertEquals(isTelNumber[0].message, "请输入合法的电话号码")

        val isZipcode = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isZipcode": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1
            }
        """.trimIndent()
        )
        assertEquals(isZipcode[0].message, "请输入合法的邮编地址")

        val isId = Validator.validate(
            """
              {
                "type": "form",
                "body": {
                    "type": "input-text",
                    "name": "a",
                    "label": "A",
                    "validations": {
                      "isId": true
                    }
                }
              }
            """.trimIndent(), """
            {
              "a": 1
            }
        """.trimIndent()
        )
        assertEquals(isId[0].message, "请输入合法的身份证号")



    }
}