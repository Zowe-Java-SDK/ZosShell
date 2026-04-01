package zos.shell.controller.factory;

import zos.shell.controller.factory.impl.ChangeControllerFactory;
import zos.shell.controller.factory.impl.DatasetControllerFactory;
import zos.shell.controller.factory.impl.JobControllerFactory;

public final class ControllerFactories {

    private static final ControllerFactoryContainer GLOBAL_FACTORY = new ControllerFactoryContainer();
    private static final ChangeControllerFactory CHANGE_FACTORY = new ChangeControllerFactory();
    private static final DatasetControllerFactory DATASET_FACTORY = new DatasetControllerFactory();
    private static final JobControllerFactory JOB_FACTORY = new JobControllerFactory();

    private ControllerFactories() {
    }

    public static ControllerFactoryContainer container() {
        return GLOBAL_FACTORY;
    }

    public static ChangeControllerFactory changeFactory() {
        return CHANGE_FACTORY;
    }

    public static DatasetControllerFactory datasetFactory() {
        return DATASET_FACTORY;
    }

    public static JobControllerFactory jobFactory() {
        return JOB_FACTORY;
    }

}
