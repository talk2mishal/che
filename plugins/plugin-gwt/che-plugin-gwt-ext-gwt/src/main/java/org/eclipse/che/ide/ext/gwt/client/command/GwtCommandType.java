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
package org.eclipse.che.ide.ext.gwt.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.gwt.client.GwtResources;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandImpl;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandProducer;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectPathProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.DevMachineHostNameProvider;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * GWT command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCommandType implements CommandType {

    public static final String COMMAND_TEMPLATE = "mvn clean gwt:run-codeserver";

    private static final String ID = "gwt";

    private final GwtResources                   resources;
    private final CurrentProjectPathProvider     currentProjectPathProvider;
    private final DevMachineHostNameProvider     devMachineHostNameProvider;
    private final GwtCommandConfigurationFactory configurationFactory;

    private final Collection<CommandConfigurationPage<? extends CommandImpl>> pages;

    @Inject
    public GwtCommandType(GwtResources resources,
                          GwtCommandPagePresenter page,
                          GwtCommandConfigurationFactory gwtCommandConfigurationFactory,
                          CurrentProjectPathProvider currentProjectPathProvider,
                          DevMachineHostNameProvider devMachineHostNameProvider,
                          IconRegistry iconRegistry) {
        this.resources = resources;
        this.currentProjectPathProvider = currentProjectPathProvider;
        this.devMachineHostNameProvider = devMachineHostNameProvider;
        configurationFactory = gwtCommandConfigurationFactory;
        pages = new LinkedList<>();
        pages.add(page);

        iconRegistry.registerIcon(new Icon(ID + ".commands.category.icon", resources.gwtCommandType()));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "GWT";
    }

    @Override
    public String getDescription() {
        return "Command for launching GWT Super Dev Mode";
    }

    @Override
    public SVGResource getIcon() {
        return resources.gwtCommandType();
    }

    @Override
    public Collection<CommandConfigurationPage<? extends CommandImpl>> getConfigurationPages() {
        return pages;
    }

    @Override
    public CommandConfigurationFactory<GwtCommandConfiguration> getConfigurationFactory() {
        return configurationFactory;
    }

    @Override
    public String getCommandTemplate() {
        return COMMAND_TEMPLATE + " -f " + currentProjectPathProvider.getKey() + " -Dgwt.bindAddress=" +
               devMachineHostNameProvider.getKey();
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
