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
package org.eclipse.che.plugin.maven.client.command;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.ide.CommandLine;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

/**
 * Factory for {@link MavenCommandConfiguration} instances.
 *
 * @author Artem Zatsarynnyi
 */
public class MavenCommandConfigurationFactory extends CommandConfigurationFactory<MavenCommandConfiguration> {

    protected MavenCommandConfigurationFactory(CommandType commandType) {
        super(commandType);
    }

    private static boolean isMavenCommand(String commandLine) {
        return commandLine.startsWith("mvn");
    }

    @Override
    public MavenCommandConfiguration createFromDto(Command command) {
        if (!isMavenCommand(command.getCommandLine())) {
            throw new IllegalArgumentException("Not a valid Maven command: " + command.getCommandLine());
        }

        final MavenCommandConfiguration configuration =
                new MavenCommandConfiguration(getCommandType(), command.getName(), command.getAttributes());

        final CommandLine cmd = new CommandLine(command.getCommandLine());

        if (cmd.hasArgument("-f")) {
            final int index = cmd.indexOf("-f");
            final String workDir = cmd.getArgument(index + 1);
            configuration.setWorkingDirectory(workDir);

            cmd.removeArgument("-f");
            cmd.removeArgument(workDir);
        }

        cmd.removeArgument("mvn");
        configuration.setCommandLine(cmd.toString());

        return configuration;
    }
}
