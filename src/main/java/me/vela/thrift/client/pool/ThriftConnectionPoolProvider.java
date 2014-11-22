/**
 * 
 */
package me.vela.thrift.client.pool;

import org.apache.thrift.transport.TTransport;

/**
 * @author w.vela
 *
 * @date 2014年11月22日 下午8:57:12
 */
public interface ThriftConnectionPoolProvider {

    /**
     * @param thriftServerInfo
     * @return
     */
    public TTransport getConnection(ThriftServerInfo thriftServerInfo);

    /**
     * @param thriftServerInfo
     * @param transport
     */
    public void returnConnection(ThriftServerInfo thriftServerInfo, TTransport transport);

    /**
     * @param thriftServerInfo
     * @param transport
     */
    public void returnBrokenConnection(ThriftServerInfo thriftServerInfo, TTransport transport);

}
