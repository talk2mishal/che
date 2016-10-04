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
package org.eclipse.che.api.core.jdbc.jpa.event;

import org.eclipse.che.api.core.notification.EventSubscriber;

/**
 * TODO
 *
 * @author Anton Korneta.
 */
public abstract class CascadeRemoveEventSubscriber<T extends CascadeRemovalEvent> implements EventSubscriber<T> {

    @Override
    public void onEvent(T event) {
        if (!event.getContext().isFailed()) {
            try {
                onRemove(event);
            } catch (Exception ex) {
                event.getContext().setCause(ex);
            }
        }
    }

    /**
     * TODO
     */
    public abstract void onRemove(T event) throws Exception;
}
