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
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandImpl;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandProducer;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link CommandConfigurationManager}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandManagerImpl implements CommandConfigurationManager {

    public static final String PREVIEW_URL_ATTR = "previewUrl";

    private final CommandTypeRegistry    commandTypeRegistry;
    private final AppContext             appContext;
    private final WorkspaceServiceClient workspaceServiceClient;
    private final DtoFactory             dtoFactory;

    private Map<String, CommandImpl> commands;

    @Inject
    public CommandManagerImpl(CommandTypeRegistry commandTypeRegistry,
                              AppContext appContext,
                              WorkspaceServiceClient workspaceServiceClient,
                              DtoFactory dtoFactory,
                              EventBus eventBus) {
        this.commandTypeRegistry = commandTypeRegistry;
        this.appContext = appContext;
        this.workspaceServiceClient = workspaceServiceClient;
        this.dtoFactory = dtoFactory;

        commands = new HashMap<>();

        eventBus.addHandler(WorkspaceStartedEvent.TYPE, new WorkspaceStartedEvent.Handler() {
            @Override
            public void onWorkspaceStarted(WorkspaceStartedEvent event) {
                retrieveAllCommands();
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

                return newCommand;
            }
        });
    }

    @Override
    public Promise<CommandImpl> update(final String commandName, final CommandImpl command) {
        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName(getUniqueCommandName(command.getType(), command.getName()))
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

                                             return updatedCommand;
                                         }
                                     });
    }

    @Override
    public Promise<Void> remove(final String commandName) {
        return workspaceServiceClient.deleteCommand(appContext.getWorkspaceId(), commandName).then(new Function<WorkspaceDto, Void>() {
            @Override
            public Void apply(WorkspaceDto arg) throws FunctionException {
                commands.remove(commandName);
                return null;
            }
        });
    }

    @Override
    public List<CommandConfigurationPage> getPages(String type) {
        final CommandType commandType = commandTypeRegistry.getCommandTypeById(type);

        return commandType.getConfigurationPages();
    }

    @Override
    public List<CommandProducer> getApplicableProducers() {
        List<CommandProducer> producers = new ArrayList<>();

        for (CommandType commandType : commandTypeRegistry.getCommandTypes()) {
            for (CommandProducer commandProducer : commandType.getProducers()) {
                if (commandProducer.isApplicable()) {
                    producers.add(commandProducer);
                }
            }
        }

        return producers;
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
}
