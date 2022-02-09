package org.amurzeau.allocation;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.core.IsIterableContaining;

public class Utils {
    public static Map<Object, Object> toMap(Object jsonObject) {
        ObjectMapper oMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        var ret = (Map<Object, Object>) oMapper.convertValue(jsonObject, Map.class);

        return ret;
    }

    public static org.hamcrest.Matcher<java.lang.Iterable<? super Map<Object, Object>>> hasItemFromObject(
            Object jsonObject) {
        return IsIterableContaining.hasItem(toMap(jsonObject));
    }
}
