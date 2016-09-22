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
package org.eclipse.che.ide.extension.machine.client.command.api;

import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.List;

/**
 * Used for registering new command type and providing all the necessary
 * information and components for working with the appropriate command.
 * <p>Implementations of this interface need to be registered using
 * a multibinder in order to be picked-up on application's start-up.
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

    /** Returns the icon for displaying in the UI. */
    SVGResource getIcon();

    /** Returns the {@link CommandConfigurationPage}s that allow to edit commands of concrete type. */
    List<CommandConfigurationPage> getConfigurationPages();

    String getCommandTemplate();

    /** Returns the {@link CommandProducer}s that can produce commands of concrete type from the current context. */
    List<? extends CommandProducer> getProducers();

    /** Returns template for preview Url. */
    String getPreviewUrlTemplate();
}
