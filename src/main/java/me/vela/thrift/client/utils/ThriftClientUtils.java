/**
 * 
 */
package me.vela.thrift.client.utils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author w.vela
 *
 * @date 2014年11月24日 上午10:18:11
 */
public final class ThriftClientUtils {

    private static ConcurrentMap<Class<?>, Set<String>> interfaceMethodCache = new ConcurrentHashMap<>();

    private ThriftClientUtils() {
        throw new UnsupportedOperationException();
    }

    public static final Set<String> getInterfaceMethodNames(Class<?> ifaceClass) {
        return interfaceMethodCache.computeIfAbsent(
                ifaceClass,
                i -> Stream.of(i.getInterfaces()).flatMap(c -> Stream.of(c.getMethods()))
                        .map(Method::getName).collect(Collectors.toSet()));
    }
}
