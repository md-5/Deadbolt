package com.daemitus.deadbolt.config;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlStorageReader {

    private static Map<Class<?>, Yaml> PREPARED_YAMLS = Collections.synchronizedMap(new HashMap<Class<?>, Yaml>());
    private static final Map<Class<?>, ReentrantLock> LOCKS = new HashMap<Class<?>, ReentrantLock>();
    private Reader reader;
    private Plugin plugin;

    public YamlStorageReader(Reader reader, Plugin plugin) {
        this.reader = reader;
        this.plugin = plugin;
    }

    public <T extends ConfigObject> T load(Class<? extends T> clazz) throws Exception {
        Yaml yaml = PREPARED_YAMLS.get(clazz);
        if (yaml == null) {
            yaml = new Yaml(prepareConstructor(clazz));
            PREPARED_YAMLS.put(clazz, yaml);
        }
        ReentrantLock lock;
        synchronized (LOCKS) {
            lock = LOCKS.get(clazz);
            if (lock == null) {
                lock = new ReentrantLock();
            }
        }
        lock.lock();
        try {
            T object = (T) yaml.load(reader);
            if (object == null) {
                object = clazz.newInstance();
            }
            return object;
        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            lock.unlock();
        }
    }

    private Constructor prepareConstructor(Class<?> clazz) {
        Constructor constructor = new BukkitConstructor(clazz, plugin);
        Set<Class<?>> classes = new HashSet<Class<?>>();

        prepareConstructor(constructor, classes, clazz);
        return constructor;
    }

    private void prepareConstructor(Constructor constructor, Set<Class<?>> classes, Class<?> clazz) {
        classes.add(clazz);
        TypeDescription description = new TypeDescription(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            prepareList(field, description, classes, constructor);
            prepareMap(field, description, classes, constructor);

            if (ConfigObject.class.isAssignableFrom(field.getType()) && !classes.contains(field.getType())) {
                prepareConstructor(constructor, classes, field.getType());
            }
        }
        constructor.addTypeDescription(description);


    }

    private void prepareList(Field field, TypeDescription description, Set<Class<?>> classes, Constructor constructor) {
        ListType listType = field.getAnnotation(ListType.class);
        if (listType != null) {
            description.putListPropertyType(field.getName(), listType.value());
            if (ConfigObject.class.isAssignableFrom(listType.value()) && !classes.contains(listType.value())) {
                prepareConstructor(constructor, classes, listType.value());
            }
        }
    }

    private void prepareMap(Field field, TypeDescription description, Set<Class<?>> classes, Constructor constructor) {
        MapValueType mapType = field.getAnnotation(MapValueType.class);
        if (mapType != null) {
            MapKeyType mapKeyType = field.getAnnotation(MapKeyType.class);
            description.putMapPropertyType(field.getName(), mapKeyType == null ? String.class : mapKeyType.value(), mapType.value());
            if (ConfigObject.class.isAssignableFrom(mapType.value()) && !classes.contains(mapType.value())) {
                prepareConstructor(constructor, classes, mapType.value());
            }
        }
    }
}
