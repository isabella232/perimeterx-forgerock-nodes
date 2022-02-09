package com.perimeterx.BD.nodes.PX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PXLogger {
    private Logger logger;
    private final String DEBUG_PREFIX = "[PerimeterX - DEBUG] ";
    private final String ERROR_PREFIX = "[PerimeterX - ERROR] ";

    public static PXLogger getLogger(Class<?> clazz) {
        return new PXLogger(clazz);
    }

    private PXLogger(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    public void debug(String msg, Object... args) {
        logger.debug(DEBUG_PREFIX + msg, args);
    }

    public void error(String msg, Object... args) {
        logger.error(ERROR_PREFIX + msg, args);
    }
}