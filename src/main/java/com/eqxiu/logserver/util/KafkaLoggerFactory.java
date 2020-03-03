package com.eqxiu.logserver.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存一个topic-logger对象map
 */
public class KafkaLoggerFactory {

    private static Map<String, Logger> loggers = new HashMap<>();

    public  static Logger getLogger(String topic) {
        Logger logger = loggers.get(topic);
        if (logger == null) {
            synchronized(KafkaLoggerFactory.class) {
                if (logger == null) {
                    logger = LoggerFactory.getLogger(topic);
                    loggers.put(topic, logger);
                }
            }
        }
        return logger;
    }
}