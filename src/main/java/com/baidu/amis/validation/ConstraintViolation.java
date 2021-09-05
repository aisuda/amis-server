package com.baidu.amis.validation;

/**
 * 仿照 javax.validation 力度命名，但多了个字段名
 */

public class ConstraintViolation {
    // 字段名
    private String name;

    // 违反信息
    private String message;

    public ConstraintViolation(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

}
