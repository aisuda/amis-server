package com.baidu.amis.validation;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * 实现 amis 中 src/utils/validations 里的校验方法
 * 但实现不完全一致，有些正则判断改成了 commons 里的方法来方便维护，可能会造成前后端校验不一致
 * 目前还不清楚 js 和 java 在正则上有哪些不一致，这会导致自定义正则可能会不一致
 */
public class ValidationFn {
    /**
     * 必填项是否有值
     */
    static boolean isRequired(JsonNode value) {
        if (value == null) {
            return false;
        }
        JsonNodeType nodeType = value.getNodeType();
        switch (nodeType) {
            case NULL:
            case MISSING:
                return false;
            case ARRAY:
                return !value.isEmpty();
            case STRING:
                return !value.asText().equals("");
        }
        return true;
    }

    /**
     * 是否存在这个值，只要不是 null 就是存在
     */
    static boolean isExisty(JsonNode value) {
        if (value == null) {
            return false;
        }
        JsonNodeType nodeType = value.getNodeType();
        switch (nodeType) {
            case NULL:
            case MISSING:
                return false;
        }
        return true;
    }

    /**
     * 判断字符串是否符合正则，主要是将检查的正则字符串转成正则对象后调用 {@link #matchRegexp(JsonNode value, Pattern pattern)}
     *
     * @param regex 正则字符串
     */
    static boolean matchRegexp(JsonNode value, String regex) {
        return matchRegexp(value, Pattern.compile(regex));
    }

    /**
     * 根据正则进行判断，大部分方法是基于这个
     *
     * @param pattern 正则
     */
    static boolean matchRegexp(JsonNode value, Pattern pattern) {
        if (value == null) {
            return false;
        }
        String text = value.asText();
        if (value.asText().equals("")) {
            return false;
        }
        return pattern.matcher(text).find();
    }

    /**
     * 是否是 undefined，它的值永远为 false，这是 amis 前端才需要的，转成 json 后就不会有 undefined 的数据
     *
     * @return 永远是 false
     */
    static boolean isUndefined(JsonNode value) {
        return false;
    }

    /**
     * 是否是空字符串
     */
    static boolean isEmptyString(JsonNode value) {
        return isString(value) && value.asText().equals("");
    }

    // 内部方法，主要是很多基于正则的判断是需要确保字符串的
    static private boolean isString(JsonNode value) {
        return value != null && value.getNodeType() == JsonNodeType.STRING;
    }

    /**
     * 是否是邮箱地址
     */
    static boolean isEmail(JsonNode value) {
        if (isString(value)) {
            return EmailValidator.getInstance().isValid(value.asText());
        }
        return false;
    }

    /**
     * 是否是网址
     */
    static boolean isUrl(JsonNode value) {
        if (isString(value)) {
            return UrlValidator.getInstance().isValid(value.asText());
        }
        return false;
    }

    /**
     * 是否是 true
     */
    static boolean isTrue(JsonNode value) {
        return value != null && value.getNodeType() == JsonNodeType.BOOLEAN && value.asBoolean();
    }

    /**
     * 是否是 false
     */
    static boolean isFalse(JsonNode value) {
        return value != null && value.getNodeType() == JsonNodeType.BOOLEAN && !value.asBoolean();
    }

    /**
     * 是否是数字
     */
    static boolean isNumeric(JsonNode value) {
        // 目前看来这里不需要判断类型，即便是数字，asText() 也会转成对应的字符串
        return value != null && NumberUtils.isCreatable(value.asText());
    }

    /**
     * 是否只有英文字母
     */
    static boolean isAlpha(JsonNode value) {
        return value != null && !value.asText().isEmpty() && value.asText().chars().allMatch(Character::isLetter);
    }

    /**
     * 是否只有英文字母和数字
     */
    static boolean isAlphanumeric(JsonNode value) {
        return value != null && !value.asText().isEmpty() && value.asText().chars()
                .allMatch(Character::isLetterOrDigit);
    }

    /**
     * 是否是整数
     */
    static boolean isInt(JsonNode value) {
        return matchRegexp(value, "^(?:[-+]?(?:0|[1-9]\\d*))$");
    }

    /**
     * 是否是浮点数
     */
    static boolean isFloat(JsonNode value) {
        return matchRegexp(value, "^(?:[-+]?(?:\\d+))?(?:\\.\\d*)?(?:[eE][\\+\\-]?(?:\\d+))?$");
    }

    /**
     * 是否是字母或空格
     */
    static boolean isWords(JsonNode value) {
        Pattern pattern = Pattern.compile("^[A-Z\\s]+$", Pattern.CASE_INSENSITIVE);
        return matchRegexp(value, pattern);
    }

    /**
     * 是否是字母及带重音的字母
     */
    static boolean isSpecialWords(JsonNode value) {
        Pattern pattern = Pattern.compile("^[A-Z\\s\\u00C0-\\u017F]+$", Pattern.CASE_INSENSITIVE);
        return matchRegexp(value, pattern);
    }

    /**
     * 是否长度正好等于设定值
     */
    static boolean isLength(JsonNode value, int length) {
        return value != null && value.asText().length() == length;
    }

    /**
     * 是否值等于某个值
     */
    static boolean equals(JsonNode value, JsonNode otherValue) {
        return value != null && value.equals(otherValue);
    }

    /**
     * 是否值等于另一个字段值，注意因为这个功能只能拿到表单数据，所以无法支持 amis 的数据域查找
     */
    static boolean equalsField(JsonNode value, String fieldName) {
        JsonNode otherField = value.get(fieldName);
        return value.equals(otherField);
    }

    /**
     * 最大长度
     */
    static boolean maxLength(JsonNode value, int length) {
        return value != null && value.asText().length() <= length;
    }

    /**
     * 最小长度
     */
    static boolean minLength(JsonNode value, int length) {
        return value != null && value.asText().length() > length;
    }

    /**
     * 是否是 url 路径
     */
    static boolean isUrlPath(JsonNode value) {
        Pattern pattern = Pattern.compile("^[a-z0-9_\\\\-]+$", Pattern.CASE_INSENSITIVE);
        return matchRegexp(value, pattern);
    }

    /**
     * 最大值
     */
    static boolean maximum(JsonNode value, Double compare) {
        return value != null && Double.compare(value.asDouble(), compare) < 0;
    }

    /**
     * 小于等于
     */
    static boolean lt(JsonNode value, Double compare) {
        return value != null && Double.compare(value.asDouble(), compare) <= 0;
    }

    /**
     * 最小值
     */
    static boolean minimum(JsonNode value, Double compare) {
        return value != null && Double.compare(value.asDouble(), compare) > 0;
    }

    /**
     * 大于等于
     */
    static boolean gt(JsonNode value, Double compare) {
        return value != null && Double.compare(value.asDouble(), compare) >= 0;
    }

    /**
     * 字符串是否是 json 格式
     */
    static boolean isJson(JsonNode value) {
        if (value == null) {
            return false;
        }
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(value.asText());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 字符串是否是手机号，这里只考虑国内的情况
     */
    static boolean isPhoneNumber(JsonNode value) {
        return matchRegexp(value, "^[1]([3-9])[0-9]{9}$");
    }

    /**
     * 字符串是否是电话号码，这里只考虑国内的情况
     */
    static boolean isTelNumber(JsonNode value) {
        return matchRegexp(value, "^(\\(\\d{3,4}\\)|\\d{3,4}-|\\s)?\\d{7,14}$");
    }

    /**
     * 是否是邮编，只考虑国内的情况
     */
    static boolean isZipcode(JsonNode value) {
        return matchRegexp(value, "^[1-9]{1}(\\d+){5}$");
    }

    /**
     * 是否是身份证号，这里没做有效性验证
     */
    static boolean isId(JsonNode value) {
        return matchRegexp(value, "(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])"
                + "|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)"
                + "\\d{3}$)");
    }

    /**
     * 不是空白字符串
     */
    static boolean notEmptyString(JsonNode value) {
        return value != null && StringUtils.isBlank(value.asText());
    }

    /**
     * 是否匹配某个正则，预留了多个
     */
    static boolean matchRegexp1(JsonNode value, String regex) {
        return matchRegexp(value, regex);
    }

    static boolean matchRegexp2(JsonNode value, String regex) {
        return matchRegexp(value, regex);
    }

    static boolean matchRegexp3(JsonNode value, String regex) {
        return matchRegexp(value, regex);
    }

    static boolean matchRegexp4(JsonNode value, String regex) {
        return matchRegexp(value, regex);
    }

    static boolean matchRegexp5(JsonNode value, String regex) {
        return matchRegexp(value, regex);
    }

    static boolean matchRegexp6(JsonNode value, String regex) {
        return matchRegexp(value, regex);
    }

    static boolean matchRegexp7(JsonNode value, String regex) {
        return matchRegexp(value, regex);
    }

    static boolean matchRegexp8(JsonNode value, String regex) {
        return matchRegexp(value, regex);
    }

    static boolean matchRegexp9(JsonNode value, String regex) {
        return matchRegexp(value, regex);
    }

}
