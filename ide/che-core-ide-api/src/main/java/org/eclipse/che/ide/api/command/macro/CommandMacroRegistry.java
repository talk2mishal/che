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
package org.eclipse.che.ide.api.command.macro;

import java.util.List;
import java.util.Set;

/**
 * Registry for {@link CommandMacro}s.
 *
 * @author Artem Zatsarynnyi
 * @see CommandMacro
 */
public interface CommandMacroRegistry {

    /** Register set of property value providers. */
    void register(Set<CommandMacro> valueProviders);

    /** Unregister specific property value provider. */
    void unregister(CommandMacro valueProvider);

    /** Returns keys of all registered {@link CommandMacro}s. */
    Set<String> getKeys();

    /** Returns {@link CommandMacro} by the given key. */
    CommandMacro getProvider(String key);

    /** Returns all registered {@link CommandMacro}s. */
    List<CommandMacro> getProviders();
}
