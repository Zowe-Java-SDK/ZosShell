package zos.shell.controller.factory;

import zos.shell.controller.factory.impl.*;

public final class ControllerFactories {

    private static final GlobalControllerFactory GLOBAL_FACTORY = new GlobalControllerFactory();
    private static final ChangeControllerFactory CHANGE_FACTORY = new ChangeControllerFactory();
    private static final DatasetControllerFactory DATASET_FACTORY = new DatasetControllerFactory();
    private static final JobControllerFactory JOB_FACTORY = new JobControllerFactory();
    private static final TsoControllerFactory TSO_FACTORY = new TsoControllerFactory();
    private static final ConsoleControllerFactory CONSOLE_FACTORY = new ConsoleControllerFactory();
    private static final UssControllerFactory USS_FACTORY = new UssControllerFactory();

    private ControllerFactories() {
    }

    public static GlobalControllerFactory getGlobalFactory() {
        return GLOBAL_FACTORY;
    }

    public static ChangeControllerFactory getChangeFactory() {
        return CHANGE_FACTORY;
    }

    public static DatasetControllerFactory getDatasetFactory() {
        return DATASET_FACTORY;
    }

    public static JobControllerFactory getJobFactory() {
        return JOB_FACTORY;
    }

    public static TsoControllerFactory getTsoFactory() {
        return TSO_FACTORY;
    }

    public static ConsoleControllerFactory getConsoleFactory() {
        return CONSOLE_FACTORY;
    }

    public static UssControllerFactory getUssFactory() {
        return USS_FACTORY;
    }

}
