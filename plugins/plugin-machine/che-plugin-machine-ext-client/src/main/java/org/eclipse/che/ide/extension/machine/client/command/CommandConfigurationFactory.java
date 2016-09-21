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

import org.eclipse.che.api.core.model.machine.Command;

/**
 * Factory for {@link CommandConfiguration} instances.
 *
 * @param <T>
 *         type of the command configuration which this factory produces
 * @author Artem Zatsarynnyi
 */
public interface CommandConfigurationFactory<T extends CommandConfiguration> {

    /**
     * Creates a new 'empty' command with the given name.
     * <p>Called when user tries to create new command in 'Commands' dialog.
     *
     * @return a new 'empty' command
     */
    T create(String name);

    /** Creates duplicate of the given {@code commandConfiguration}. */
    T create(T commandConfiguration);

    /**
     * Creates a new command based on the given model object.
     * <p>Called for instantiating command from the saved state.
     * <p>Typically, should create a new command and set it up from the given {@link Command} object.
     *
     * @param command
     *         {@link Command} model
     * @return a new command based on the given {@link Command} object
     * @throws IllegalArgumentException
     *         if the {@code command} represents not a suitable command
     */
    T create(Command command);
}
