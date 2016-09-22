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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandProducer;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandType;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Arbitrary command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CustomCommandType implements CommandType {

    private static final String ID               = "custom";
    private static final String COMMAND_TEMPLATE = "echo \"hello\"";

    private final MachineResources resources;

    private final List<CommandConfigurationPage> pages;

    @Inject
    public CustomCommandType(MachineResources resources, CustomPagePresenter page) {
        this.resources = resources;

        pages = new LinkedList<>();
        pages.add(page);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Custom";
    }

    @Override
    public String getDescription() {
        return "Arbitrary command";
    }

    @Override
    public SVGResource getIcon() {
        return resources.customCommandTypeSubElementIcon();
    }

    @Override
    public List<CommandConfigurationPage> getConfigurationPages() {
        return pages;
    }

    @Override
    public String getCommandTemplate() {
        return COMMAND_TEMPLATE;
    }

    @Override
    public List<CommandProducer> getProducers() {
        return Collections.emptyList();
    }

    @Override
    public String getPreviewUrlTemplate() {
        return "";
    }
}
