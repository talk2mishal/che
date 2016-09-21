package org.eclipse.che.plugin.maven.client.command;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.extension.machine.client.command.CommandProducer;

import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaFile;

/**
 * //
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MavenTestCommandProducer implements CommandProducer {

    private final AppContext appContext;
    private final MavenCommandFactory mavenCommandFactory;

    @Inject
    public MavenTestCommandProducer(AppContext appContext, MavenCommandFactory mavenCommandFactory) {
        this.appContext = appContext;
        this.mavenCommandFactory = mavenCommandFactory;
    }

    @Override
    public String getName() {
        return "maven test";
    }

    @Override
    public boolean isApplicable() {
        final Resource[] resources = appContext.getResources();
        if (resources == null || resources.length != 1) {
            return false;
        }

        final Resource resource = appContext.getResource();
        final Optional<Project> projectOptional = appContext.getResource().getRelatedProject();

        if (!projectOptional.isPresent()) {
            return false;
        }

        final Project project = projectOptional.get();
        if (project.isTypeOf("maven") && resource.isFile()) {
            final String ext = ((File)resource).getExtension();
            return "java".equals(ext);
        }

        return false;
    }

    @Override
    public MavenCommandConfiguration createCommand() {
        final Optional<Project> projectOptional = appContext.getResource().getRelatedProject();

        final MavenCommandConfiguration command = mavenCommandFactory.newCommand("name");

        if (projectOptional.isPresent()) {
            command.setWorkingDirectory(projectOptional.get().getPath());
        }

        command.setCommandLine("test -Dtest=" + getCurrentClassFQN());

        return command;
    }

    private String getCurrentClassFQN() {
        final Resource[] resources = appContext.getResources();

        if (resources == null || resources.length != 1) {
            return "";
        }

        final Resource resource = resources[0];
        final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

        if (resource.getResourceType() == FILE && isJavaFile(resource) && srcFolder.isPresent()) {
            return JavaUtil.resolveFQN((Container)srcFolder.get(), resource);
        }

        return "";
    }
}
