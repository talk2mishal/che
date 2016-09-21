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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.promises.client.Promise;

/**
 * Properties may be used in a command as a substitution.
 * <p>Actual value will be substituted before sending a command for execution to the server.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandPropertyValueProvider {

    /** Get key. The format is ${key.name}. */
    String getKey();

    /** Returns the macros description. */
    String getDescription();

    /** Get value. */
    Promise<String> getValue();
}
