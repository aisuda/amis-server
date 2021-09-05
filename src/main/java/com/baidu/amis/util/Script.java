package com.baidu.amis.util;

import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * 执行 amis 中的脚本判断
 */
public class Script {

    private static Logger logger = Logger.getLogger("Validator");

    public static boolean eval(String script, SimpleBindings dataBindings) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn"); // TODO: 后续支持 GraalVM

        try {
            Boolean res = (Boolean) engine.eval(script, dataBindings);
            return res;
        } catch (ScriptException e) {
            logger.warning(e.getMessage());
        }
        return false;
    }
}
