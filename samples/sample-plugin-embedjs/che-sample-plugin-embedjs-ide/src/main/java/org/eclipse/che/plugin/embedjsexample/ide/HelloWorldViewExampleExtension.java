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
package org.eclipse.che.plugin.embedjsexample.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.embedjsexample.ide.action.HelloWorldAction;

/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
@Extension(title = "Hello world from JavaScript example")
@Singleton
public class HelloWorldViewExampleExtension {

    @Inject
    private void configureActions(final ActionManager actionManager,
                                  final HelloWorldAction helloWorldAction) {

        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        DefaultActionGroup jsGroup = new DefaultActionGroup("JavaScript View Example", true, actionManager);
        mainContextMenuGroup.add(jsGroup);

        actionManager.registerAction(helloWorldAction.ACTION_ID, helloWorldAction);
        jsGroup.addAction(helloWorldAction);
    }

}
