package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
    private Map<Class<?>, Object> instancesMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    //    && fields.getDeclaringClass().isAnnotationPresent(Component.class)
    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredField = clazz.getDeclaredFields();
        for (Field field : declaredField) {
            if (field.isAnnotationPresent(Inject.class)
                    && field.getDeclaringClass().isAnnotationPresent(Component.class)) {
                Object fieldInstance = getInstance(field.getType());

                clazzImplementationInstance = createNewInstance(clazz);

                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cant initialise field value"
                            + clazz.getName() + field.getName());
                }

            }
        }

        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementation = new HashMap<>();
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementation.get(interfaceClazz);
        }
        return null;
    }

    public Object createNewInstance(Class<?> clazz) {
        if (instancesMap.containsKey(clazz)) {
            return instancesMap.get(clazz);
        }

        Constructor<?> constructor = null;

        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instancesMap.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cant create new instance" + clazz.getName());
        }

    }

}
