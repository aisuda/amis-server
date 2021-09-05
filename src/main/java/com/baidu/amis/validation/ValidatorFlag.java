package com.baidu.amis.validation;

import java.util.EnumSet;

/**
 * 校验的功能开关，目前只有一个
 */
public enum ValidatorFlag {
    /**
     * 是否关闭脚本功能，这个功能是用来支持类似 requireOn 这样的属性，但可能会不准确，主要有两方面：
     * 1. 在数据属于上级数据域，后端是拿不到的，这个问题无解
     * 2. java 中的 nashorn 引擎只支持 es5，但前端是可以支持 es6 的，要解决必须使用 GraalVM 最新版本，对部署要求比较高
     */
    DISABLE_SCRIPT;

    public static final EnumSet<ValidatorFlag> ALL_OPTS = EnumSet.allOf(ValidatorFlag.class);
}
