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
package org.eclipse.che.ide.ext.java.client.command;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.ide.CommandLine;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

/**
 * Factory for {@link JavaCommandConfiguration} instances.
 *
 * @author Valeriy Svydenko
 */
public class JavaCommandConfigurationFactory extends CommandConfigurationFactory<JavaCommandConfiguration> {

    protected JavaCommandConfigurationFactory(CommandType commandType) {
        super(commandType);
    }

    @Override
    public JavaCommandConfiguration createFromDto(Command command) {
        final JavaCommandConfiguration configuration = new JavaCommandConfiguration(getCommandType(),
                                                                                    command.getName(),
                                                                                    command.getAttributes());

        final CommandLine cmd = new CommandLine(command.getCommandLine());

        if (cmd.hasArgument("-d")) {
            int index = cmd.indexOf("-d");
            final String mainClass = cmd.getArgument(index + 2);
            configuration.setMainClass(mainClass);
            configuration.setMainClassFqn(cmd.getArgument(cmd.getArguments().size() - 1));
        }

        configuration.setCommandLine(command.getCommandLine());
        return configuration;
    }
}
