package zos.shell.controller.container.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.container.type.GlobalControllerType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GlobalControllerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerFactory.class);

    private final Map<GlobalControllerType.Name, Object> controllers = new HashMap<>();

    public <T> T getOrCreateController(final GlobalControllerType.Name key,
                                       final Class<T> controllerClass,
                                       final Supplier<T> creator) {

        var controller = controllerClass.cast(this.controllers.get(key));
        if (controller != null) {
            return controller;
        }

        T newController = creator.get();
        this.controllers.put(key, newController);
        return newController;
    }

}
