/**
 * 
 */
package me.vela.thrift.client.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import me.vela.thrift.client.ThriftClient;
import me.vela.thrift.client.exception.NoBackendException;
import me.vela.thrift.client.pool.ThriftConnectionPoolProvider;
import me.vela.thrift.client.pool.ThriftServerInfo;
import me.vela.thrift.client.pool.impl.DefaultThriftConnectionPoolImpl;
import me.vela.thrift.client.utils.ThriftClientUtils;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

/**
 * <p>
 * ThriftClientImpl class.
 * </p>
 *
 * @author w.vela
 * @version $Id: $Id
 */
public class ThriftClientImpl implements ThriftClient {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    private final ThriftConnectionPoolProvider poolProvider;

    private final Supplier<List<ThriftServerInfo>> serverInfoProvider;

    private final Random random = new Random();

    /**
     * <p>
     * Constructor for ThriftClientImpl.
     * </p>
     *
     * @param serverInfoProvider provide service list
     */
    public ThriftClientImpl(Supplier<List<ThriftServerInfo>> serverInfoProvider) {
        this(serverInfoProvider, DefaultThriftConnectionPoolImpl.getInstance());
    }

    /**
     * <p>
     * Constructor for ThriftClientImpl.
     * </p>
     *
     * @param serverInfoProvider provide service list
     * @param poolProvider provide a pool
     */
    public ThriftClientImpl(Supplier<List<ThriftServerInfo>> serverInfoProvider,
            ThriftConnectionPoolProvider poolProvider) {
        this.poolProvider = poolProvider;
        this.serverInfoProvider = serverInfoProvider;
    }

    /**
     * <p>
     * iface.
     * </p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @return proxied iface class
     * @param <X> a X object.
     */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass) {
        return iface(ifaceClass, random.nextInt());
    }

    /**
     * <p>
     * iface.
     * </p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @param hash a int.
     * @return proxied iface class
     * @param <X> a X object.
     */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass, int hash) {
        return iface(ifaceClass, TCompactProtocol::new, hash);
    }

    /**
     * <p>
     * iface.
     * </p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @param protocolProvider a {@link java.util.function.Function}
     *        object.
     * @param hash a int.
     * @return proxied iface class
     * @param <X> a X object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass,
            Function<TTransport, TProtocol> protocolProvider, int hash) {
        List<ThriftServerInfo> servers = serverInfoProvider.get();
        if (servers == null || servers.isEmpty()) {
            throw new NoBackendException();
        }
        hash = Math.abs(hash);
        hash = hash < 0 ? 0 : hash;
        ThriftServerInfo selected = servers.get(hash % servers.size());
        logger.trace("get connection for [{}]->{} with hash:{}", ifaceClass, selected, hash);

        TTransport transport = poolProvider.getConnection(selected);
        TProtocol protocol = protocolProvider.apply(transport);

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(ifaceClass);
        factory.setFilter(m -> ThriftClientUtils.getInterfaceMethodNames(ifaceClass).contains(
                m.getName()));
        try {
            X x = (X) factory.create(new Class[] { org.apache.thrift.protocol.TProtocol.class },
                    new Object[] { protocol });
            ((Proxy) x).setHandler((self, thisMethod, proceed, args) -> {
                boolean success = false;
                try {
                    Object result = proceed.invoke(self, args);
                    success = true;
                    return result;
                } finally {
                    if (success) {
                        poolProvider.returnConnection(selected, transport);
                    } else {
                        poolProvider.returnBrokenConnection(selected, transport);
                    }
                }
            });
            return x;
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("fail to create proxy.", e);
        }
    }

}
