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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.ide.CommandLine;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectPathProvider;

/**
 * Factory for {@link MavenCommandConfiguration} instances.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MavenCommandConfigurationFactory implements CommandConfigurationFactory<MavenCommandConfiguration> {

    private final MavenCommandFactory        mavenCommandFactory;
    private final CurrentProjectPathProvider currentProjectPathProvider;

    @Inject
    public MavenCommandConfigurationFactory(MavenCommandFactory mavenCommandFactory,
                                            CurrentProjectPathProvider currentProjectPathProvider) {
        this.mavenCommandFactory = mavenCommandFactory;
        this.currentProjectPathProvider = currentProjectPathProvider;
    }

    private static boolean isMavenCommand(String commandLine) {
        return commandLine.startsWith("mvn");
    }

    @Override
    public MavenCommandConfiguration create(String name) {
        final MavenCommandConfiguration configuration = mavenCommandFactory.newCommand(name);
        configuration.setWorkingDirectory(currentProjectPathProvider.getKey());
        configuration.setCommandLine("clean install");

        return configuration;
    }

    @Override
    public MavenCommandConfiguration create(Command command) {
        if (!isMavenCommand(command.getCommandLine())) {
            throw new IllegalArgumentException("Not a valid Maven command: " + command.getCommandLine());
        }

        final MavenCommandConfiguration configuration = mavenCommandFactory.newCommand(command.getName());
        configuration.setAttributes(command.getAttributes());

        setUpFromCommandLine(configuration, command.getCommandLine());

        return configuration;
    }

    public void setUpFromCommandLine(MavenCommandConfiguration commandConfiguration, String commandLine) {
        final CommandLine cmd = new CommandLine(commandLine);

        if (cmd.hasArgument("-f")) {
            final int index = cmd.indexOf("-f");
            final String workDir = cmd.getArgument(index + 1);
            commandConfiguration.setWorkingDirectory(workDir);

            cmd.removeArgument("-f");
            cmd.removeArgument(workDir);
        }

        cmd.removeArgument("mvn");
        commandConfiguration.setCommandLine(cmd.toString());
    }
}
