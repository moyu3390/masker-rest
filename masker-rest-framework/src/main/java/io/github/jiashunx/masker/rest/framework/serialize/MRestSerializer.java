package io.github.jiashunx.masker.rest.framework.serialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jiashunx.masker.rest.framework.exception.MRestSerializeException;
import io.github.jiashunx.masker.rest.framework.serialize.impl.MRestJSONSerializer;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author jiashunx
 */
public class MRestSerializer {

    public static byte[] jsonSerialize(Object object) {
        return new MRestJSONSerializer().serialize(object);
    }

    public static <T> T jsonDeserialize(Class<T> klass, byte[] bytes) {
        return new MRestJSONSerializer().deserialize(klass, bytes);
    }

    public static <T> T jsonToObj(String json, Class<T> klass) {
        try {
            return new ObjectMapper().readValue(json, klass);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static <T> T jsonToObj(byte[] bytes, Class<T> klass) {
        try {
            return new ObjectMapper().readValue(bytes, klass);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static <T> List<T> jsonToList(String json, Class<T> klass) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<List<T>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static <T> List<T> jsonToList(byte[] bytes, Class<T> klass) {
        try {
            return new ObjectMapper().readValue(bytes, new TypeReference<List<T>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static String objectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static byte[] objectToJsonBytes(Object object) {
        try {
            return new ObjectMapper().writeValueAsBytes(object);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

}
