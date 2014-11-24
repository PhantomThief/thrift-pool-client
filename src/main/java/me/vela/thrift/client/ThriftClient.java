/**
 * 
 */
package me.vela.thrift.client;

import java.util.function.Function;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

/**
 * @author w.vela
 */
public interface ThriftClient {

    /**
     * @param ifaceClass
     * @return
     */
    public <X extends TServiceClient> X iface(Class<X> ifaceClass);

    /**
     * @param ifaceClass
     * @param hash
     * @return
     */
    public <X extends TServiceClient> X iface(Class<X> ifaceClass, int hash);

    /**
     * @param ifaceClass
     * @param protocolProvider
     * @param hash
     * @return
     */
    public <X extends TServiceClient> X iface(Class<X> ifaceClass,
            Function<TTransport, TProtocol> protocolProvider, int hash);

}
