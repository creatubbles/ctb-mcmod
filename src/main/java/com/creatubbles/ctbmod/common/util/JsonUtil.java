package com.creatubbles.ctbmod.common.util;

import java.lang.reflect.Type;
import java.util.EnumMap;

import com.google.gson.InstanceCreator;

public class JsonUtil {

    public static class EnumMapInstanceCreator<K extends Enum<K>, V> implements InstanceCreator<EnumMap<K, V>> {

        private final Class<K> enumClazz;

        public EnumMapInstanceCreator(final Class<K> enumClazz) {
            super();
            this.enumClazz = enumClazz;
        }

        @Override
        public EnumMap<K, V> createInstance(final Type type) {
            return new EnumMap<K, V>(enumClazz);
        }
    }
}
