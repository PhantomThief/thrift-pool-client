/**
 * 
 */
package com.github.phantomthief.thrift.example;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.github.phantomthief.thrift.client.ThriftClient;
import com.github.phantomthief.thrift.client.impl.FailoverThriftClientImpl;
import com.github.phantomthief.thrift.client.impl.ThriftClientImpl;
import com.github.phantomthief.thrift.client.pool.ThriftServerInfo;
import com.github.phantomthief.thrift.client.pool.impl.DefaultThriftConnectionPoolImpl;
import com.github.phantomthief.thrift.client.utils.FailoverCheckingStrategy;
import com.github.phantomthief.thrift.test.service.TestThriftService.Client;

/**
 * @author w.vela
 */
public class ClientMain {

    /**
     * @throws TException
     */
    public static void main(String[] args) throws TException {
        // init a thrift client
        ThriftClient thriftClient = new ThriftClientImpl(() -> Arrays.asList(//
                ThriftServerInfo.of("127.0.0.1", 9090),
                ThriftServerInfo.of("127.0.0.1", 9091)
                // or you can return a dynamic result.
                ));
        // get iface and call
        System.out.println(thriftClient.iface(Client.class).echo("hello world."));

        // get iface with custom hash, the same hash return the same thrift backend server
        System.out.println(thriftClient.iface(Client.class, "hello world".hashCode()).echo(
                "hello world"));

        // customize protocol
        System.out.println(thriftClient.iface(Client.class, TBinaryProtocol::new,
                "hello world".hashCode()).echo("hello world"));

        // customize pool config
        GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
        // ... customize pool config here
        // customize transport, while if you expect pooling the connection, you should use TFrameTransport.
        Function<ThriftServerInfo, TTransport> transportProvider = info -> {
            TSocket socket = new TSocket(info.getHost(), info.getPort());
            TFramedTransport transport = new TFramedTransport(socket);
            return transport;
        };
        ThriftClient customizeThriftClient = new ThriftClientImpl(() -> Arrays.asList(//
                ThriftServerInfo.of("127.0.0.1", 9090),
                ThriftServerInfo.of("127.0.0.1", 9091)
                ), new DefaultThriftConnectionPoolImpl(poolConfig, transportProvider));
        customizeThriftClient.iface(Client.class).echo("hello world.");

        // init a failover thrift client
        ThriftClient failoverThriftClient = new FailoverThriftClientImpl(() -> Arrays.asList(//
                ThriftServerInfo.of("127.0.0.1", 9090),
                ThriftServerInfo.of("127.0.0.1", 9091)
                ));
        failoverThriftClient.iface(Client.class).echo("hello world.");

        // a customize failover client, if the call fail 10 times in 30 seconds, the backend server will be marked as fail for 1 minutes.
        FailoverCheckingStrategy<ThriftServerInfo> failoverCheckingStrategy = new FailoverCheckingStrategy<>(
                10, TimeUnit.SECONDS.toMillis(30), TimeUnit.MINUTES.toMillis(1));
        ThriftClient customizedFailoverThriftClient = new FailoverThriftClientImpl(
                failoverCheckingStrategy, () -> Arrays.asList(//
                        ThriftServerInfo.of("127.0.0.1", 9090),
                        ThriftServerInfo.of("127.0.0.1", 9091)
                        ), DefaultThriftConnectionPoolImpl.getInstance());
        customizedFailoverThriftClient.iface(Client.class).echo("hello world.");
    }
}
