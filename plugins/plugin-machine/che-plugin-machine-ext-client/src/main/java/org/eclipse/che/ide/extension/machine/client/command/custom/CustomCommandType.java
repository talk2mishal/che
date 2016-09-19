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
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Arbitrary command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CustomCommandType implements CommandType {

    private static final String ID               = "custom";
    private static final String COMMAND_TEMPLATE = "echo \"hello\"";

    private final MachineResources                  resources;
    private final CustomCommandConfigurationFactory configurationFactory;

    private final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages;

    @Inject
    public CustomCommandType(MachineResources resources, CustomPagePresenter page) {
        this.resources = resources;
        configurationFactory = new CustomCommandConfigurationFactory(this);
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
    public Collection<CommandConfigurationPage<? extends CommandConfiguration>> getConfigurationPages() {
        return pages;
    }

    @Override
    public CommandConfigurationFactory<CustomCommandConfiguration> getConfigurationFactory() {
        return configurationFactory;
    }

    @Override
    public String getCommandTemplate() {
        return COMMAND_TEMPLATE;
    }

    @Override
    public String getPreviewUrlTemplate() {
        return "";
    }
}
