package zos.shell.controller.dependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

public class Dependency {

    private static final Logger LOG = LoggerFactory.getLogger(Dependency.class);

    private final Optional<ZosConnection> zosConnection;
    private final Optional<SshConnection> sshConnection;
    private final Optional<String> data;
    private final boolean toggle;
    private final OptionalLong timeout;

    public Dependency(Builder builder) {
        LOG.debug("*** Dependency ***");
        this.zosConnection = Optional.ofNullable(builder.zosConnection);
        this.sshConnection = Optional.ofNullable(builder.sshConnection);
        if (builder.data == null) {
            this.data = Optional.empty();
        } else {
            this.data = Optional.of(builder.data);
        }
        if (builder.timeout == 0) {
            this.timeout = OptionalLong.empty();
        } else {
            this.timeout = OptionalLong.of(builder.timeout);
        }
        this.toggle = builder.toggle;
    }

    @Override
    public boolean equals(Object o) {
        LOG.debug("*** equals ***");
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return toggle == that.toggle && Objects.equals(zosConnection, that.zosConnection) &&
                Objects.equals(sshConnection, that.sshConnection) && Objects.equals(data, that.data) &&
                Objects.equals(timeout, that.timeout);
    }

    @Override
    public int hashCode() {
        LOG.debug("*** hashCode ***");
        return Objects.hash(zosConnection, sshConnection, data, toggle, timeout);
    }

    public static class Builder {

        private ZosConnection zosConnection;
        private SshConnection sshConnection;
        private String data;
        private boolean toggle;
        private long timeout;

        public Builder zosConnection(final ZosConnection zosConnection) {
            this.zosConnection = zosConnection;
            return this;
        }

        public Builder sshConnection(final SshConnection sshConnection) {
            this.sshConnection = sshConnection;
            return this;
        }

        public Builder data(final String data) {
            this.data = data;
            return this;
        }

        public Builder toggle(final boolean toggle) {
            this.toggle = toggle;
            return this;
        }

        public Builder timeout(final long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Dependency build() {
            return new Dependency(this);
        }

    }

}
