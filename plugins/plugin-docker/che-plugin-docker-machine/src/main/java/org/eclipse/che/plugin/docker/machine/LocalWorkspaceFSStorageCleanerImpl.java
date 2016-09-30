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
package org.eclipse.che.plugin.docker.machine;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.api.workspace.server.WorkspaceFSStorageCleaner;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Local implementation of the {@link WorkspaceFSStorageCleaner}.
 *
 * @author Alexander Andrienko
 */
@Singleton
public class LocalWorkspaceFSStorageCleanerImpl implements WorkspaceFSStorageCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(LocalWorkspaceFSStorageCleanerImpl.class);

    private final WorkspaceFolderPathProvider workspaceFolderPathProvider;
    private final ExecutorService             executor;

    @Inject(optional = true)
    @Named("host.projects.root")
    private String hostProjectsFolder;

    @Inject
    public LocalWorkspaceFSStorageCleanerImpl(WorkspaceFolderPathProvider workspaceFolderPathProvider) {
        this.workspaceFolderPathProvider = workspaceFolderPathProvider;
        executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("LocalWorkspaceFSStorageCleanerImpl-%d")
                                                                               .setDaemon(true)
                                                                               .build());
    }

    @Override
    public void clear(String workspaceId) {
        ThreadLocalPropagateContext.wrap(() -> {
            try {
                String workspacePath = workspaceFolderPathProvider.getPath(workspaceId);
                File workspaceStorage = new File(workspacePath);
                if (!workspacePath.equals(hostProjectsFolder) && workspaceStorage.exists()) {
                    IoUtil.deleteRecursive(workspaceStorage);
                }
            } catch (IOException e) {
                LOG.error("Failed to clean up workspace folder for workspace with id: {}. Cause: {}.", workspaceId, e.getMessage());
            }
        });
    }
}
