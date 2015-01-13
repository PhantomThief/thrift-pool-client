/**
 * 
 */
package com.github.phantomthief.thrift.client.pool;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Splitter;
import com.google.common.collect.MapMaker;

/**
 * <p>
 * ThriftServerInfo class.
 * </p>
 *
 * @author w.vela
 * @version $Id: $Id
 */
public final class ThriftServerInfo {

    private static ConcurrentMap<String, ThriftServerInfo> allInfos = new MapMaker().weakValues()
            .makeMap();

    private static Splitter splitter = Splitter.on(':');

    private final String host;

    private final int port;

    /**
     * <p>
     * Constructor for ThriftServerInfo.
     * </p>
     *
     * @param hostAndPort
     */
    private ThriftServerInfo(String hostAndPort) {
        List<String> split = splitter.splitToList(hostAndPort);
        assert split.size() == 2;
        this.host = split.get(0);
        this.port = Integer.parseInt(split.get(1));
    }

    public static final ThriftServerInfo of(String host, int port) {
        return allInfos.computeIfAbsent(host + ":" + port, ThriftServerInfo::new);
    }

    public static final ThriftServerInfo of(String hostAndPort) {
        return allInfos.computeIfAbsent(hostAndPort, ThriftServerInfo::new);
    }

    /**
     * <p>
     * Getter for the field <code>host</code>.
     * </p>
     *
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * <p>
     * Getter for the field <code>port</code>.
     * </p>
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ThriftServerInfo)) {
            return false;
        }
        ThriftServerInfo other = (ThriftServerInfo) obj;
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ThriftServerInfo [host=" + host + ", port=" + port + "]";
    }

}
