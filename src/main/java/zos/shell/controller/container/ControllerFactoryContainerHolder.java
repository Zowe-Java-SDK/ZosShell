package zos.shell.controller.container;

public final class ControllerFactoryContainerHolder {

    private static final ControllerFactoryContainer INSTANCE = new ControllerFactoryContainer();

    public static ControllerFactoryContainer container() {
        return INSTANCE;
    }

}

