/**
 * 
 */
package me.vela.thrift.client.pool;

/**
 * @author w.vela
 *
 * @date 2014年11月22日 下午8:53:33
 */
public final class ThriftServerInfo {

    private final String host;

    private final int port;

    /**
     * @param host host
     * @param port port
     */
    public ThriftServerInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return port
     */
    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + port;
        return result;
    }

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

    @Override
    public String toString() {
        return "ThriftServerInfo [host=" + host + ", port=" + port + "]";
    }
}
