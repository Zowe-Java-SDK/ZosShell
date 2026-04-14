package zos.shell.controller.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractControllerFactory<K> {

    private final Map<K, Object> controllers = new HashMap<>();

    public <T> T getOrCreateController(final K key,
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
