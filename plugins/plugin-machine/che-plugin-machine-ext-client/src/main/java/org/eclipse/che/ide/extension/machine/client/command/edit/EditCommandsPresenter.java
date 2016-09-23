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

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.dialogs.ChoiceDialog;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.actions.SelectCommandComboBox;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationManager;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandConfigurationPage.DirtyStateListener;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandConfigurationPage.FieldStateActionDelegate;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandImpl;
import org.eclipse.che.ide.extension.machine.client.command.api.CommandType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Presenter for managing commands.
 *
 * @author Artem Zatsarynnyi
 * @author Oleksii Orel
 * @author Valeriy Svydenko
 */
@Singleton
public class EditCommandsPresenter implements EditCommandsView.ActionDelegate, FieldStateActionDelegate {

    public static final String PREVIEW_URL_ATTR = "previewUrl";

    private final EditCommandsView                  view;
    private final CommandConfigurationManager       commandConfigurationManager;
    private final CommandManager                    commandManager;
    private final CommandTypeRegistry               commandTypeRegistry;
    private final DialogFactory                     dialogFactory;
    private final MachineLocalizationConstant       machineLocale;
    private final CoreLocalizationConstant          coreLocale;
    private final Provider<SelectCommandComboBox>   selectCommandActionProvider;
    private final Set<ConfigurationChangedListener> configurationChangedListeners;

    /** Initial name of the currently edited command. */
    String                    editedCommandOriginName;
    String                    editedCommandOriginPreviewUrl;
    CommandProcessingCallback commandProcessingCallback;

    private CommandConfigurationPage editedPage;
    /** Command that being edited. */
    private CommandImpl              editedCommand;

    @Inject
    protected EditCommandsPresenter(EditCommandsView view,
                                    CommandConfigurationManager commandConfigurationManager,
                                    CommandTypeRegistry commandTypeRegistry,
                                    DialogFactory dialogFactory,
                                    MachineLocalizationConstant machineLocale,
                                    CoreLocalizationConstant coreLocale,
                                    Provider<SelectCommandComboBox> selectCommandActionProvider,
                                    CommandManager commandManager) {
        this.view = view;
        this.commandConfigurationManager = commandConfigurationManager;
        this.commandManager = commandManager;
        this.commandTypeRegistry = commandTypeRegistry;
        this.dialogFactory = dialogFactory;
        this.machineLocale = machineLocale;
        this.coreLocale = coreLocale;
        this.selectCommandActionProvider = selectCommandActionProvider;
        this.view.setDelegate(this);
        configurationChangedListeners = new HashSet<>();
    }

    @Override
    public void onCloseClicked() {
        final CommandImpl selectedConfiguration = view.getSelectedConfiguration();
        onNameChanged();
        if (selectedConfiguration != null && isViewModified()) {
            onConfigurationSelected(selectedConfiguration);
        }
        view.close();
    }

    private void selectCommandOnToolbar(CommandImpl commandToSelect) {
        selectCommandActionProvider.get().setSelectedCommand(commandToSelect);
    }

    @Override
    public void onSaveClicked() {
        final CommandImpl selectedConfiguration;
        if (view.getSelectedConfiguration() == null) {
            return;
        }

        onNameChanged();
        selectedConfiguration = view.getSelectedConfiguration();

        updateCommand(selectedConfiguration).then(new Operation<CommandImpl>() {
            @Override
            public void apply(CommandImpl arg) throws OperationException {
                commandProcessingCallback = getCommandProcessingCallback();
                fetchCommands();
                fireConfigurationUpdated(selectedConfiguration);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                dialogFactory.createMessageDialog("Error", arg.getMessage(), null).show();
            }
        });
    }

    private Promise<CommandImpl> updateCommand(final CommandImpl selectedConfiguration) {
        if (editedCommandOriginName.trim().equals(selectedConfiguration.getName())) {
            return commandConfigurationManager.update(selectedConfiguration.getName(), selectedConfiguration);
        } else {
            return commandConfigurationManager.update(selectedConfiguration.getName(), selectedConfiguration).then(
                    new Operation<CommandImpl>() {
                        @Override
                        public void apply(CommandImpl arg) throws OperationException {
                            final String newName = arg.getName();

                            onNameChanged();

                            if (selectedConfiguration.equals(view.getSelectedConfiguration())) {
                                //update selected configuration name
                                view.getSelectedConfiguration().setName(newName);
                            }
                        }
                    });
        }
    }

    @Override
    public void onCancelClicked() {
        commandProcessingCallback = getCommandProcessingCallback();
        fetchCommands();
    }

    @Override
    public void onDuplicateClicked() {
        final CommandImpl selectedConfiguration = view.getSelectedConfiguration();

        if (selectedConfiguration != null) {
            createNewCommand(selectedConfiguration.getType(),
                             selectedConfiguration.getCommandLine(),
                             selectedConfiguration.getName(),
                             selectedConfiguration.getAttributes());
        }
    }

    @Override
    public void onAddClicked() {
        final String selectedType = view.getSelectedCommandType();
        if (selectedType != null) {
            createNewCommand(selectedType, null, null, null);
        }
    }

    private void createNewCommand(final String type,
                                  final String customCommand,
                                  final String customName,
                                  final Map<String, String> attributes) {
        if (!isViewModified()) {
            reset();
            createCommand(type, customCommand, customName, attributes);
            return;
        }

        final ConfirmCallback saveCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                updateCommand(editedCommand).then(new Operation<CommandImpl>() {
                    @Override
                    public void apply(CommandImpl arg) throws OperationException {
                        reset();
                        createCommand(type, customCommand, customName, attributes);
                    }
                });
            }
        };

        final ConfirmCallback discardCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                fetchCommands();
                reset();
                createCommand(type, customCommand, customName, attributes);
            }
        };

        final ChoiceDialog dialog = dialogFactory.createChoiceDialog(
                machineLocale.editCommandsSaveChangesTitle(),
                machineLocale.editCommandsSaveChangesConfirmation(editedCommand.getName()),
                coreLocale.save(),
                machineLocale.editCommandsSaveChangesDiscard(),
                saveCallback,
                discardCallback);
        dialog.show();
    }

    private void createCommand(String type, String customCommand, String customName, Map<String, String> attributes) {
        commandConfigurationManager.create(customName,
                                           customCommand,
                                           type,
                                           attributes).then(new Operation<CommandImpl>() {
            @Override
            public void apply(CommandImpl command) throws OperationException {
                fetchCommands();

                fireConfigurationAdded(command);
                view.setSelectedConfiguration(command);
            }
        });
    }

    @Override
    public void onRemoveClicked(final CommandImpl selectedConfiguration) {
        if (selectedConfiguration == null) {
            return;
        }

        final ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                commandConfigurationManager.remove(selectedConfiguration.getName()).then(new Operation<Void>() {
                    @Override
                    public void apply(Void arg) throws OperationException {
                        view.selectNextItem();
                        commandProcessingCallback = getCommandProcessingCallback();
                        fetchCommands();
                        fireConfigurationRemoved(selectedConfiguration);
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        dialogFactory.createMessageDialog("Error", arg.getMessage(), null).show();
                    }
                });
            }
        };

        final ConfirmDialog confirmDialog = dialogFactory.createConfirmDialog(
                machineLocale.editCommandsViewRemoveTitle(),
                machineLocale.editCommandsRemoveConfirmation(selectedConfiguration.getName()),
                confirmCallback,
                null);
        confirmDialog.show();
    }

    @Override
    public void onExecuteClicked() {
        final CommandImpl selectedConfiguration = view.getSelectedConfiguration();
        if (selectedConfiguration == null) {
            return;
        }

        if (isViewModified()) {
            dialogFactory.createMessageDialog("", machineLocale.editCommandsExecuteMessage(), null).show();
            return;
        }

        commandManager.execute(selectedConfiguration);
        view.close();
    }

    @Override
    public void onEnterClicked() {
        if (view.isCancelButtonInFocus()) {
            onCancelClicked();
            return;
        }

        if (view.isCloseButtonInFocus()) {
            onCloseClicked();
            return;
        }

        onSaveClicked();
    }

    private void reset() {
        editedCommand = null;
        editedCommandOriginName = null;
        editedCommandOriginPreviewUrl = null;
        editedPage = null;

        view.setConfigurationName("");
        view.setConfigurationPreviewUrl("");
        view.clearCommandConfigurationsContainer();
    }

    @Override
    public void onConfigurationSelected(final CommandImpl configuration) {
        if (!isViewModified()) {
            handleCommandSelection(configuration);
            return;
        }

        final ConfirmCallback saveCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                updateCommand(editedCommand).then(new Operation<CommandImpl>() {
                    @Override
                    public void apply(CommandImpl arg) throws OperationException {
                        fetchCommands();
                        fireConfigurationUpdated(editedCommand);
                        handleCommandSelection(configuration);
                    }
                });
            }
        };

        final ConfirmCallback discardCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                reset();
                fetchCommands();
                handleCommandSelection(configuration);
            }
        };

        final ChoiceDialog dialog = dialogFactory.createChoiceDialog(
                machineLocale.editCommandsSaveChangesTitle(),
                machineLocale.editCommandsSaveChangesConfirmation(editedCommand.getName()),
                coreLocale.save(),
                machineLocale.editCommandsSaveChangesDiscard(),
                saveCallback,
                discardCallback);
        dialog.show();
    }

    private String getPreviewUrlOrNull(CommandImpl configuration) {
        if (configuration.getAttributes() != null && configuration.getAttributes().containsKey(PREVIEW_URL_ATTR)) {
            return configuration.getAttributes().get(PREVIEW_URL_ATTR);
        }
        return null;
    }

    private void handleCommandSelection(CommandImpl configuration) {
        editedCommand = configuration;
        editedCommandOriginName = configuration.getName();
        editedCommandOriginPreviewUrl = getPreviewUrlOrNull(configuration);

        view.setConfigurationName(configuration.getName());
        view.setConfigurationPreviewUrl(getPreviewUrlOrNull(configuration));

        final Collection<CommandConfigurationPage> pages = commandConfigurationManager.getPages(configuration.getType());
        for (CommandConfigurationPage page : pages) {
            editedPage = page;

            page.setFieldStateActionDelegate(this);

            page.setDirtyStateListener(new DirtyStateListener() {
                @Override
                public void onDirtyStateChanged() {
                    view.setCancelButtonState(isViewModified());
                    view.setSaveButtonState(isViewModified());
                }
            });

            page.resetFrom(configuration);
            page.go(view.getCommandConfigurationsContainer());

            // TODO: for now only the 1'st page is showing but need to show all the pages
            break;
        }
    }

    @Override
    public void onNameChanged() {
        CommandImpl selectedConfiguration = view.getSelectedConfiguration();
        if (selectedConfiguration == null || !selectedConfiguration.equals(editedCommand)) {
            return;
        }

        selectedConfiguration.setName(view.getConfigurationName());

        view.setCancelButtonState(isViewModified());
        view.setSaveButtonState(isViewModified());
    }

    @Override
    public void onPreviewUrlChanged() {
        CommandImpl selectedConfiguration = view.getSelectedConfiguration();
        if (selectedConfiguration == null || !selectedConfiguration.equals(editedCommand)) {
            return;
        }
        selectedConfiguration.getAttributes().put(PREVIEW_URL_ATTR, view.getConfigurationPreviewUrl());
        view.setCancelButtonState(isViewModified());
        view.setSaveButtonState(isViewModified());
    }

    /** Show dialog. */
    public void show() {
        fetchCommands();

        view.show();
    }

    /** Fetch commands from server and update view. */
    private void fetchCommands() {
        final String originName = editedCommandOriginName;

        reset();

        view.setCancelButtonState(false);
        view.setSaveButtonState(false);

        List<CommandImpl> commands = commandConfigurationManager.getCommands();

        final Map<CommandType, List<CommandImpl>> categories = new HashMap<>();

        for (CommandType type : commandTypeRegistry.getCommandTypes()) {
            final List<CommandImpl> settingsCategory = new ArrayList<>();
            for (CommandImpl configuration : commands) {
                if (type.getId().equals(configuration.getType())) {
                    settingsCategory.add(configuration);

                    if (configuration.getName().equals(originName)) {
                        view.setSelectedConfiguration(configuration);
                    }
                }
            }

            Collections.sort(settingsCategory, new Comparator<CommandImpl>() {
                @Override
                public int compare(CommandImpl o1, CommandImpl o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            categories.put(type, settingsCategory);
        }

        view.setData(categories);
        view.setFilterState(!commands.isEmpty());

        if (commandProcessingCallback != null) {
            commandProcessingCallback.onCompleted();
            commandProcessingCallback = null;
        }
    }

    private boolean isViewModified() {
        if (editedCommand == null || editedPage == null) {
            return false;
        }
        return editedPage.isDirty()
               || !editedCommandOriginName.equals(view.getConfigurationName())
               || !Strings.nullToEmpty(editedCommandOriginPreviewUrl).equals(Strings.nullToEmpty(view.getConfigurationPreviewUrl()));
    }

    private void fireConfigurationAdded(CommandImpl command) {
        for (ConfigurationChangedListener listener : configurationChangedListeners) {
            listener.onConfigurationAdded(command);
        }
    }

    private void fireConfigurationRemoved(CommandImpl command) {
        for (ConfigurationChangedListener listener : configurationChangedListeners) {
            listener.onConfigurationRemoved(command);
        }
    }

    private void fireConfigurationUpdated(CommandImpl command) {
        for (ConfigurationChangedListener listener : configurationChangedListeners) {
            listener.onConfigurationsUpdated(command);
        }
    }

    private CommandProcessingCallback getCommandProcessingCallback() {
        return new CommandProcessingCallback() {
            @Override
            public void onCompleted() {
                view.setCloseButtonInFocus();
            }
        };
    }

    public void addConfigurationsChangedListener(ConfigurationChangedListener listener) {
        configurationChangedListeners.add(listener);
    }

    public void removeConfigurationsChangedListener(ConfigurationChangedListener listener) {
        configurationChangedListeners.remove(listener);
    }

    @Override
    public void updatePreviewURLState(boolean isVisible) {
        view.setPreviewUrlState(isVisible);
    }

    /** Listener that will be called when command configuration changed. */
    public interface ConfigurationChangedListener {
        void onConfigurationAdded(CommandImpl command);

        void onConfigurationRemoved(CommandImpl command);

        void onConfigurationsUpdated(CommandImpl command);
    }

    interface CommandProcessingCallback {
        /** Called when handling of command is completed successfully. */
        void onCompleted();
    }

}
