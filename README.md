thrift-client-pool-java
=======================

A Thrift Client pool for Java

* raw and TypeSafe TServiceClient pool
* Multi Backend Servers support
* Backend Servers replace on the fly
* Backend route by hash or any other algorithm
* Ease of use
* jdk 1.8 only

## Usage

<pre><code>

        // define serverList provider, you can use dynamic provider here to impl on the fly changing...
        Supplier<List<ThriftServerInfo>> serverListProvider = () -> Arrays.asList( //
                new ThriftServerInfo("127.0.0.1", 9092), //
                new ThriftServerInfo("127.0.0.1", 9091), //
                new ThriftServerInfo("127.0.0.1", 9090));

        // init pool client
        ThriftClient client = new ThriftClient(serverListProvider);

        // do call as normal
        String result = client.iface(me.vela.thrift.test.service.TestThriftService.Client.class)
                .echo("haha");
        System.out.println(result);

        GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
        // customize pool config here...
        ThriftClient customizedPoolClient = new ThriftClient(serverListProvider,
                new DefaultThriftConnectionPoolImpl(poolConfig));
        String param = "hello";
        // customize server hash
        String result2 = client.iface(me.vela.thrift.test.service.TestThriftService.Client.class,
                param.hashCode()).echo(param);
        System.out.println(result2);
    
</pre></code>
