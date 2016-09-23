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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationManager;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandImpl;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandProducer;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;

import java.util.Collections;

@Singleton
public class ProdAction extends AbstractPerspectiveAction {

    private final CommandConfigurationManager commandConfigurationManager;

    private final CommandProducer commandProducer;

    @Inject
    public ProdAction(CommandConfigurationManager commandConfigurationManager) {
        super(Collections.singletonList(ProjectPerspective.PROJECT_PERSPECTIVE_ID), "prod", "", null, null);
        this.commandConfigurationManager = commandConfigurationManager;

        commandProducer = commandConfigurationManager.getApplicableProducers().get(0);
    }

    @Override
    public void updateInPerspective(ActionEvent event) {
        event.getPresentation().setEnabledAndVisible(commandProducer.isApplicable());

        event.getPresentation().setText(commandProducer.getName());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        CommandImpl command = commandProducer.createCommand();

        commandConfigurationManager.create(command.getName(),
                                           command.getCommandLine(),
                                           command.getType(),
                                           command.getAttributes());
    }
}
