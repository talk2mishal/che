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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.ide.CommandLine;

/**
 * Factory for {@link JavaCommandConfiguration} instances.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaCommandConfigurationFactory implements CommandConfigurationFactory<JavaCommandConfiguration> {

    private final Provider<JavaCommandType> javaCommandTypeProvider;

    @Inject
    public JavaCommandConfigurationFactory(Provider<JavaCommandType> javaCommandTypeProvider) {
        // TODO: avoid getting through provider
        this.javaCommandTypeProvider = javaCommandTypeProvider;
    }

    @Override
    public JavaCommandConfiguration create(String name) {
        return null;
    }

    @Override
    public JavaCommandConfiguration create(JavaCommandConfiguration commandConfiguration) {
        return null;
    }

    @Override
    public JavaCommandConfiguration create(Command command) {
        // TODO: need better way of creating
        final JavaCommandConfiguration configuration = new JavaCommandConfiguration(javaCommandTypeProvider.get(),
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
