/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.panels;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.commons.AnyDataProvider;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.commons.status.ConnObjectWrapper;
import org.apache.syncope.client.console.pages.BasePage;
import org.apache.syncope.client.console.rest.AbstractAnyRestClient;
import org.apache.syncope.client.console.rest.BaseRestClient;
import org.apache.syncope.client.console.wicket.ajax.form.AjaxDownloadBehavior;
import org.apache.syncope.client.console.rest.SchemaRestClient;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.AttrColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.BooleanPropertyColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.DatePropertyColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.KeyPropertyColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.TokenColumn;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.dialog.BaseModal;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.client.console.wizards.AjaxWizard;
import org.apache.syncope.client.console.wizards.CSVPullWizardBuilder;
import org.apache.syncope.client.console.wizards.CSVPushWizardBuilder;
import org.apache.syncope.client.console.wizards.any.AnyWrapper;
import org.apache.syncope.client.console.wizards.any.ProvisioningReportsPanel;
import org.apache.syncope.client.console.wizards.any.ResultPage;
import org.apache.syncope.client.console.wizards.any.StatusPanel;
import org.apache.syncope.common.lib.to.ConnObjectTO;
import org.apache.syncope.common.lib.to.ProvisioningReport;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.syncope.common.lib.to.AnyTypeClassTO;
import org.apache.syncope.common.lib.to.DerSchemaTO;
import org.apache.syncope.common.lib.to.PlainSchemaTO;
import org.apache.syncope.common.lib.to.ProvisioningResult;
import org.apache.syncope.common.lib.types.SchemaType;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.syncope.common.rest.api.beans.AnyQuery;
import org.apache.syncope.common.rest.api.beans.CSVPullSpec;
import org.apache.syncope.common.rest.api.beans.CSVPushSpec;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.springframework.util.ReflectionUtils;

public abstract class AnyDirectoryPanel<A extends AnyTO, E extends AbstractAnyRestClient<A>>
        extends DirectoryPanel<A, AnyWrapper<A>, AnyDataProvider<A>, E> {

    private static final long serialVersionUID = -1100228004207271270L;

    protected final SchemaRestClient schemaRestClient = new SchemaRestClient();

    protected final List<PlainSchemaTO> plainSchemas;

    protected final List<DerSchemaTO> derSchemas;

    /**
     * Filter used in case of filtered search.
     */
    protected String fiql;

    /**
     * Realm related to current panel.
     */
    protected final String realm;

    /**
     * Any type related to current panel.
     */
    protected final String type;

    protected final BaseModal<Serializable> utilityModal = new BaseModal<>(Constants.OUTER);

    protected final AjaxLink<Void> csvPushLink;

    protected final AjaxLink<Void> csvPullLink;

    protected AnyDirectoryPanel(final String id, final Builder<A, E> builder) {
        this(id, builder, true);
    }

    protected AnyDirectoryPanel(final String id, final Builder<A, E> builder, final boolean wizardInModal) {
        super(id, builder, wizardInModal);
        if (SyncopeConsoleSession.get().owns(String.format("%s_CREATE", builder.type), builder.realm)
                && builder.realm.startsWith(SyncopeConstants.ROOT_REALM)) {
            MetaDataRoleAuthorizationStrategy.authorizeAll(addAjaxLink, RENDER);
        } else {
            MetaDataRoleAuthorizationStrategy.unauthorizeAll(addAjaxLink, RENDER);
        }
        if (builder.dynRealm == null) {
            setReadOnly(!SyncopeConsoleSession.get().owns(String.format("%s_UPDATE", builder.type), builder.realm));
        } else {
            setReadOnly(!SyncopeConsoleSession.get().owns(String.format("%s_UPDATE", builder.type), builder.dynRealm));
        }

        realm = builder.realm;
        type = builder.type;
        fiql = builder.fiql;

        utilityModal.size(Modal.Size.Large);
        addOuterObject(utilityModal);
        setWindowClosedReloadCallback(utilityModal);

        modal.size(Modal.Size.Large);

        altDefaultModal.size(Modal.Size.Large);

        plainSchemas = AnyDirectoryPanelBuilder.class.cast(builder).getAnyTypeClassTOs().stream().
                flatMap(anyTypeClassTO -> anyTypeClassTO.getPlainSchemas().stream()).
                map(schema -> {
                    try {
                        return schemaRestClient.<PlainSchemaTO>read(SchemaType.PLAIN, schema);
                    } catch (SyncopeClientException e) {
                        LOG.warn("Could not read plain schema {}, ignoring", e);
                        return null;
                    }
                }).
                filter(Objects::nonNull).
                collect(Collectors.toList());

        derSchemas = AnyDirectoryPanelBuilder.class.cast(builder).getAnyTypeClassTOs().stream().
                flatMap(anyTypeClassTO -> anyTypeClassTO.getDerSchemas().stream()).
                map(schema -> {
                    try {
                        return schemaRestClient.<DerSchemaTO>read(SchemaType.DERIVED, schema);
                    } catch (SyncopeClientException e) {
                        LOG.warn("Could not read derived schema {}, ignoring", e);
                        return null;
                    }
                }).
                filter(Objects::nonNull).
                collect(Collectors.toList());

        initResultTable();

        AjaxDownloadBehavior csvDownloadBehavior = new AjaxDownloadBehavior();
        WebMarkupContainer csvEventSink = new WebMarkupContainer(Constants.OUTER) {

            private static final long serialVersionUID = -957948639666058749L;

            @Override
            public void onEvent(final IEvent<?> event) {
                if (event.getPayload() instanceof AjaxWizard.NewItemCancelEvent) {
                    ((AjaxWizard.NewItemCancelEvent<?>) event.getPayload()).getTarget().ifPresent(modal::close);
                } else if (event.getPayload() instanceof AjaxWizard.NewItemFinishEvent) {
                    AjaxWizard.NewItemFinishEvent<?> payload = (AjaxWizard.NewItemFinishEvent) event.getPayload();
                    Optional<AjaxRequestTarget> target = payload.getTarget();

                    if (payload.getResult() instanceof ArrayList) {
                        modal.setContent(new ResultPage<Serializable>(
                                null,
                                payload.getResult()) {

                            private static final long serialVersionUID = -2630573849050255233L;

                            @Override
                            protected void closeAction(final AjaxRequestTarget target) {
                                modal.close(target);
                            }

                            @Override
                            protected Panel customResultBody(
                                    final String id, final Serializable item, final Serializable result) {

                                @SuppressWarnings("unchecked")
                                ArrayList<ProvisioningReport> reports = (ArrayList<ProvisioningReport>) result;
                                return new ProvisioningReportsPanel(id, reports, pageRef);
                            }
                        });
                        target.ifPresent(t -> t.add(modal.getForm()));

                        SyncopeConsoleSession.get().success(getString(Constants.OPERATION_SUCCEEDED));
                    } else if (Constants.OPERATION_SUCCEEDED.equals(payload.getResult())) {
                        target.ifPresent(modal::close);
                        SyncopeConsoleSession.get().success(getString(Constants.OPERATION_SUCCEEDED));
                    } else if (payload.getResult() instanceof Exception) {
                        SyncopeConsoleSession.get().onException((Exception) payload.getResult());
                    } else {
                        SyncopeConsoleSession.get().error(payload.getResult());
                    }

                    if (target.isPresent()) {
                        ((BasePage) pageRef.getPage()).getNotificationPanel().refresh(target.get());
                        target.get().add(container);
                    }
                }
            }
        };
        csvEventSink.add(csvDownloadBehavior);
        addOuterObject(csvEventSink);
        csvPushLink = new AjaxLink<Void>("csvPush") {

            private static final long serialVersionUID = -817438685948164787L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                CSVPushSpec spec = csvPushSpec();
                AnyQuery query = csvAnyQuery();

                target.add(modal.setContent(new CSVPushWizardBuilder(spec, query, csvDownloadBehavior, pageRef).
                        setEventSink(csvEventSink).setAsync(false).
                        build(BaseModal.CONTENT_ID, AjaxWizard.Mode.EDIT)));

                modal.header(new StringResourceModel("csvPush", AnyDirectoryPanel.this, Model.of(spec)));
                modal.show(true);
            }
        };
        csvPushLink.setOutputMarkupPlaceholderTag(true).setVisible(wizardInModal).setEnabled(wizardInModal);
        MetaDataRoleAuthorizationStrategy.authorize(csvPushLink, RENDER,
                String.format("%s,%s", StandardEntitlement.IMPLEMENTATION_LIST, StandardEntitlement.TASK_EXECUTE));
        addInnerObject(csvPushLink.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));
        csvPullLink = new AjaxLink<Void>("csvPull") {

            private static final long serialVersionUID = -817438685948164787L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                CSVPullSpec spec = csvPullSpec();

                target.add(modal.setContent(new CSVPullWizardBuilder(spec, pageRef).
                        setEventSink(csvEventSink).
                        build(BaseModal.CONTENT_ID, AjaxWizard.Mode.EDIT)));

                modal.header(new StringResourceModel("csvPull", AnyDirectoryPanel.this, Model.of(spec)));
                modal.show(true);
            }
        };
        csvPullLink.setOutputMarkupPlaceholderTag(true).setVisible(wizardInModal).setEnabled(wizardInModal);
        MetaDataRoleAuthorizationStrategy.authorize(csvPullLink, RENDER,
                String.format("%s,%s", StandardEntitlement.IMPLEMENTATION_LIST, StandardEntitlement.TASK_EXECUTE));
        addInnerObject(csvPullLink.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));
    }

    protected CSVPushSpec csvPushSpec() {
        CSVPushSpec spec = new CSVPushSpec.Builder(type).build();
        spec.setFields(prefMan.getList(getRequest(), DisplayAttributesModalPanel.getPrefDetailView(type)).
                stream().filter(name -> !Constants.KEY_FIELD_NAME.equalsIgnoreCase(name)).
                collect(Collectors.toList()));
        spec.setPlainAttrs(
                prefMan.getList(getRequest(), DisplayAttributesModalPanel.getPrefPlainAttributeView(type)).stream().
                        filter(a -> plainSchemas.stream().anyMatch(p -> p.getKey().equals(a))).
                        collect(Collectors.toList()));
        spec.setDerAttrs(
                prefMan.getList(getRequest(), DisplayAttributesModalPanel.getPrefPlainAttributeView(type)).stream().
                        filter(a -> derSchemas.stream().anyMatch(p -> p.getKey().equals(a))).
                        collect(Collectors.toList()));
        return spec;
    }

    protected CSVPullSpec csvPullSpec() {
        CSVPullSpec spec = new CSVPullSpec();
        spec.setAnyTypeKey(type);
        spec.setDestinationRealm(realm);
        return spec;
    }

    protected AnyQuery csvAnyQuery() {
        return new AnyQuery.Builder().realm(realm).
                fiql(fiql).page(dataProvider.getCurrentPage() + 1).size(rows).
                orderBy(BaseRestClient.toOrderBy(dataProvider.getSort())).
                build();
    }

    @Override
    protected List<IColumn<A, String>> getColumns() {
        List<IColumn<A, String>> columns = new ArrayList<>();
        columns.add(new KeyPropertyColumn<>(
                new ResourceModel(Constants.KEY_FIELD_NAME, Constants.KEY_FIELD_NAME), Constants.KEY_FIELD_NAME));

        List<IColumn<A, String>> prefcolumns = new ArrayList<>();
        prefMan.getList(getRequest(), DisplayAttributesModalPanel.getPrefDetailView(type)).stream().
                filter(name -> !Constants.KEY_FIELD_NAME.equalsIgnoreCase(name)).
                forEach(name -> addPropertyColumn(
                name,
                ReflectionUtils.findField(DisplayAttributesModalPanel.getTOClass(type), name),
                prefcolumns));

        prefMan.getList(getRequest(), DisplayAttributesModalPanel.getPrefPlainAttributeView(type)).stream().
                map(a -> plainSchemas.stream().filter(p -> p.getKey().equals(a)).findFirst()).
                filter(Optional::isPresent).map(Optional::get).
                forEach(s -> prefcolumns.add(new AttrColumn<>(
                s.getKey(), s.getLabel(SyncopeConsoleSession.get().getLocale()), SchemaType.PLAIN)));

        prefMan.getList(getRequest(), DisplayAttributesModalPanel.getPrefDerivedAttributeView(type)).stream().
                map(a -> derSchemas.stream().filter(p -> p.getKey().equals(a)).findFirst()).
                filter(Optional::isPresent).map(Optional::get).
                forEach(s -> prefcolumns.add(new AttrColumn<>(
                s.getKey(), s.getLabel(SyncopeConsoleSession.get().getLocale()), SchemaType.DERIVED)));

        // Add defaults in case of no selection
        if (prefcolumns.isEmpty()) {
            for (String name : getDefaultAttributeSelection()) {
                addPropertyColumn(
                        name,
                        ReflectionUtils.findField(DisplayAttributesModalPanel.getTOClass(type), name),
                        prefcolumns);
            }

            prefMan.setList(
                    getRequest(), getResponse(), DisplayAttributesModalPanel.getPrefDetailView(type),
                    Arrays.asList(getDefaultAttributeSelection()));
        }

        columns.addAll(prefcolumns);
        return columns;
    }

    protected abstract String[] getDefaultAttributeSelection();

    protected void addPropertyColumn(
            final String name,
            final Field field,
            final List<IColumn<A, String>> columns) {

        if (Constants.KEY_FIELD_NAME.equalsIgnoreCase(name)) {
            columns.add(new KeyPropertyColumn<>(new ResourceModel(name, name), name, name));
        } else if (Constants.DEFAULT_TOKEN_FIELD_NAME.equalsIgnoreCase(name)) {
            columns.add(new TokenColumn<>(new ResourceModel(name, name), name));
        } else if (field != null && !field.isSynthetic()
                && (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class))) {

            columns.add(new BooleanPropertyColumn<>(new ResourceModel(name, name), name, name));
        } else if (field != null && !field.isSynthetic() && field.getType().equals(Date.class)) {
            columns.add(new DatePropertyColumn<>(new ResourceModel(name, name), name, name));
        } else {
            columns.add(new PropertyColumn<>(new ResourceModel(name, name), name, name));
        }
    }

    @Override
    protected AnyDataProvider<A> dataProvider() {
        return new AnyDataProvider<>(restClient, rows, filtered, realm, type, pageRef).setFIQL(this.fiql);
    }

    public void search(final String fiql, final AjaxRequestTarget target) {
        this.fiql = fiql;
        dataProvider.setFIQL(fiql);
        super.search(target);
    }

    @Override
    protected Collection<ActionLink.ActionType> getBatches() {
        List<ActionLink.ActionType> batches = new ArrayList<>();
        batches.add(ActionLink.ActionType.DELETE);
        return batches;
    }

    public interface AnyDirectoryPanelBuilder extends Serializable {

        List<AnyTypeClassTO> getAnyTypeClassTOs();
    }

    public abstract static class Builder<A extends AnyTO, E extends AbstractAnyRestClient<A>>
            extends DirectoryPanel.Builder<A, AnyWrapper<A>, E>
            implements AnyDirectoryPanelBuilder {

        private static final long serialVersionUID = -6828423611982275640L;

        /**
         * Realm related to current panel.
         */
        protected String realm = SyncopeConstants.ROOT_REALM;

        protected String dynRealm = null;

        /**
         * Any type related to current panel.
         */
        protected final String type;

        private final List<AnyTypeClassTO> anyTypeClassTOs;

        public Builder(
                final List<AnyTypeClassTO> anyTypeClassTOs,
                final E restClient,
                final String type,
                final PageReference pageRef) {

            super(restClient, pageRef);
            this.anyTypeClassTOs = anyTypeClassTOs;
            this.type = type;
        }

        public Builder<A, E> setRealm(final String realm) {
            this.realm = realm;
            return this;
        }

        public Builder<A, E> setDynRealm(final String dynRealm) {
            this.dynRealm = dynRealm;
            return this;
        }

        @Override
        public List<AnyTypeClassTO> getAnyTypeClassTOs() {
            return this.anyTypeClassTOs;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Panel customResultBody(final String panelId, final AnyWrapper<A> item, final Serializable result) {
        if (!(result instanceof ProvisioningResult)) {
            throw new IllegalStateException("Unsupported result type");
        }

        return new StatusPanel(
                panelId,
                ((ProvisioningResult<A>) result).getEntity(),
                new ListModel<>(new ArrayList<>()),
                ((ProvisioningResult<A>) result).getPropagationStatuses().stream().map(status -> {
                    ConnObjectTO before = status.getBeforeObj();
                    ConnObjectWrapper afterObjWrapper = new ConnObjectWrapper(
                            ((ProvisioningResult<A>) result).getEntity(),
                            status.getResource(),
                            status.getAfterObj());
                    return Triple.of(before, afterObjWrapper, status.getFailureReason());
                }).collect(Collectors.toList()),
                pageRef);
    }
}
