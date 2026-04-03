package zos.shell.controller.factory.impl;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.*;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.factory.AbstractDependencyControllerFactory;
import zos.shell.controller.factory.type.DatasetControllerType;
import zos.shell.service.checksum.CheckSumService;
import zos.shell.service.dsn.concat.ConcatService;
import zos.shell.service.dsn.copy.CopyService;
import zos.shell.service.dsn.count.CountService;
import zos.shell.service.dsn.delete.DeleteService;
import zos.shell.service.dsn.download.*;
import zos.shell.service.dsn.edit.EditService;
import zos.shell.service.dsn.list.ListingService;
import zos.shell.service.dsn.makedir.MakeDirService;
import zos.shell.service.dsn.save.SaveService;
import zos.shell.service.dsn.touch.TouchService;
import zos.shell.service.env.EnvVariableService;
import zos.shell.service.grep.GrepService;
import zos.shell.service.path.PathService;
import zos.shell.service.rename.RenameService;
import zos.shell.singleton.ConnSingleton;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

public class DatasetControllerFactory extends AbstractDependencyControllerFactory<DatasetControllerType.Name> {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetControllerFactory.class);


    public ConcatController getConcatController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getConcatController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.CONCAT,
                ConcatController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var pathService = new PathService(ConnSingleton.getInstance(), envVariableController);
                    var dsnGet = new DsnGet(connection);
                    var download = new Download(dsnGet, pathService, false);
                    var concatService = new ConcatService(download, timeout);
                    return new ConcatController(concatService, dependency);
                }
        );
    }

    public CopyController getCopyController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCopyController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.COPY,
                CopyController.class,
                dependency,
                () -> new CopyController(
                        new CopyService(connection, timeout),
                        dependency
                )
        );
    }

    public CountController getCountController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getCountController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.COUNT,
                CountController.class,
                dependency,
                () -> new CountController(
                        new CountService(new DsnList(connection), timeout),
                        dependency
                )
        );
    }

    public DeleteController getDeleteController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getDeleteController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.DELETE,
                DeleteController.class,
                dependency,
                () -> new DeleteController(
                        new DeleteService(connection, timeout),
                        dependency
                )
        );
    }

    public RenameController getRenameController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getRenameController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.RENAME,
                RenameController.class,
                dependency,
                () -> new RenameController(
                        new RenameService(connection, timeout),
                        dependency
                )
        );
    }

    public SaveController getSaveController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getSaveController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.SAVE,
                SaveController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var pathService = new PathService(ConnSingleton.getInstance(), envVariableController);
                    var checkSumService = new CheckSumService();
                    var dsnWrite = new DsnWrite(connection);
                    return new SaveController(
                            new SaveService(dsnWrite, pathService, checkSumService, timeout),
                            dependency
                    );
                }
        );
    }

    public DownloadDsnController getDownloadDsnController(final ZosConnection connection,
                                                          final boolean isBinary,
                                                          final long timeout) {
        LOG.debug("*** getDownloadDsnController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .toggle(isBinary)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.DOWNLOAD,
                DownloadDsnController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var pathService = new PathService(ConnSingleton.getInstance(), envVariableController);
                    var downloadMemberService = new DownloadMemberService(
                            connection,
                            pathService,
                            isBinary,
                            timeout
                    );
                    var downloadPdsMemberService = new DownloadPdsMemberService(
                            connection,
                            pathService,
                            isBinary,
                            timeout
                    );
                    var downloadSeqDatasetService = new DownloadSeqDatasetService(
                            connection,
                            pathService,
                            isBinary,
                            timeout
                    );
                    var downloadMembersService = new DownloadMembersService(
                            connection,
                            new DownloadMemberListService(connection, isBinary, timeout),
                            timeout
                    );
                    return new DownloadDsnController(
                            downloadMemberService,
                            downloadPdsMemberService,
                            downloadSeqDatasetService,
                            downloadMembersService,
                            envVariableController,
                            dependency
                    );
                }
        );
    }

    public EditController getEditController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getEditController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.EDIT,
                EditController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var pathService = new PathService(ConnSingleton.getInstance(), envVariableController);
                    var checkSumService = new CheckSumService();
                    var dsnGet = new DsnGet(connection);
                    var download = new Download(dsnGet, pathService, false);
                    var editService = new EditService(download, pathService, checkSumService, timeout);
                    return new EditController(editService, dependency);
                }
        );
    }

    public GrepController getGrepController(final ZosConnection connection,
                                            final String target,
                                            final long timeout) {
        LOG.debug("*** getGrepController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .data(target)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.GREP,
                GrepController.class,
                dependency,
                () -> {
                    var envVariableController = new EnvVariableController(new EnvVariableService());
                    var pathService = new PathService(ConnSingleton.getInstance(), envVariableController);
                    var grepService = new GrepService(connection, pathService, target, timeout);
                    return new GrepController(grepService, dependency);
                }
        );
    }

    public ListingController getListingController(final ZosConnection connection,
                                                  final TextTerminal<?> terminal,
                                                  final long timeout) {
        LOG.debug("*** getListingController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.LIST,
                ListingController.class,
                dependency,
                () -> {
                    var dsnList = new DsnList(connection);
                    var listingService = new ListingService(terminal, dsnList, timeout);
                    return new ListingController(listingService, dependency);
                }
        );
    }

    public MakeDirController getMakeDirectoryController(final ZosConnection connection,
                                                        final TextTerminal<?> terminal,
                                                        final long timeout) {
        LOG.debug("*** getMakeDirectoryController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.MAKE_DIR,
                MakeDirController.class,
                dependency,
                () -> {
                    var dsnCreate = new DsnCreate(connection);
                    var makeDirService = new MakeDirService(dsnCreate, timeout);
                    return new MakeDirController(terminal, makeDirService, dependency);
                }
        );
    }

    public TouchController getTouchController(final ZosConnection connection, final long timeout) {
        LOG.debug("*** getTouchController ***");
        var dependency = new Dependency.Builder()
                .zosConnection(connection)
                .timeout(timeout)
                .build();

        return this.getOrCreateController(
                DatasetControllerType.Name.TOUCH,
                TouchController.class,
                dependency,
                () -> {
                    var dsnWrite = new DsnWrite(connection);
                    var dsnList = new DsnList(connection);
                    var touchService = new TouchService(dsnWrite, dsnList, timeout);
                    return new TouchController(touchService, dependency);
                }
        );
    }

}
