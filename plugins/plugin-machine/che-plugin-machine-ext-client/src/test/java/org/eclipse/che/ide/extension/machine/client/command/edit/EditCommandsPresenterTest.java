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
package org.eclipse.che.ide.extension.machine.client.command.edit;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationManager;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandImpl;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Roman Nikitenko */
@RunWith(MockitoJUnitRunner.class)
public class EditCommandsPresenterTest {

    private static String COMMAND_NAME = "commandName";

    @Mock
    private EditCommandsView            view;
    @Mock
    private CommandConfigurationManager commandConfigurationManager;
    @Mock
    private CommandManager              commandManager;
    @Mock
    private CommandTypeRegistry         commandTypeRegistry;
    @Mock
    private DialogFactory               dialogFactory;
    @Mock
    private MachineLocalizationConstant machineLocale;
    @Mock
    private CoreLocalizationConstant    coreLocale;

    @Mock
    private Promise<List<CommandImpl>>                                     commandsPromise;
    @Mock
    private Promise<CommandImpl>                                          commandPromise;
    @Mock
    private Promise<List<CommandImpl>>                                    commandConfigurationPromise;
    @Captor
    private ArgumentCaptor<Function<List<CommandDto>, List<CommandImpl>>> commandsCaptor;
    @Captor
    private ArgumentCaptor<Operation<List<CommandImpl>>>                  commandConfigurationCaptor;
    @Captor
    private ArgumentCaptor<Operation<WorkspaceDto>>                       workspaceCaptor;


    @InjectMocks
    private EditCommandsPresenter presenter;

    @Before
    public void setUp() {
        presenter.editedCommandOriginName = COMMAND_NAME;

//        when(commandConfigurationManager.getCommands()).thenReturn(commandsPromise);
//        when(commandsPromise.then((Function<List<CommandDto>, List<CommandImpl>>)anyObject())).thenReturn(commandConfigurationPromise);
//        when(commandConfigurationPromise.then((Operation<List<CommandImpl>>)anyObject())).thenReturn(commandConfigurationPromise);
        when(commandConfigurationManager.update(anyString(), anyObject())).thenReturn(commandPromise);
    }

    @Test
    public void onEnterClickedWhenCancelButtonInFocus() throws Exception {
        when(view.isCancelButtonInFocus()).thenReturn(true);
        CommandImpl commandConfiguration = mock(CommandImpl.class);
        List<CommandImpl> commands = new ArrayList<>(1);
        commands.add(commandConfiguration);

        presenter.onEnterClicked();

        verify(view).setCancelButtonState(false);
        verify(view).setSaveButtonState(false);
        verify(commandConfigurationManager).getCommands();

//        verify(commandsPromise).then(commandsCaptor.capture());
//        commandsCaptor.getValue().apply(commands);

        verify(commandConfigurationPromise).then(commandConfigurationCaptor.capture());
        commandConfigurationCaptor.getValue().apply(commands);

        verify(view).setData(anyObject());
        verify(view).setFilterState(anyBoolean());
        verify(view).setCloseButtonInFocus();

        verify(view, never()).close();
        verify(commandConfigurationManager, never()).update(anyString(), anyObject());
        verify(commandConfigurationManager, never()).remove(anyString());
    }

    @Test
    public void onEnterClickedWhenCloseButtonInFocus() throws Exception {
        when(view.isCloseButtonInFocus()).thenReturn(true);

        presenter.onEnterClicked();

        verify(view).close();
        verify(commandConfigurationManager, never()).getCommands();
        verify(commandConfigurationManager, never()).update(anyString(), anyObject());
        verify(commandConfigurationManager, never()).remove(anyString());
    }

    @Test
    public void onEnterClickedWhenSaveButtonInFocus() throws Exception {
        when(view.isCancelButtonInFocus()).thenReturn(false);
        when(view.isCloseButtonInFocus()).thenReturn(false);
        CommandImpl commandConfiguration = mock(CommandImpl.class);
//        List<CommandDto> commands = new ArrayList<>(1);
        List<CommandImpl> commands = new ArrayList<>(1);
//        commands.add(command);
        commands.add(commandConfiguration);
//        when(command.withName(anyString())).thenReturn(command);
//        when(command.withCommandLine(anyString())).thenReturn(command);
//        when(command.withType(anyString())).thenReturn(command);
//        when(command.withAttributes(anyMap())).thenReturn(command);
        when(view.getSelectedConfiguration()).thenReturn(commandConfiguration);
//        when(commandConfiguration.getType()).thenReturn(mock(CommandType.class));
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);

//        when(commandPromise.thenPromise(any(Function.class))).thenReturn(commandPromise);
        when(commandPromise.then((Operation)anyObject())).thenReturn(commandPromise);

        presenter.onEnterClicked();

//        verify(dtoFactory).createDto(CommandDto.class);
        verify(commandConfigurationManager).update(anyString(), eq(commandConfiguration));
//        verify(commandPromise).then(workspaceCaptor.capture());
//        workspaceCaptor.getValue().apply(workspace);

        verify(view).setCancelButtonState(false);
        verify(view).setSaveButtonState(false);
        verify(commandConfigurationManager).getCommands();

//        verify(commandsPromise).then(commandsCaptor.capture());
//        commandsCaptor.getValue().apply(commands);

        verify(commandConfigurationPromise).then(commandConfigurationCaptor.capture());
        commandConfigurationCaptor.getValue().apply(commands);

        verify(view).setData(anyObject());
        verify(view).setFilterState(anyBoolean());
        verify(view).setCloseButtonInFocus();
        verify(view, never()).close();
    }
}
