package zos.shell.controller.dependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDependencyController.class);

    private final Dependency dependency;

    protected AbstractDependencyController(Dependency dependency) {
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
