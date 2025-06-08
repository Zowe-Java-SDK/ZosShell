package zos.shell.controller.dependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(DependencyController.class);

    private final Dependency dependency;

    protected DependencyController(Dependency dependency) {
        LOG.debug("*** DependencyController ***");
        this.dependency = dependency;
    }

    public boolean isNotValid(Dependency dependency) {
        LOG.debug("*** isNotValid ***");
        if (this.dependency == null) {
            return true;
        }
        return !this.dependency.equals(dependency);
    }

}
