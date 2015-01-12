/**
 * 
 */
package me.vela.thrift.client.utils;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * ThriftClientUtils class.
 * </p>
 *
 * @author w.vela
 * @version $Id: $Id
 */
public final class ThriftClientUtils {

    private static ConcurrentMap<Class<?>, Set<String>> interfaceMethodCache = new ConcurrentHashMap<>();

    private static final Random RANDOM = new Random();

    private ThriftClientUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * randomNextInt.
     * </p>
     *
     * @return a int.
     */
    public static final int randomNextInt() {
        return RANDOM.nextInt();
    }

    /**
     * <p>
     * getInterfaceMethodNames.
     * </p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @return a {@link java.util.Set} object.
     */
    public static final Set<String> getInterfaceMethodNames(Class<?> ifaceClass) {
        return interfaceMethodCache.computeIfAbsent(
                ifaceClass,
                i -> Stream.of(i.getInterfaces()).flatMap(c -> Stream.of(c.getMethods()))
                        .map(Method::getName).collect(Collectors.toSet()));
    }
}
