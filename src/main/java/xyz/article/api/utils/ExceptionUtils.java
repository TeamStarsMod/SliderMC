package xyz.article.api.utils;

import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    public static void exceptionHandler(Logger logger, Throwable cause) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        cause.printStackTrace(pw);
        logger.error("发生异常：{}", writer);
    }
}
