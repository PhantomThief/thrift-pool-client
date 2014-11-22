/**
 * 
 */
package me.vela.thrift.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import me.vela.thrift.client.pool.ThriftConnectionPoolProvider;
import me.vela.thrift.client.pool.ThriftServerInfo;
import me.vela.thrift.client.pool.impl.DefaultThriftConnectionPoolImpl;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

/**
 * @author w.vela
 *
 * @date 2014年7月14日 下午5:01:17
 */
public class ThriftClient {

    private final ThriftConnectionPoolProvider poolProvider;

    private final Supplier<List<ThriftServerInfo>> servicesInfoProvider;

    private final Random random = new Random();

    /**
     * @param servicesInfoProvider
     */
    public ThriftClient(Supplier<List<ThriftServerInfo>> servicesInfoProvider) {
        this(servicesInfoProvider, DefaultThriftConnectionPoolImpl.getInstance());
    }

    /**
     * @param servicesInfoProvider
     * @param poolProvider
     */
    public ThriftClient(Supplier<List<ThriftServerInfo>> servicesInfoProvider,
            ThriftConnectionPoolProvider poolProvider) {
        this.poolProvider = poolProvider;
        this.servicesInfoProvider = servicesInfoProvider;
    }

    public <X extends TServiceClient> X iface(Function<TProtocol, X> clientConstructor) {
        return iface(clientConstructor, random.nextInt());
    }

    @SuppressWarnings("unchecked")
    public <X extends TServiceClient> X iface(Function<TProtocol, X> clientConstructor, int hash) {
        List<ThriftServerInfo> servers = servicesInfoProvider.get();
        if (servers == null || servers.isEmpty()) {
            throw new RuntimeException("no backend server.");
        }
        hash = Math.abs(hash);
        hash = hash < 0 ? 0 : hash;
        ThriftServerInfo selected = servers.get(hash % servers.size());

        TTransport transport = poolProvider.getConnection(selected);
        TProtocol protocol = new TCompactProtocol(transport);
        X client = clientConstructor.apply(protocol);

        return (X) Proxy.newProxyInstance(ThriftClient.class.getClassLoader(), client.getClass()
                .getInterfaces(), new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                boolean success = false;
                try {
                    Object result = method.invoke(proxy, args);
                    success = true;
                    return result;
                } finally {
                    if (success) {
                        poolProvider.returnConnection(selected, transport);
                    } else {
                        poolProvider.returnBrokenConnection(selected, transport);
                    }
                }
            }
        });
    }

}
