/**
 * 
 */
package me.vela.thrift.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import me.vela.thrift.client.impl.ThriftClientImpl;
import me.vela.thrift.client.pool.ThriftServerInfo;
import me.vela.thrift.test.service.TestThriftService.Client;

import org.junit.Test;

/**
 * @author w.vela
 *
 * @date 2014年11月22日 下午9:10:30
 */
public class TestThriftPoolClient {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @Test
    public void testEcho() throws InterruptedException {

        // define serverList provider, you can use dynamic provider here to impl on the fly changing...
        Supplier<List<ThriftServerInfo>> serverListProvider = () -> Arrays.asList( //
                new ThriftServerInfo("127.0.0.1", 9092), //
                new ThriftServerInfo("127.0.0.1", 9091), //
                new ThriftServerInfo("127.0.0.1", 9090));

        // init pool client
        ThriftClientImpl client = new ThriftClientImpl(serverListProvider);

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            int counter = i;
            executorService.submit(() -> {
                try {
                    String result = client.iface(Client.class).echo("hi " + counter + "!");
                    logger.info("get result: {}", result);
                } catch (Throwable e) {
                    logger.error("get client fail", e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }
}
