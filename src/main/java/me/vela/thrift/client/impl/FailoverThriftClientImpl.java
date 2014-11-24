/**
 * 
 */
package me.vela.thrift.client.impl;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import me.vela.thrift.client.ThriftClient;
import me.vela.thrift.client.pool.ThriftConnectionPoolProvider;
import me.vela.thrift.client.pool.ThriftServerInfo;
import me.vela.thrift.client.pool.impl.DefaultThriftConnectionPoolImpl;
import me.vela.thrift.client.utils.FailoverCheckingStrategy;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

/**
 * @author w.vela
 */
public class FailoverThriftClientImpl implements ThriftClient {

    private final ThriftClient thriftClient;

    private class FailoverStategy implements Supplier<List<ThriftServerInfo>>,
            ThriftConnectionPoolProvider {

        private final Supplier<List<ThriftServerInfo>> originalServerInfoProvider;

        private final ThriftConnectionPoolProvider connectionPoolProvider;

        private final FailoverCheckingStrategy<ThriftServerInfo> failoverCheckingStrategy;

        private FailoverStategy(Supplier<List<ThriftServerInfo>> originalServerInfoProvider,
                ThriftConnectionPoolProvider connectionPoolProvider,
                FailoverCheckingStrategy<ThriftServerInfo> failoverCheckingStrategy) {
            this.originalServerInfoProvider = originalServerInfoProvider;
            this.connectionPoolProvider = connectionPoolProvider;
            this.failoverCheckingStrategy = failoverCheckingStrategy;
        }

        /* (non-Javadoc)
         * @see java.util.function.Supplier#get()
         */
        @Override
        public List<ThriftServerInfo> get() {
            List<ThriftServerInfo> thisServerList = originalServerInfoProvider.get();
            return thisServerList.stream()
                    .filter(i -> !failoverCheckingStrategy.getFailed().contains(i))
                    .collect(Collectors.toList());
        }

        /* (non-Javadoc)
         * @see me.vela.thrift.client.pool.ThriftConnectionPoolProvider#getConnection(me.vela.thrift.client.pool.ThriftServerInfo)
         */
        @Override
        public TTransport getConnection(ThriftServerInfo thriftServerInfo) {
            return connectionPoolProvider.getConnection(thriftServerInfo);
        }

        /* (non-Javadoc)
         * @see me.vela.thrift.client.pool.ThriftConnectionPoolProvider#returnConnection(me.vela.thrift.client.pool.ThriftServerInfo, org.apache.thrift.transport.TTransport)
         */
        @Override
        public void returnConnection(ThriftServerInfo thriftServerInfo, TTransport transport) {
            connectionPoolProvider.returnConnection(thriftServerInfo, transport);
        }

        /* (non-Javadoc)
         * @see me.vela.thrift.client.pool.ThriftConnectionPoolProvider#returnBrokenConnection(me.vela.thrift.client.pool.ThriftServerInfo, org.apache.thrift.transport.TTransport)
         */
        @Override
        public void returnBrokenConnection(ThriftServerInfo thriftServerInfo, TTransport transport) {
            failoverCheckingStrategy.fail(thriftServerInfo);
            connectionPoolProvider.returnBrokenConnection(thriftServerInfo, transport);
        }
    }

    public FailoverThriftClientImpl(Supplier<List<ThriftServerInfo>> serverInfoProvider) {
        this(new FailoverCheckingStrategy<>(), serverInfoProvider, DefaultThriftConnectionPoolImpl
                .getInstance());
    }

    public FailoverThriftClientImpl(
            FailoverCheckingStrategy<ThriftServerInfo> failoverCheckingStrategy,
            Supplier<List<ThriftServerInfo>> serverInfoProvider,
            ThriftConnectionPoolProvider poolProvider) {
        FailoverStategy failoverStategy = new FailoverStategy(serverInfoProvider, poolProvider,
                failoverCheckingStrategy);
        this.thriftClient = new ThriftClientImpl(failoverStategy, failoverStategy);
    }

    /* (non-Javadoc)
     * @see me.vela.thrift.client.ThriftClient#iface(java.lang.Class)
     */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass) {
        return thriftClient.iface(ifaceClass);
    }

    /* (non-Javadoc)
     * @see me.vela.thrift.client.ThriftClient#iface(java.lang.Class, int)
     */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass, int hash) {
        return thriftClient.iface(ifaceClass, hash);
    }

    /* (non-Javadoc)
     * @see me.vela.thrift.client.ThriftClient#iface(java.lang.Class, java.util.function.Function, int)
     */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass,
            Function<TTransport, TProtocol> protocolProvider, int hash) {
        return thriftClient.iface(ifaceClass, protocolProvider, hash);
    }

}
