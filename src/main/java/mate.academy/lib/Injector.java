package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object implementationInstance = null;
        Class<?> clazzImplementation = findImplementation(interfaceClazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)
                    && clazzImplementation.isAnnotationPresent(Component.class)) {
                Object instance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(implementationInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value: ");
                }
            }
            throw new RuntimeException();
        }

        if (implementationInstance == null) {
            implementationInstance = createInstance(clazzImplementation);
        }
        return implementationInstance;
    }

    private Object createInstance(Class<?> clazzImplementation) {
        if (instances.containsKey(clazzImplementation)) {
            return instances.get(clazzImplementation);
        }

        try {
            Constructor<?> constructor = clazzImplementation.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazzImplementation, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
