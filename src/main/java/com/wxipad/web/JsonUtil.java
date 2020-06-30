package com.wxipad.web;

/**
 * 功能描述
 *
 * @author: aweie
 * @date: 2019/7/5 00051:45
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String convertObj2String(Object object) {
        String s = null;
        try {
            s = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static <T> T convertString2Obj(String s, Class<T> clazz) {
        T t = null;
        try {
            t = objectMapper.readValue(s, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }

}
