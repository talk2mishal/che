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
package org.eclipse.che.ide.ext.gwt.client.command;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.ide.CommandLine;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

/**
 * Factory for {@link GwtCommandConfiguration} instances.
 *
 * @author Artem Zatsarynnyi
 */
public class GwtCommandConfigurationFactory extends CommandConfigurationFactory<GwtCommandConfiguration> {

    protected GwtCommandConfigurationFactory(CommandType commandType) {
        super(commandType);
    }

    private static boolean isGwtCommand(String commandLine) {
        return commandLine.startsWith(GwtCommandType.COMMAND_TEMPLATE);
    }

    @Override
    public GwtCommandConfiguration createFromDto(Command command) {
        if (!isGwtCommand(command.getCommandLine())) {
            throw new IllegalArgumentException("Not a valid GWT command: " + command.getCommandLine());
        }

        final GwtCommandConfiguration configuration = new GwtCommandConfiguration(getCommandType(),
                                                                                  command.getName(),
                                                                                  command.getAttributes());
        final CommandLine cmd = new CommandLine(command.getCommandLine());

        if (cmd.hasArgument("-f")) {
            final int index = cmd.indexOf("-f");
            final String workDir = cmd.getArgument(index + 1);
            configuration.setWorkingDirectory(workDir);
        }

        for (String arg : cmd.getArguments()) {
            if (arg.startsWith("-Dgwt.module=")) {
                configuration.setGwtModule(arg.split("=")[1]);
            } else if (arg.startsWith("-Dgwt.bindAddress=")) {
                configuration.setCodeServerAddress(arg.split("=")[1]);
            }
        }

        return configuration;
    }
}
