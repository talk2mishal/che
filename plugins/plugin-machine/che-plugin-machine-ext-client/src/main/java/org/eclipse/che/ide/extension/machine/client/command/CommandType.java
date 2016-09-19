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

import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.Collection;

/**
 * The type of a command.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandType {

    /** Returns unique identifier for this command type. */
    String getId();

    /** Returns the display name of the command type. */
    String getDisplayName();

    /** Returns a short description what command of concrete type can do. */
    String getDescription();

    /** Returns the icon used to represent the command type. */
    SVGResource getIcon();

    /** Returns the {@link CommandConfigurationPage}s that allow to configure specific command parameters. */
    Collection<CommandConfigurationPage<? extends CommandConfiguration>> getConfigurationPages();

    /** Returns factory for {@link CommandConfiguration} instances. */
    CommandConfigurationFactory<? extends CommandConfiguration> getConfigurationFactory();

    /** Returns command template that will be used for newly created command. */
    String getCommandTemplate();

    /** Returns template for preview Url. */
    String getPreviewUrlTemplate();
}
