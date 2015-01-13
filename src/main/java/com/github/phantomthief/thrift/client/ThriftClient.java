/**
 * 
 */
package com.github.phantomthief.thrift.client;

import java.util.function.Function;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

/**
 * <p>ThriftClient interface.</p>
 *
 * @author w.vela
 * @version $Id: $Id
 */
public interface ThriftClient {

    /**
     * <p>iface.</p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @return a X object.
     */
    public <X extends TServiceClient> X iface(Class<X> ifaceClass);

    /**
     * <p>iface.</p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @param hash a int.
     * @return a X object.
     */
    public <X extends TServiceClient> X iface(Class<X> ifaceClass, int hash);

    /**
     * <p>iface.</p>
     *
     * @param ifaceClass a {@link java.lang.Class} object.
     * @param protocolProvider a {@link java.util.function.Function} object.
     * @param hash a int.
     * @return a X object.
     */
    public <X extends TServiceClient> X iface(Class<X> ifaceClass,
            Function<TTransport, TProtocol> protocolProvider, int hash);

}
