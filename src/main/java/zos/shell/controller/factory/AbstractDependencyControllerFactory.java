package zos.shell.controller.factory;

import zos.shell.controller.dependency.AbstractDependencyController;
import zos.shell.controller.dependency.Dependency;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractDependencyControllerFactory<K> {

    private final Map<K, Object> controllers = new HashMap<>();

    public <T extends AbstractDependencyController> T getOrCreateController(
            final K key,
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
