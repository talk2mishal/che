package org.eclipse.che.plugin.maven.client.command;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
public interface MavenCommandFactory {

    MavenCommandConfiguration newCommand(String name);
}
