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

import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.gwt.client.GwtResources;
import org.eclipse.che.ide.extension.machine.client.command.macros.CurrentProjectPathProvider;
import org.eclipse.che.ide.extension.machine.client.command.macros.DevMachineHostNameProvider;

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

    private final CurrentProjectPathProvider currentProjectPathProvider;
    private final DevMachineHostNameProvider devMachineHostNameProvider;

    private final List<CommandPage> pages;

    @Inject
    public GwtCommandType(GwtResources resources,
                          GwtCommandPagePresenter page,
                          CurrentProjectPathProvider currentProjectPathProvider,
                          DevMachineHostNameProvider devMachineHostNameProvider,
                          IconRegistry iconRegistry) {
        this.currentProjectPathProvider = currentProjectPathProvider;
        this.devMachineHostNameProvider = devMachineHostNameProvider;
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
    public List<CommandPage> getPages() {
        return pages;
    }

    @Override
    public String getCommandLineTemplate() {
        return COMMAND_TEMPLATE + " -f " + currentProjectPathProvider.getKey() + " -Dgwt.bindAddress=" +
               devMachineHostNameProvider.getKey();
    }

    @Override
    public String getPreviewUrlTemplate() {
        return "";
    }
}
