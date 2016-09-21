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
package org.eclipse.che.ide.extension.machine.client.command.custom;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;

/**
 * Factory for {@link CustomCommandConfiguration} instances.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CustomCommandConfigurationFactory implements CommandConfigurationFactory<CustomCommandConfiguration> {

    @Override
    public CustomCommandConfiguration create(String name) {
        final CustomCommandConfiguration commandConfiguration = new CustomCommandConfiguration(null, name, null);
        commandConfiguration.setCommandLine("echo \"hello\"");

        return commandConfiguration;
    }

    @Override
    public CustomCommandConfiguration create(CustomCommandConfiguration commandConfiguration) {
        return null;
    }

    @Override
    public CustomCommandConfiguration create(Command command) {
        final CustomCommandConfiguration configuration = new CustomCommandConfiguration(null, command.getName(), null);
        configuration.setCommandLine(command.getCommandLine());

        return configuration;
    }
}
