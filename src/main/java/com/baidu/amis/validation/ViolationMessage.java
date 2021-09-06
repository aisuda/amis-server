package com.baidu.amis.validation;

/**
 * 默认验证信息，拷贝自 amis 的 locale/zh-CN.ts
 */
public class ViolationMessage {
    static final String equals = "输入的数据与 $1 不一致";
    static final String equalsField = "输入的数据与 $1 值不一致";
    static final String gt = "请输入大于 $1 的值";
    static final String isAlpha = "请输入字母";
    static final String isAlphanumeric = "请输入字母或者数字";
    static final String isEmail = "Email 格式不正确";
    static final String isFloat = "请输入浮点型数值";
    static final String isId = "请输入合法的身份证号";
    static final String isInt = "请输入整型数字";
    static final String isJson = "JSON 格式不正确";
    static final String isLength = "请输入长度为 $1 的内容";
    static final String isNumeric = "请输入数字";
    static final String isPhoneNumber = "请输入合法的手机号码";
    static final String isRequired = "这是必填项";
    static final String isTelNumber = "请输入合法的电话号码";
    static final String isUrl = "URL 格式不正确";
    static final String isUrlPath = "只能输入字母、数字、`-` 和 `_`.";
    static final String isWords = "请输入单词";
    static final String isZipcode = "请输入合法的邮编地址";
    static final String isExisty = "不存在这个值";
    static final String lt = "请输入小于 $1 的值";
    static final String matchRegexp = "格式不正确, 请输入符合规则为 $1 的内容";
    static final String maximum = "当前输入值超出最大值 $1";
    static final String maxLength = "请控制内容长度, 不要输入 $1 个以上字符";
    static final String minimum = "当前输入值低于最小值 $1";
    static final String minLength = "请输入更多的内容，至少输入 $1 个字符";
    static final String notEmptyString = "请不要全输入空白字符";
}
