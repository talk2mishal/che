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

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandImpl;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandProducer;

import java.util.List;
import java.util.Map;

/**
 * Facade for command related operations.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandManager {

    List<CommandImpl> getCommands();

    Promise<CommandImpl> create(String type);

    Promise<CommandImpl> create(String name, String commandLine, String type, Map<String, String> attributes);

    /**
     * Returns Command because it's name may be different from the original name.
     * Note that updated command's name may differ from the original name.
     */
    Promise<CommandImpl> update(String commandName, CommandImpl command);

    Promise<Void> remove(String commandName);

    List<CommandConfigurationPage> getPages(String type);

    List<CommandProducer> getApplicableProducers();

    /**
     * Executes the given {@code command} on the specified {@code machine}.
     *
     * @param command
     *         command to execute
     * @param machine
     *         machine to execute the command
     */
    void executeCommand(CommandImpl command, Machine machine);

    /**
     * Substitutes all properties with the appropriate values in the given {@code commandLine}.
     *
     * @see CommandPropertyValueProvider
     */
    Promise<String> substituteProperties(String commandLine);

    void addCommandChangedListener(CommandChangedListener listener);

    void removeCommandChangedListener(CommandChangedListener listener);

    /** Listener that will be called when command changed. */
    interface CommandChangedListener {
        void onCommandAdded(CommandImpl command);

        void onCommandRemoved(CommandImpl command);

        void onCommandUpdated(CommandImpl command);
    }
}
