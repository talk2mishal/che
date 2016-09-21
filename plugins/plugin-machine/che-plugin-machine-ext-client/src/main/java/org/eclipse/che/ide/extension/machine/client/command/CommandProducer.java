package org.eclipse.che.ide.extension.machine.client.command;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandProducer {

    /** Returns the text that will be used as related action's title. */
    String getName();

    /** Whether the producer can produce command from the current context? */
    boolean isApplicable();

    /** Creates command from the current context of application. */
    CommandConfiguration createCommand();
}
