/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.command;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProviderRegistry;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Implementation of {@link CommandManager}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandManagerImpl implements CommandManager {

    public static final String PREVIEW_URL_ATTR = "previewUrl";

    private final CommandTypeRegistry                  commandTypeRegistry;
    private final AppContext                           appContext;
    private final WorkspaceServiceClient               workspaceServiceClient;
    private final MachineServiceClient                 machineServiceClient;
    private final DtoFactory                           dtoFactory;
    private final CommandPropertyValueProviderRegistry commandPropertyValueProviderRegistry;
    private final NotificationManager                  notificationManager;
    private final MachineLocalizationConstant          localizationConstants;
    private final CommandConsoleFactory                commandConsoleFactory;
    private final ProcessesPanelPresenter              processesPanelPresenter;

    private final Map<String, CommandImpl>    commands;
    private final Set<CommandChangedListener> commandChangedListeners;

    @Inject
    public CommandManagerImpl(CommandTypeRegistry commandTypeRegistry,
                              AppContext appContext,
                              WorkspaceServiceClient workspaceServiceClient,
                              MachineServiceClient machineServiceClient,
                              DtoFactory dtoFactory,
                              EventBus eventBus,
                              CommandPropertyValueProviderRegistry commandPropertyValueProviderRegistry,
                              NotificationManager notificationManager,
                              MachineLocalizationConstant localizationConstants,
                              CommandConsoleFactory commandConsoleFactory,
                              ProcessesPanelPresenter processesPanelPresenter) {
        this.commandTypeRegistry = commandTypeRegistry;
        this.appContext = appContext;
        this.workspaceServiceClient = workspaceServiceClient;
        this.machineServiceClient = machineServiceClient;
        this.dtoFactory = dtoFactory;
        this.commandPropertyValueProviderRegistry = commandPropertyValueProviderRegistry;
        this.notificationManager = notificationManager;
        this.localizationConstants = localizationConstants;
        this.commandConsoleFactory = commandConsoleFactory;
        this.processesPanelPresenter = processesPanelPresenter;

        commands = new HashMap<>();
        commandChangedListeners = new HashSet<>();

        eventBus.addHandler(WorkspaceStartedEvent.TYPE, new WorkspaceStartedEvent.Handler() {
            @Override
            public void onWorkspaceStarted(WorkspaceStartedEvent event) {
                retrieveAllCommands();
            }
        });
    }

    private void retrieveAllCommands() {
        workspaceServiceClient.getCommands(appContext.getWorkspaceId()).then(new Operation<List<CommandDto>>() {
            @Override
            public void apply(List<CommandDto> arg) throws OperationException {
                for (Command command : arg) {
                    commands.put(command.getName(), new CommandImpl(command));
                }
            }
        });
    }

    @Override
    public List<CommandImpl> getCommands() {
        return new ArrayList<>(commands.values());
    }

    @Override
    public Promise<CommandImpl> create(String type) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(type);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(PREVIEW_URL_ATTR, commandType.getPreviewUrlTemplate());

        final CommandImpl command = new CommandImpl(getUniqueCommandName(type, null),
                                                    commandType.getCommandTemplate(),
                                                    type,
                                                    attributes);
        return add(command);
    }

    @Override
    public Promise<CommandImpl> create(String name, String commandLine, String type, Map<String, String> attributes) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(type);

        Map<String, String> attr = (attributes != null) ? attributes : new HashMap<String, String>();
        attr.put(PREVIEW_URL_ATTR, commandType.getPreviewUrlTemplate());

        final CommandImpl command = new CommandImpl(getUniqueCommandName(type, name),
                                                    commandLine != null ? commandLine : commandType.getCommandTemplate(),
                                                    type,
                                                    attr);
        return add(command);
    }

    private Promise<CommandImpl> add(final CommandImpl command) {
        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName(getUniqueCommandName(command.getType(), command.getName()))
                                                .withCommandLine(command.getCommandLine())
                                                .withType(command.getType())
                                                .withAttributes(command.getAttributes());

        return workspaceServiceClient.addCommand(appContext.getWorkspaceId(), commandDto).then(new Function<WorkspaceDto, CommandImpl>() {
            @Override
            public CommandImpl apply(WorkspaceDto arg) throws FunctionException {
                final CommandImpl newCommand = new CommandImpl(commandDto.getName(),
                                                               command.getCommandLine(),
                                                               command.getType(),
                                                               command.getAttributes());
                commands.put(newCommand.getName(), newCommand);

                fireCommandAdded(newCommand);

                return newCommand;
            }
        });
    }

    @Override
    public Promise<CommandImpl> update(final String commandName, final CommandImpl command) {
        final String name;
        if (commandName.equals(command.getName())) {
            name = commandName;
        } else {
            name = getUniqueCommandName(command.getType(), command.getName());
        }

        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName(name)
                                                .withCommandLine(command.getCommandLine())
                                                .withType(command.getType())
                                                .withAttributes(command.getAttributes());

        return workspaceServiceClient.updateCommand(appContext.getWorkspaceId(), commandName, commandDto)
                                     .then(new Function<WorkspaceDto, CommandImpl>() {
                                         @Override
                                         public CommandImpl apply(WorkspaceDto arg) throws FunctionException {
                                             final CommandImpl updatedCommand = new CommandImpl(commandDto.getName(),
                                                                                                command.getCommandLine(),
                                                                                                command.getType(),
                                                                                                command.getAttributes());
                                             commands.remove(commandName);
                                             commands.put(updatedCommand.getName(), updatedCommand);

                                             fireCommandUpdated(updatedCommand);

                                             return updatedCommand;
                                         }
                                     });
    }

    @Override
    public Promise<Void> remove(final String name) {
        return workspaceServiceClient.deleteCommand(appContext.getWorkspaceId(), name).then(new Function<WorkspaceDto, Void>() {
            @Override
            public Void apply(WorkspaceDto arg) throws FunctionException {
                fireCommandRemoved(commands.remove(name));
                return null;
            }
        });
    }

    @Override
    public List<CommandPage> getPages(String type) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(type);

        return commandType.getPages();
    }

    @Override
    public List<CommandProducer> getApplicableProducers() {
        List<CommandProducer> producers = new ArrayList<>();

        for (CommandType commandType : commandTypeRegistry.getCommandTypes()) {
            for (CommandProducer commandProducer : commandType.getProducers()) {
//                if (commandProducer.isApplicable()) {
                producers.add(commandProducer);
//                }
            }
        }

        return producers;
    }

    @Override
    public void executeCommand(final CommandImpl command, final Machine machine) {
        if (machine == null) {
            notificationManager.notify(localizationConstants.failedToExecuteCommand(),
                                       localizationConstants.noDevMachine(),
                                       FAIL,
                                       FLOAT_MODE);
            return;
        }

        final String outputChannel = "process:output:" + UUID.uuid();

        final CommandOutputConsole console = commandConsoleFactory.create(command, machine);
        console.listenToOutput(outputChannel);
        processesPanelPresenter.addCommandOutput(machine.getId(), console);

        substituteProperties(command.getCommandLine()).then(new Operation<String>() {
            @Override
            public void apply(String arg) throws OperationException {
                Promise<MachineProcessDto> processPromise = machineServiceClient.executeCommand(machine.getWorkspaceId(),
                                                                                                machine.getId(),
                                                                                                command,
                                                                                                outputChannel);
                processPromise.then(new Operation<MachineProcessDto>() {
                    @Override
                    public void apply(MachineProcessDto process) throws OperationException {
                        console.attachToProcess(process);
                    }
                });
            }
        });
    }

    @Override
    public Promise<String> substituteProperties(String commandLine) {
        Promise<String> promise = Promises.resolve(null);
        CommandLineContainer commandLineContainer = new CommandLineContainer(commandLine);
        return replaceParameters(promise, commandLineContainer, commandPropertyValueProviderRegistry.getProviders().iterator());
    }


    private Promise<String> replaceParameters(Promise<String> promise,
                                              CommandLineContainer commandLineContainer,
                                              Iterator<CommandPropertyValueProvider> iterator) {
        if (!iterator.hasNext()) {
            return promise;
        }

        final CommandPropertyValueProvider provider = iterator.next();

        Promise<String> derivedPromise = promise.thenPromise(proceedRefactoringMove(commandLineContainer, provider));

        return replaceParameters(derivedPromise, commandLineContainer, iterator);
    }

    private Function<String, Promise<String>> proceedRefactoringMove(final CommandLineContainer commandLineContainer,
                                                                     final CommandPropertyValueProvider provider) {
        return new Function<String, Promise<String>>() {
            @Override
            public Promise<String> apply(String arg) throws FunctionException {
                return provider.getValue().thenPromise(new Function<String, Promise<String>>() {
                    @Override
                    public Promise<String> apply(String arg) throws FunctionException {
                        commandLineContainer.setCommandLine(commandLineContainer.getCommandLine().replace(provider.getKey(), arg));
                        return Promises.resolve(commandLineContainer.getCommandLine());
                    }
                });
            }
        };
    }

    @Override
    public void addCommandChangedListener(CommandChangedListener listener) {
        commandChangedListeners.add(listener);
    }

    @Override
    public void removeCommandChangedListener(CommandChangedListener listener) {
        commandChangedListeners.remove(listener);
    }

    private void fireCommandAdded(CommandImpl command) {
        for (CommandChangedListener listener : commandChangedListeners) {
            listener.onCommandAdded(command);
        }
    }

    private void fireCommandRemoved(CommandImpl command) {
        for (CommandChangedListener listener : commandChangedListeners) {
            listener.onCommandRemoved(command);
        }
    }

    private void fireCommandUpdated(CommandImpl command) {
        for (CommandChangedListener listener : commandChangedListeners) {
            listener.onCommandUpdated(command);
        }
    }

    private String getUniqueCommandName(String customType, String customName) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(customType);
        final Set<String> commandNames = commands.keySet();

        final String newCommandName;

        if (customName == null || customName.isEmpty()) {
            newCommandName = "new" + commandType.getDisplayName();
        } else {
            if (!commandNames.contains(customName)) {
                return customName;
            }
            newCommandName = customName + " copy";
        }

        if (!commandNames.contains(newCommandName)) {
            return newCommandName;
        }

        for (int count = 1; count < 1000; count++) {
            if (!commandNames.contains(newCommandName + "-" + count)) {
                return newCommandName + "-" + count;
            }
        }

        return newCommandName;
    }

    private class CommandLineContainer {
        private String commandLine;

        CommandLineContainer(String commandLine) {
            this.commandLine = commandLine;
        }

        public String getCommandLine() {
            return commandLine;
        }

        public void setCommandLine(String commandLine) {
            this.commandLine = commandLine;
        }
    }
}
