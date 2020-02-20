/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package stroom.config.global.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.MyPresenterWidget;
import com.gwtplatform.mvp.client.View;
import stroom.alert.client.event.AlertEvent;
import stroom.config.global.shared.ConfigProperty;
import stroom.config.global.shared.GlobalConfigResource;
import stroom.config.global.shared.OverrideValue;
import stroom.dispatch.client.Rest;
import stroom.dispatch.client.RestFactory;
import stroom.node.shared.FetchNodeStatusResponse;
import stroom.node.shared.NodeResource;
import stroom.security.client.api.ClientSecurityContext;
import stroom.svg.client.SvgPreset;
import stroom.svg.client.SvgPresets;
import stroom.ui.config.client.UiConfigCache;
import stroom.widget.button.client.ButtonView;
import stroom.widget.popup.client.event.HidePopupEvent;
import stroom.widget.popup.client.event.ShowPopupEvent;
import stroom.widget.popup.client.presenter.DefaultPopupUiHandlers;
import stroom.widget.popup.client.presenter.PopupPosition;
import stroom.widget.popup.client.presenter.PopupSize;
import stroom.widget.popup.client.presenter.PopupUiHandlers;
import stroom.widget.popup.client.presenter.PopupView.PopupType;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ManageGlobalPropertyEditPresenter
        extends MyPresenterWidget<ManageGlobalPropertyEditPresenter.GlobalPropertyEditView>
        implements ManageGlobalPropertyEditUiHandlers {

    private static final NodeResource NODE_RESOURCE = GWT.create(NodeResource.class);
    private static final GlobalConfigResource GLOBAL_CONFIG_RESOURCE_RESOURCE = GWT.create(GlobalConfigResource.class);

    private static final String MAGIC_NULL = "NULL";

    private final RestFactory restFactory;
    private final ClientSecurityContext securityContext;
    private final UiConfigCache clientPropertyCache;
    private ConfigProperty configProperty;
    private Map<String, OverrideValue<String>> clusterYamlOverrides = new HashMap<>();
    private Map<String, Set<String>> effectiveValueToNodesMap = new HashMap<>();
    private final ButtonView yamlValueWarningsButton;
    private Provider<ConfigPropertyClusterValuesPresenter> clusterValuesPresenterProvider;
    private final ButtonView effectiveValueWarningsButton;

    @Inject
    public ManageGlobalPropertyEditPresenter(final EventBus eventBus,
                                             final GlobalPropertyEditView view,
                                             final RestFactory restFactory,
                                             final ClientSecurityContext securityContext,
                                             final UiConfigCache clientPropertyCache,
                                             final Provider<ConfigPropertyClusterValuesPresenter> clusterValuesPresenterProvider) {
        super(eventBus, view);
        this.restFactory = restFactory;
        this.securityContext = securityContext;
        this.clientPropertyCache = clientPropertyCache;
        this.clusterValuesPresenterProvider = clusterValuesPresenterProvider;

        this.yamlValueWarningsButton = view.addYamlValueWarningIcon(SvgPresets.ALERT.title("Node values differ"));
        this.clusterValuesPresenterProvider = clusterValuesPresenterProvider;
        this.yamlValueWarningsButton.setVisible(false);

        this.effectiveValueWarningsButton = view.addEffectiveValueWarningIcon(SvgPresets.ALERT.title("Node values differ"));
        this.effectiveValueWarningsButton.setVisible(false);

        view.setUiHandlers(this);
    }

    @Override
    protected void onBind() {
        registerHandler(yamlValueWarningsButton.addClickHandler(event -> {
            if ((event.getNativeButton() & NativeEvent.BUTTON_LEFT) != 0) {
                // TODO Open popup showing all values
                onOpenClusterValues();
            }
        }));

        registerHandler(effectiveValueWarningsButton.addClickHandler(event -> {
            if ((event.getNativeButton() & NativeEvent.BUTTON_LEFT) != 0) {
                // TODO Open popup showing all values
                onOpenClusterValues();
            }
        }));
    }

    private void onOpenClusterValues() {
        final PopupUiHandlers popupUiHandlers = new DefaultPopupUiHandlers() {
            @Override
            public void onHide(final boolean autoClose, final boolean ok) {

            }
        };

        if (clusterValuesPresenterProvider != null) {
            final ConfigPropertyClusterValuesPresenter clusterValuesPresenter = clusterValuesPresenterProvider.get();
            // Get the position of this popup so we can show the cluster values popup at a slight
            // offset to make it clear it is above the other one. Offsets are not equal to account
            // for the title bar
            final PopupPosition offsetPopupPosition = new PopupPosition(
                    getView().asWidget().getElement().getAbsoluteLeft() + 20,
                    getView().asWidget().getElement().getAbsoluteTop() + 10);

            clusterValuesPresenter.show(getEntity(), effectiveValueToNodesMap, offsetPopupPosition, popupUiHandlers);
        }
    }

    protected ClientSecurityContext getSecurityContext() {
        return securityContext;
    }

    void showEntity(final ConfigProperty configProperty, final PopupUiHandlers popupUiHandlers) {

        if (configProperty.getId() != null) {
            updateValuesFromResource(configProperty.getName().toString(), popupUiHandlers);

        } else {
            // new configProperty
            setEntity(configProperty);
            showPopup(popupUiHandlers);
        }
        // find out the yaml values for each node in the cluster
        refreshYamlOverrideForAllNodes();
    }

    private void updateWarningState() {
        final long uniqueYamlOverrideValues = getUniqueYamlOverrideValues();
        // TODO here just for testing
        yamlValueWarningsButton.setVisible(true);
        yamlValueWarningsButton.setTitle("Unique value(s): " + uniqueYamlOverrideValues);

        effectiveValueWarningsButton.setVisible(true);
        effectiveValueWarningsButton.setTitle("Unique value(s): " + uniqueYamlOverrideValues);

//        warningsButton.setVisible(uniqueYamlOverrideValues > 1);
    }

    private void updateValuesFromResource(final String propertyName, final PopupUiHandlers popupUiHandlers) {
        final Rest<ConfigProperty> fetchPropertyRest = restFactory.create();

        fetchPropertyRest
            .onSuccess(configProperty -> {
                setEntity(configProperty);
                showPopup(popupUiHandlers);
            })
            .onFailure(throwable -> {
                showError(throwable, "Error fetching property " + propertyName);
            })
            .call(GLOBAL_CONFIG_RESOURCE_RESOURCE)
            .getPropertyByName(propertyName);
    }

    private long getUniqueYamlOverrideValues() {
        return clusterYamlOverrides.values()
                .stream()
            .filter(OverrideValue::isHasOverride)
                .distinct()
                .count();
    }

    private Map<String, String> getEffectiveValues() {
        return clusterYamlOverrides.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                            String effectiveValue = configProperty.getEffectiveValue(entry.getValue())
                                    .orElse(null);
                            return new AbstractMap.SimpleEntry<>(entry.getKey(), effectiveValue);
                        })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void refreshYamlOverrideForAllNodes() {
        final Rest<FetchNodeStatusResponse> fetchAllNodes = restFactory.create();

        // For each node fire off a request to get the yaml override for that node
        fetchAllNodes
                .onSuccess(response -> {
                    response.getValues().forEach(nodeStatus -> {
                        refreshYamlOverrideForNode(nodeStatus.getNode().getName());
                    });
                })
                .onFailure(throwable -> {
                    showError(throwable, "Error getting list of all nodes");
                })
                .call(NODE_RESOURCE)
                .list();
    }

    private void refreshYamlOverrideForNode(final String nodeName) {
        final Rest<OverrideValue<String>> fetchNodeYamlOverrideRest = restFactory.create();

        fetchNodeYamlOverrideRest
                .onSuccess(yamlOverride -> {
                    // Add the node's result to our maps
                    clusterYamlOverrides.put(nodeName, yamlOverride);
                    final String effectiveValueFromNode = configProperty.getEffectiveValue(yamlOverride)
                        .orElse(MAGIC_NULL);
                    if (yamlOverride == null) {
                        effectiveValueToNodesMap.forEach((effectiveValue, nodes) -> {
                            if (effectiveValue.equals(effectiveValueFromNode))
                            nodes.remove(nodeName);
                        });
                    } else {
                        this.effectiveValueToNodesMap.computeIfAbsent(
                            effectiveValueFromNode,
                            k -> new HashSet<>())
                            .add(nodeName);
                    }
                    updateWarningState();
                })
                .onFailure(throwable -> {
                    clusterYamlOverrides.remove(nodeName);
                    showError(throwable, "Error getting YAML override for node " + nodeName);
                })
                .call(GLOBAL_CONFIG_RESOURCE_RESOURCE)
                .getYamlValueByNodeAndName(configProperty.getName().toString(), nodeName);
    }

    private void showPopup(final PopupUiHandlers popupUiHandlers) {
        final String caption = getEntityDisplayType() + " - " + configProperty.getName();
        final PopupType popupType = PopupType.OK_CANCEL_DIALOG;

        final PopupUiHandlers internalPopupUiHandlers = new PopupUiHandlers() {
            @Override
            public void onHideRequest(final boolean autoClose, final boolean ok) {
                if (ok) {
                    write(true);
                } else {
                    hide();
                }

                popupUiHandlers.onHideRequest(autoClose, ok);
            }

            @Override
            public void onHide(final boolean autoClose, final boolean ok) {
                popupUiHandlers.onHide(autoClose, ok);
            }
        };

        read();
        ShowPopupEvent.fire(
                ManageGlobalPropertyEditPresenter.this,
                ManageGlobalPropertyEditPresenter.this,
                popupType,
                getPopupSize(),
                caption,
                internalPopupUiHandlers);
    }

    protected void hide() {
        HidePopupEvent.fire(
                ManageGlobalPropertyEditPresenter.this,
                ManageGlobalPropertyEditPresenter.this);
    }

    private ConfigProperty getEntity() {
        return configProperty;
    }

    private void setEntity(final ConfigProperty entity) {
        this.configProperty = entity;
    }

    private String getEntityDisplayType() {
        return "Application Property";
    }

    private void read() {
        getView().setPasswordStyle(getEntity().isPassword());
        getView().setRequireRestart(getEntity().isRequireRestart());
        getView().setRequireUiRestart(getEntity().isRequireUiRestart());
        getView().getName().setText(getEntity().getName().toString());
        getView().setUseOverride(getEntity().hasDatabaseOverride());
        String databaseOverrideValue = "";
        if (getEntity().hasDatabaseOverride()) {
            databaseOverrideValue = getEntity()
                    .getDatabaseOverrideValue()
                    .getValueOrElse("");
        }
        String yamlOverrideValue = "";
        if (getEntity().hasYamlOverride()) {
            yamlOverrideValue = getEntity()
                    .getYamlOverrideValue()
                    .getValueOrElse("");
        }
        getView().getDefaultValue().setText(getEntity()
                .getDefaultValue()
                .orElse(""));
        getView().getYamlValue().setText(yamlOverrideValue);
        getView().getDatabaseValue().setText(databaseOverrideValue);
        getView().getEffectiveValue().setText(getEntity()
                .getEffectiveValue()
                .orElse(""));
        getView().getDescription().setText(getEntity().getDescription());
        getView().getDataType().setText(getEntity().getDataTypeName());
        getView().getSource().setText(getEntity().getSource().getName());

        getView().setEditable(getEntity().isEditable());
    }

    private void write(final boolean hideOnSave) {
        refreshValuesOnChange();

        ConfigProperty configPropertyToSave = getEntity();

        Rest<ConfigProperty> restCall = restFactory.create();
        restCall
            .onSuccess(savedConfigProperty -> {
                setEntity(savedConfigProperty);
                if (hideOnSave) {
                    hide();
                    // Refresh client properties in case they were affected by this change.
                    clientPropertyCache.refresh();
                }
            });

        if (configPropertyToSave.getId() == null) {
            // No ID so this doesn't exist in the DB
            restCall
                .onFailure(throwable ->
                   showError(throwable, "Error creating property"))
                .call(GLOBAL_CONFIG_RESOURCE_RESOURCE)
                .create(configPropertyToSave);
        } else {
            restCall
                .onFailure(throwable ->
                    showError(throwable, "Error updating property"))
                .call(GLOBAL_CONFIG_RESOURCE_RESOURCE)
                .update(configPropertyToSave.getName().toString(), configPropertyToSave);
        }
    }

    private void showError(final Throwable throwable, final String message) {
        AlertEvent.fireError(
            ManageGlobalPropertyEditPresenter.this,
            message + " - " + throwable.getMessage(),
            null,
            null);
    }

    private void refreshValuesOnChange() {
        if (getView().getUseOverride()) {
            final String value = getView().getDatabaseValue().getText();
            getEntity().setDatabaseOverrideValue(OverrideValue.with(value.trim()));
        } else {
            getEntity().setDatabaseOverrideValue(OverrideValue.unSet(String.class));

            // Don't clear the db override field on screen in case they unticked
            // by accident
        }

        getView().getEffectiveValue().setText(getEntity().getEffectiveValue().orElse(null));
        getView().getSource().setText(getEntity().getSource().getName());

        // Refresh the edit status of the override fields
        getView().setEditable(getEntity().isEditable());
    }

    protected PopupSize getPopupSize() {
        return new PopupSize(
                700, 513,
                700, 513,
                1024, 513,
                true);
    }

    @Override
    public void onChangeUseOverride() {
        refreshValuesOnChange();
    }

    @Override
    public void onChangeOverrideValue() {
        refreshValuesOnChange();
    }

    public interface GlobalPropertyEditView extends View, HasUiHandlers<ManageGlobalPropertyEditUiHandlers> {
        HasText getName();

        HasText getDescription();

        HasText getDefaultValue();

        HasText getYamlValue();

        boolean getUseOverride();

        HasText getDatabaseValue();

        HasText getEffectiveValue();

        HasText getSource();

        HasText getDataType();

        void setPasswordStyle(boolean password);

        void setRequireRestart(boolean requiresRestart);

        void setRequireUiRestart(boolean requiresRestart);

        void setUseOverride(boolean useOverride);

        void setEditable(boolean edit);

        ButtonView addYamlValueWarningIcon(SvgPreset preset);

        ButtonView addEffectiveValueWarningIcon(SvgPreset preset);
    }

}
