/**
 * 
 */
package me.vela.thrift.client.pool;

/**
 * <p>
 * ThriftServerInfo class.
 * </p>
 *
 * @author w.vela
 * @version $Id: $Id
 */
public final class ThriftServerInfo {

    private final String host;

    private final int port;

    /**
     * <p>
     * Constructor for ThriftServerInfo.
     * </p>
     *
     * @param host host
     * @param port port
     */
    public ThriftServerInfo(String host, int port) {
        this.host = host;
        this.port = port;
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
