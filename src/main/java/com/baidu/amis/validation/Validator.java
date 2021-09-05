package com.baidu.amis.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.script.SimpleBindings;

import com.baidu.amis.util.JSONHelper;
import com.baidu.amis.util.Script;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 用于校验数据是否符合 amis 表单 schema 中配置的表单校验规则
 */
public class Validator {

    /**
     * 根据解析的 JSON 节点来进行校验，需要注意第一个参数必须是表单对应的 amis 配置，如果没有解析出来推荐使用后面的方法
     *
     * @param form 相关表单的 amis JSON 配置
     * @param data 数据的 JSON
     * @return 违反规则的列表，如果列表为空意味着没有违反
     */
    public static List<ConstraintViolation> validate(JsonNode form, JsonNode data) {
        JsonNode body = form.get("body");
        // 兼容旧版的写法
        if (body == null) {
            body = form.get("controls");
        }
        ArrayList<ConstraintViolation> res = new ArrayList<ConstraintViolation>();

        if (body == null) {
            return res;
        }

        // 后面需要的数据类型
        SimpleBindings dataBindings = new SimpleBindings();
        if (data.isObject()) {
            // 用于内嵌的 data
            SimpleBindings dataInnerBindings = new SimpleBindings();
            Iterator<Map.Entry<String, JsonNode>> it = data.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                dataBindings.put(entry.getKey(), entry.getValue());
                dataInnerBindings.put(entry.getKey(), entry.getValue());
            }
            dataBindings.put("data", dataInnerBindings);

        }

        // 表单级别校验
        JsonNode rules = form.get("rules");
        if (rules != null && rules.isArray()) {
            for (JsonNode ruleProps : rules) {
                JsonNode rule = ruleProps.get("rule");
                JsonNode message = ruleProps.get("message");
                if (rule != null && message != null) {
                    boolean ruleValue = Script.eval(rule.asText(), dataBindings);
                    if (!ruleValue) {
                        res.add(new ConstraintViolation("", message.asText()));
                    }
                }
            }
        }

        // 只有一个表单项的情况
        if (body.isObject()) {
            return validateFormItem(body, data, dataBindings);
        } else if (body.isArray()) {
            for (JsonNode formItem : body) {
                res.addAll(validateFormItem(formItem, data, dataBindings));
            }
            return res;
        }

        return res;
    }

    // 就是多了自动解析 JSON
    public static List<ConstraintViolation> validate(String amisSchemaStr, String data)
            throws JsonProcessingException {
        return validate(JSONHelper.toJSONNode(amisSchemaStr), JSONHelper.toJSONNode(data));
    }

    /**
     * 根据 amis schema 和表单名进行自动校验
     * 和前面比就是多了自动转成 JSON 节点和查找对应的表单 schema
     *
     * @param amisSchemaStr amis schema 的字符串
     * @param formName      表单名
     * @param data          需要校验的数据
     * @return 违反规则的列表，如果列表为空意味着没有违反
     * @throws JsonProcessingException
     */
    public static List<ConstraintViolation> validate(String amisSchemaStr, String formName, String data)
            throws JsonProcessingException {
        return validate(amisSchemaStr, formName, JSONHelper.toJSONNode(data));
    }

    public static List<ConstraintViolation> validate(String amisSchemaStr, String formName, JsonNode data)
            throws JsonProcessingException {
        JsonNode amisSchema = JSONHelper.findObject(JSONHelper.toJSONNode(amisSchemaStr),
                (String key, JsonNode node, JsonNode parentNode) -> {
                    if (key != null && key.equals("name")) {
                        String type = parentNode.get("type").asText();
                        if (Objects.equals(type, "form")) {
                            return node.asText().equals(formName);
                        }
                    }
                    return false;
                }
        );
        return validate(amisSchema, data);
    }

    // 如果有用户自定义 message，就用那个
    private static ConstraintViolation genConstraintViolation(String name, JsonNode formItemSchema,
                                                              String violationName,
                                                              String defaultMessage) {
        String message = defaultMessage;
        JsonNode validationErrors = formItemSchema.get("validationErrors");
        if (validationErrors != null && validationErrors.isObject()) {
            JsonNode customValidationMessage = validationErrors.get(violationName);
            if (customValidationMessage != null && !customValidationMessage.asText().isEmpty()) {
                message = customValidationMessage.asText();
            }
        }

        return new ConstraintViolation(name, message);
    }

    /**
     * 验证单个表单项
     *
     * @param formItemSchema 表单项
     * @param data           数据
     * @return 如果数组非空就代表验证不通过
     */
    public static List<ConstraintViolation> validateFormItem(JsonNode formItemSchema, JsonNode data,
                                                             SimpleBindings dataBindings) {
        JsonNode name = formItemSchema.get("name");

        if (name == null) {
            return new ArrayList<ConstraintViolation>();
        }
        JsonNode itemData = data.get(name.asText());
        JsonNode validations = formItemSchema.get("validations");

        // 将 requireOn 转成 isRequired
        JsonNode requireOn = formItemSchema.get("requireOn");
        if (requireOn != null && !requireOn.asText().isEmpty()) {
            boolean res = Script.eval(requireOn.asText(), dataBindings);
            if (res) {
                // 如果是 null 就新建一个
                if (validations == null) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode node = mapper.createObjectNode();
                    node.put("isRequired", true);
                    validations = node;
                } else {
                    ((ObjectNode) validations).put("isRequired", true);
                }
            }
        }

        if (validations == null) {
            return new ArrayList<ConstraintViolation>();
        }

        // visibleOn 和 hiddenOn
        JsonNode visibleOn = formItemSchema.get("visibleOn");
        if (visibleOn != null && !requireOn.asText().isEmpty()) {
            boolean res = Script.eval(visibleOn.asText(), dataBindings);
            if (!res) {
                return new ArrayList<ConstraintViolation>();
            }
        }
        JsonNode hiddenOn = formItemSchema.get("hiddenOn");
        if (hiddenOn != null && !hiddenOn.asText().isEmpty()) {
            boolean res = Script.eval(hiddenOn.asText(), dataBindings);
            if (res) {
                return new ArrayList<ConstraintViolation>();
            }
        }

        JsonNodeType validationsNodeType = validations.getNodeType();
        // 将老 string 写法转成新的对象方式
        if (validationsNodeType == JsonNodeType.STRING) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode validationObject = mapper.createObjectNode();
            String[] validateWithValues = validations.asText().split(",");
            for (String validateWithValue : validateWithValues) {
                String[] validateAndValue = validateWithValue.split(":");
                if (validateAndValue.length > 1) {
                    validationObject.put(validateAndValue[0], validateAndValue[1]);
                } else {
                    validationObject.put(validateAndValue[0], true);
                }
            }
            validations = validationObject;
        }

        if (validations != null && validations.isObject()) {

            // required 转成 isRequired 校验
            JsonNode required = validations.get("required");
            if (required != null && required.asBoolean()) {
                ((ObjectNode) validations).put("isRequired", true);
            }

            // "hidden": true 或者 "visible": false 的表单项相当于禁用了，不做处理
            JsonNode visible = validations.get("visible");
            if (visible != null && !visible.asBoolean()) {
                return new ArrayList<ConstraintViolation>();
            }
            JsonNode hidden = validations.get("hidden");
            if (hidden != null && hidden.asBoolean()) {
                return new ArrayList<ConstraintViolation>();
            }

            ArrayList<ConstraintViolation> violationResult = new ArrayList<ConstraintViolation>();
            Iterator<Map.Entry<String, JsonNode>> it = validations.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                String validateName = entry.getKey();
                JsonNode validateOption = entry.getValue();
                // 用反射会导致不好查找代码，挨个写好了
                switch (validateName) {
                    case "isRequired":
                        if (!ValidationFn.isRequired(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isRequired",
                                    ViolationMessage.isRequired));
                        }
                        break;
                    case "isExisty":
                        if (!ValidationFn.isExisty(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isExisty",
                                    ViolationMessage.isExisty));
                        }
                        break;
                    case "isEmail":
                        if (!ValidationFn.isEmail(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isEmail",
                                    ViolationMessage.isEmail));
                        }
                        break;
                    case "isUrl":
                        if (!ValidationFn.isUrl(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isUrl",
                                    ViolationMessage.isUrl));
                        }
                        break;
                    case "isInt":
                        if (!ValidationFn.isInt(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isInt",
                                    ViolationMessage.isInt));
                        }
                        break;
                    case "isAlpha":
                        if (!ValidationFn.isAlpha(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isAlpha",
                                    ViolationMessage.isAlpha));
                        }
                        break;
                    case "isNumeric":
                        if (!ValidationFn.isNumeric(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isNumeric",
                                    ViolationMessage.isNumeric));
                        }
                        break;
                    case "isAlphanumeric":
                        if (!ValidationFn.isAlphanumeric(itemData)) {
                            violationResult.add(
                                    genConstraintViolation(name.asText(), formItemSchema, "isAlphanumeric",
                                            ViolationMessage.isAlphanumeric));
                        }
                        break;
                    case "isFloat":
                        if (!ValidationFn.isFloat(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isFloat",
                                    ViolationMessage.isFloat));
                        }
                        break;
                    case "isWords":
                        if (!ValidationFn.isWords(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isWords",
                                    ViolationMessage.isWords));
                        }
                        break;
                    case "isUrlPath":
                        if (!ValidationFn.isUrlPath(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isUrlPath",
                                    ViolationMessage.isUrlPath));
                        }
                        break;
                    case "matchRegexp":
                        if (!ValidationFn.matchRegexp(itemData, validateOption.asText())) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "matchRegexp",
                                    ViolationMessage.matchRegexp));
                        }
                        break;
                    case "minLength":
                        if (!ValidationFn.minLength(itemData, validateOption.asInt())) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "minLength",
                                    ViolationMessage.minLength));
                        }
                        break;
                    case "maxLength":
                        if (!ValidationFn.maxLength(itemData, validateOption.asInt())) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "maxLength",
                                    ViolationMessage.maxLength));
                        }
                        break;
                    case "maximum":
                        if (!ValidationFn.maximum(itemData, validateOption.asDouble())) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "maximum",
                                    ViolationMessage.maximum));
                        }
                        break;
                    case "lt":
                        if (!ValidationFn.lt(itemData, validateOption.asDouble())) {
                            violationResult.add(
                                    genConstraintViolation(name.asText(), formItemSchema, "lt", ViolationMessage.lt));
                        }
                        break;
                    case "minimum":
                        if (!ValidationFn.minimum(itemData, validateOption.asDouble())) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "minimum",
                                    ViolationMessage.minimum));
                        }
                        break;
                    case "gt":
                        if (!ValidationFn.gt(itemData, validateOption.asDouble())) {
                            violationResult.add(
                                    genConstraintViolation(name.asText(), formItemSchema, "gt", ViolationMessage.gt));
                        }
                        break;
                    case "isJson":
                        if (!ValidationFn.isJson(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isJson",
                                    ViolationMessage.isJson));
                        }
                        break;
                    case "isLength":
                        if (!ValidationFn.isLength(itemData, validateOption.asInt())) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isLength",
                                    ViolationMessage.isLength));
                        }
                        break;
                    case "notEmptyString":
                        if (!ValidationFn.notEmptyString(itemData)) {
                            violationResult.add(
                                    genConstraintViolation(name.asText(), formItemSchema, "notEmptyString",
                                            ViolationMessage.notEmptyString));
                        }
                        break;
                    case "equalsField":
                        if (!ValidationFn.equalsField(itemData, validateOption.asText())) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "equalsField",
                                    ViolationMessage.equalsField));
                        }
                        break;
                    case "equals":
                        if (!ValidationFn.equals(itemData, validateOption)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isEmail",
                                    ViolationMessage.isEmail));
                        }
                        break;
                    case "isPhoneNumber":
                        if (!ValidationFn.isPhoneNumber(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isPhoneNumber",
                                    ViolationMessage.isPhoneNumber));
                        }
                        break;
                    case "isTelNumber":
                        if (!ValidationFn.isTelNumber(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isTelNumber",
                                    ViolationMessage.isTelNumber));
                        }
                        break;
                    case "isZipcode":
                        if (!ValidationFn.isZipcode(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isZipcode",
                                    ViolationMessage.isZipcode));
                        }
                        break;
                    case "isId":
                        if (!ValidationFn.isId(itemData)) {
                            violationResult.add(genConstraintViolation(name.asText(), formItemSchema, "isId",
                                    ViolationMessage.isId));
                        }
                        break;
                }
            }
            return violationResult;
        }
        return new ArrayList<ConstraintViolation>();
    }

}
