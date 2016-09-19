package org.eclipse.che.ide.extension.machine.client.command;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.Collection;

/**
 * Registry for command types.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandTypeRegistry {

    /**
     * Returns {@link CommandType} with the specified ID or {@code null} if none.
     *
     * @param id
     *         the ID of the command type
     * @return command type or {@code null}
     */
    @Nullable
    CommandType getCommandTypeById(String id);

    /** Returns all registered {@link CommandType}s. */
    Collection<CommandType> getCommandTypes();
}
