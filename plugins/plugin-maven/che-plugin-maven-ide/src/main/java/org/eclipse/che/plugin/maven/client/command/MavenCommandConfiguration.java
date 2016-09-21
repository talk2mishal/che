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
package org.eclipse.che.plugin.maven.client.command;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.extension.machine.client.command.AbstractCommandConfiguration;

/**
 * Represents Maven command.
 *
 * @author Artem Zatsarynnyi
 */
public class MavenCommandConfiguration extends AbstractCommandConfiguration {

    private String workingDirectory;
    private String commandLine;

    @Inject
    protected MavenCommandConfiguration(MavenCommandType type, @Assisted String name) {
        super(type, name, null);

        workingDirectory = "";
        commandLine = "";
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getCommandLine() {
        return commandLine;
    }

    /** Set command line, e.g. {@code [options] [<goal(s)>] [<phase(s)>]}. */
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    @Override
    public String toCommandLine() {
        final StringBuilder cmd = new StringBuilder("mvn");
        if (!workingDirectory.trim().isEmpty()) {
            cmd.append(" -f ").append(workingDirectory.trim());
        }
        if (!commandLine.trim().isEmpty()) {
            cmd.append(' ').append(commandLine.trim());
        }
        return cmd.toString();
    }
}
