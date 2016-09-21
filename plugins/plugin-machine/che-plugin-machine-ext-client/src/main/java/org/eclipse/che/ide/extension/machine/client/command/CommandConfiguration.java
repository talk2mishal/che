package org.eclipse.che.ide.extension.machine.client.command;

import java.util.Map;

/**
 * API for the command configuration.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandConfiguration {

    /** Returns command configuration name. */
    String getName();

    /** Sets command configuration name. */
    void setName(String name);

    /** Returns command configuration type. */
    CommandType getType();

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    /** Returns command line to execute in machine. */
    String toCommandLine();
}
