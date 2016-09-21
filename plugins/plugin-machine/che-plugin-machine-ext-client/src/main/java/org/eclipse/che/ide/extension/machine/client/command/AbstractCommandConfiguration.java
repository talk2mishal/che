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

import org.eclipse.che.commons.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract {@link CommandConfiguration}.
 *
 * @author Artem Zatsarynnyi
 */
public abstract class AbstractCommandConfiguration implements CommandConfiguration {

    private final CommandType         type;
    private       String              name;
    private       Map<String, String> attributes;

    /**
     * Creates new command configuration of the specified type with the given name.
     *
     * @param type
     *         type of the command
     * @param name
     *         command name
     * @param attributes
     *         command attributes
     */
    protected AbstractCommandConfiguration(CommandType type, String name, @Nullable Map<String, String> attributes) {
        this.type = type;
        this.name = name;
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public CommandType getType() {
        return type;
    }

    @Override
    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        return attributes;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CommandConfiguration)) {
            return false;
        }

        CommandConfiguration other = (CommandConfiguration)o;

        return Objects.equals(getName(), other.getName())
               && Objects.equals(getType().getId(), other.getType().getId())
               && Objects.equals(toCommandLine(), other.toCommandLine())
               && Objects.equals(getAttributes(), other.getAttributes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType().getId(), toCommandLine(), getAttributes());
    }
}
