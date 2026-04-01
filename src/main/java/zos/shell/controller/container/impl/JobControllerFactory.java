package zos.shell.controller.container.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.container.type.JobControllerType;
import zos.shell.controller.dependency.AbstractDependencyController;
import zos.shell.controller.dependency.Dependency;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class JobControllerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JobControllerFactory.class);

    private final Map<JobControllerType.Name, Object> controllers = new HashMap<>();

    public <T extends AbstractDependencyController> T getOrCreateController(
            final JobControllerType.Name key,
            final Class<T> controllerClass,
            final Dependency dependency,
            final Supplier<T> creator) {

        T controller = controllerClass.cast(this.controllers.get(key));
        if (controller != null && !controller.isNotValid(dependency)) {
            return controller;
        }

        final T newController = creator.get();
        this.controllers.put(key, newController);
        return newController;
    }

}
