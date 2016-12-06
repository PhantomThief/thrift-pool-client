/**
 * 
 */
package com.github.phantomthief.thrift.client.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import com.github.phantomthief.thrift.client.ThriftClient;
import com.github.phantomthief.thrift.client.pool.ThriftConnectionPoolProvider;
import com.github.phantomthief.thrift.client.pool.ThriftServerInfo;
import com.github.phantomthief.thrift.client.pool.impl.DefaultThriftConnectionPoolImpl;
import com.github.phantomthief.thrift.client.utils.FailoverCheckingStrategy;
import com.github.phantomthief.thrift.client.utils.ThriftClientUtils;

/**
 * <p>
 * FailoverThriftClientImpl class.
 * </p>
 *
 * @author w.vela
 * @version $Id: $Id
 */
public class FailoverThriftClientImpl implements ThriftClient {

    private final ThriftClient thriftClient;

    /**
     * <p>
     * Constructor for FailoverThriftClientImpl.
     * </p>
     *
     * @param serverInfoProvider a {@link java.util.function.Supplier}
     *        object.
     */
    public FailoverThriftClientImpl(Supplier<List<ThriftServerInfo>> serverInfoProvider) {
        this(new FailoverCheckingStrategy<>(), serverInfoProvider, DefaultThriftConnectionPoolImpl
                .getInstance());
    }

    /**
     * <p>
     * Constructor for FailoverThriftClientImpl.
     * </p>
     *
     * @param failoverCheckingStrategy a
     *        {@link com.github.phantomthief.thrift.client.utils.FailoverCheckingStrategy}
     *        object.
     * @param serverInfoProvider a {@link java.util.function.Supplier}
     *        object.
     * @param poolProvider a
     *        {@link com.github.phantomthief.thrift.client.pool.ThriftConnectionPoolProvider}
     *        object.
     */
    public FailoverThriftClientImpl(
            FailoverCheckingStrategy<ThriftServerInfo> failoverCheckingStrategy,
            Supplier<List<ThriftServerInfo>> serverInfoProvider,
            ThriftConnectionPoolProvider poolProvider) {
        FailoverStategy failoverStategy = new FailoverStategy(serverInfoProvider, poolProvider,
                failoverCheckingStrategy);
        this.thriftClient = new ThriftClientImpl(failoverStategy, failoverStategy);
    }

    /** {@inheritDoc} */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass) {
        return thriftClient.iface(ifaceClass);
    }

    /* (non-Javadoc)
     * @see com.github.phantomthief.thrift.client.ThriftClient#iface(java.lang.Class)
     */

    /** {@inheritDoc} */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass, int hash) {
        return thriftClient.iface(ifaceClass, hash);
    }

    /* (non-Javadoc)
     * @see com.github.phantomthief.thrift.client.ThriftClient#iface(java.lang.Class, int)
     */

    /** {@inheritDoc} */
    @Override
    public <X extends TServiceClient> X iface(Class<X> ifaceClass,
            Function<TTransport, TProtocol> protocolProvider, int hash) {
        return thriftClient.iface(ifaceClass, protocolProvider, hash);
    }

    /** {@inheritDoc} */
    @Override
    public <X extends TServiceClient> X mpiface(Class<X> ifaceClass, String serviceName) {
        return thriftClient.mpiface(ifaceClass, serviceName, ThriftClientUtils.randomNextInt());
    }

    /** {@inheritDoc} */
    @Override
    public <X extends TServiceClient> X mpiface(Class<X> ifaceClass, String serviceName, int hash) {
        return thriftClient.mpiface(ifaceClass, serviceName, TBinaryProtocol::new, hash);
    }

    /** {@inheritDoc} */
    @Override
    public <X extends TServiceClient> X mpiface(Class<X> ifaceClass, String serviceName,
        Function<TTransport, TProtocol> protocolProvider, int hash) {
        return thriftClient.mpiface(ifaceClass, serviceName, protocolProvider, hash);
    }

    /* (non-Javadoc)
     * @see com.github.phantomthief.thrift.client.ThriftClient#iface(java.lang.Class, java.util.function.Function, int)
     */

    private class FailoverStategy implements
                                 Supplier<List<ThriftServerInfo>>,
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
            Set<ThriftServerInfo> failedServers = failoverCheckingStrategy.getFailed();
            return originalServerInfoProvider.get().stream()
                    .filter(i -> !failedServers.contains(i)).collect(toList());
        }

        /* (non-Javadoc)
         * @see com.github.phantomthief.thrift.client.pool.ThriftConnectionPoolProvider#getConnection(com.github.phantomthief.thrift.client.pool.ThriftServerInfo)
         */
        @Override
        public TTransport getConnection(ThriftServerInfo thriftServerInfo) {
            return connectionPoolProvider.getConnection(thriftServerInfo);
        }

        /* (non-Javadoc)
         * @see com.github.phantomthief.thrift.client.pool.ThriftConnectionPoolProvider#returnConnection(com.github.phantomthief.thrift.client.pool.ThriftServerInfo, org.apache.thrift.transport.TTransport)
         */
        @Override
        public void returnConnection(ThriftServerInfo thriftServerInfo, TTransport transport) {
            connectionPoolProvider.returnConnection(thriftServerInfo, transport);
        }

        /* (non-Javadoc)
         * @see com.github.phantomthief.thrift.client.pool.ThriftConnectionPoolProvider#returnBrokenConnection(com.github.phantomthief.thrift.client.pool.ThriftServerInfo, org.apache.thrift.transport.TTransport)
         */
        @Override
        public void returnBrokenConnection(ThriftServerInfo thriftServerInfo, TTransport transport) {
            failoverCheckingStrategy.fail(thriftServerInfo);
            connectionPoolProvider.returnBrokenConnection(thriftServerInfo, transport);
        }
    }

}
