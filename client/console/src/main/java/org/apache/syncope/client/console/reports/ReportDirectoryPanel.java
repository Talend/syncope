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
package org.apache.syncope.client.console.reports;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.commons.DirectoryDataProvider;
import org.apache.syncope.client.console.commons.SortableDataProviderComparator;
import org.apache.syncope.client.console.pages.BasePage;
import org.apache.syncope.client.console.panels.DirectoryPanel;
import org.apache.syncope.client.console.panels.MultilevelPanel;
import org.apache.syncope.client.console.rest.ReportRestClient;
import org.apache.syncope.client.console.wicket.ajax.IndicatorAjaxTimerBehavior;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.BooleanPropertyColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.DatePropertyColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.KeyPropertyColumn;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink.ActionType;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionsPanel;
import org.apache.syncope.client.console.widgets.JobActionPanel;
import org.apache.syncope.client.console.wizards.AjaxWizard;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.JobTO;
import org.apache.syncope.common.lib.to.ReportTO;
import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.time.Duration;

/**
 * Reports page.
 */
public abstract class ReportDirectoryPanel
        extends DirectoryPanel<ReportTO, ReportTO, DirectoryDataProvider<ReportTO>, ReportRestClient> {

    private static final long serialVersionUID = 4984337552918213290L;

    private final ReportStartAtTogglePanel startAt;

    protected ReportDirectoryPanel(final MultilevelPanel multiLevelPanelRef, final PageReference pageRef) {
        super(MultilevelPanel.FIRST_LEVEL_ID, pageRef, true);
        this.restClient = new ReportRestClient();

        this.addNewItemPanelBuilder(new ReportWizardBuilder(new ReportTO(), pageRef), true);
        MetaDataRoleAuthorizationStrategy.authorize(addAjaxLink, RENDER, StandardEntitlement.REPORT_CREATE);

        modal.size(Modal.Size.Large);
        initResultTable();

        container.add(new IndicatorAjaxTimerBehavior(Duration.seconds(10)) {

            private static final long serialVersionUID = -4661303265651934868L;

            @Override
            protected void onTimer(final AjaxRequestTarget target) {
                container.modelChanged();
                target.add(container);
            }
        });

        startAt = new ReportStartAtTogglePanel(container, pageRef);
        addInnerObject(startAt);
    }

    @Override
    protected List<IColumn<ReportTO, String>> getColumns() {
        final List<IColumn<ReportTO, String>> columns = new ArrayList<>();

        columns.add(new KeyPropertyColumn<>(
                new StringResourceModel(Constants.KEY_FIELD_NAME, this), Constants.KEY_FIELD_NAME));
        columns.add(new PropertyColumn<>(new StringResourceModel("name", this), "name", "name"));

        columns.add(new DatePropertyColumn<>(
                new StringResourceModel("lastExec", this), null, "lastExec"));

        columns.add(new DatePropertyColumn<>(
                new StringResourceModel("nextExec", this), null, "nextExec"));

        columns.add(new DatePropertyColumn<>(
                new StringResourceModel("start", this), "start", "start"));

        columns.add(new DatePropertyColumn<>(
                new StringResourceModel("end", this), "end", "end"));

        columns.add(new PropertyColumn<>(
                new StringResourceModel("latestExecStatus", this), "latestExecStatus", "latestExecStatus"));

        columns.add(new BooleanPropertyColumn<>(
                new StringResourceModel("active", this), "active", "active"));

        columns.add(new AbstractColumn<ReportTO, String>(new Model<>(""), "running") {

            private static final long serialVersionUID = 4209532514416998046L;

            @Override
            public void populateItem(
                    final Item<ICellPopulator<ReportTO>> cellItem,
                    final String componentId,
                    final IModel<ReportTO> rowModel) {

                Component panel;
                try {
                    JobTO jobTO = restClient.getJob(rowModel.getObject().getKey());
                    panel = new JobActionPanel(componentId, jobTO, false, ReportDirectoryPanel.this, pageRef);
                    MetaDataRoleAuthorizationStrategy.authorize(panel, WebPage.ENABLE,
                            String.format("%s,%s",
                                    StandardEntitlement.REPORT_EXECUTE,
                                    StandardEntitlement.REPORT_UPDATE));
                } catch (Exception e) {
                    LOG.error("Could not get job for report {}", rowModel.getObject().getKey(), e);
                    panel = new Label(componentId, Model.of());
                }
                cellItem.add(panel);
            }

            @Override
            public String getCssClass() {
                return "col-xs-1";
            }
        });

        return columns;
    }

    @Override
    public void onEvent(final IEvent<?> event) {
        if (event.getPayload() instanceof JobActionPanel.JobActionPayload) {
            container.modelChanged();
            JobActionPanel.JobActionPayload.class.cast(event.getPayload()).getTarget().add(container);
        } else {
            super.onEvent(event);
        }
    }

    @Override
    public ActionsPanel<ReportTO> getActions(final IModel<ReportTO> model) {
        final ActionsPanel<ReportTO> panel = super.getActions(model);

        panel.add(new ActionLink<ReportTO>() {

            private static final long serialVersionUID = -3722207913631435501L;

            @Override
            public void onClick(final AjaxRequestTarget target, final ReportTO ignore) {
                send(ReportDirectoryPanel.this, Broadcast.EXACT,
                        new AjaxWizard.EditItemActionEvent<>(
                                restClient.read(model.getObject().getKey()), target));
            }
        }, ActionLink.ActionType.EDIT, StandardEntitlement.REPORT_UPDATE);

        panel.add(new ActionLink<ReportTO>() {

            private static final long serialVersionUID = -3722207913631435501L;

            @Override
            public void onClick(final AjaxRequestTarget target, final ReportTO ignore) {
                final ReportTO clone = SerializationUtils.clone(model.getObject());
                clone.setKey(null);
                send(ReportDirectoryPanel.this, Broadcast.EXACT,
                        new AjaxWizard.EditItemActionEvent<>(clone, target));
            }
        }, ActionLink.ActionType.CLONE, StandardEntitlement.REPORT_CREATE);

        panel.add(new ActionLink<ReportTO>() {

            private static final long serialVersionUID = -3722207913631435501L;

            @Override
            public void onClick(final AjaxRequestTarget target, final ReportTO ignore) {
                target.add(modal.setContent(new ReportletDirectoryPanel(
                        modal, model.getObject().getKey(), pageRef)));

                modal.header(new StringResourceModel(
                        "reportlet.conf", ReportDirectoryPanel.this, Model.of(model.getObject())));

                MetaDataRoleAuthorizationStrategy.authorize(
                        modal.getForm(), ENABLE, StandardEntitlement.REPORT_UPDATE);

                modal.show(true);
            }
        }, ActionLink.ActionType.COMPOSE, StandardEntitlement.REPORT_UPDATE);

        panel.add(new ActionLink<ReportTO>() {

            private static final long serialVersionUID = -3722207913631435501L;

            @Override
            public void onClick(final AjaxRequestTarget target, final ReportTO ignore) {
                viewReport(model.getObject(), target);
            }
        }, ActionLink.ActionType.VIEW_EXECUTIONS, StandardEntitlement.REPORT_READ);

        panel.add(new ActionLink<ReportTO>() {

            private static final long serialVersionUID = -3722207913631435501L;

            @Override
            public void onClick(final AjaxRequestTarget target, final ReportTO ignore) {
                startAt.setExecutionDetail(
                        model.getObject().getKey(), model.getObject().getName(), target);
                startAt.toggle(target, true);
            }
        }, ActionLink.ActionType.EXECUTE, StandardEntitlement.REPORT_EXECUTE);

        panel.add(new ActionLink<ReportTO>() {

            private static final long serialVersionUID = -3722207913631435501L;

            @Override
            public void onClick(final AjaxRequestTarget target, final ReportTO ignore) {
                final ReportTO reportTO = model.getObject();
                try {
                    restClient.delete(reportTO.getKey());
                    SyncopeConsoleSession.get().success(getString(Constants.OPERATION_SUCCEEDED));
                    target.add(container);
                } catch (SyncopeClientException e) {
                    LOG.error("While deleting {}", reportTO.getKey(), e);
                    SyncopeConsoleSession.get().onException(e);
                }
                ((BasePage) pageRef.getPage()).getNotificationPanel().refresh(target);
            }
        }, ActionLink.ActionType.DELETE, StandardEntitlement.REPORT_DELETE, true);
        return panel;
    }

    @Override
    protected Collection<ActionType> getBatches() {
        List<ActionType> batches = new ArrayList<>();
        batches.add(ActionType.EXECUTE);
        batches.add(ActionType.DELETE);
        return batches;
    }

    @Override
    protected ReportDataProvider dataProvider() {
        return new ReportDataProvider(rows);
    }

    @Override
    protected String paginatorRowsKey() {
        return Constants.PREF_REPORT_TASKS_PAGINATOR_ROWS;
    }

    protected abstract void viewReport(ReportTO reportTO, AjaxRequestTarget target);

    protected class ReportDataProvider extends DirectoryDataProvider<ReportTO> {

        private static final long serialVersionUID = 4725679400450513556L;

        private final SortableDataProviderComparator<ReportTO> comparator;

        public ReportDataProvider(final int paginatorRows) {
            super(paginatorRows);

            setSort("name", SortOrder.ASCENDING);
            comparator = new SortableDataProviderComparator<>(this);
        }

        @Override
        public Iterator<ReportTO> iterator(final long first, final long count) {
            List<ReportTO> list = restClient.list();
            Collections.sort(list, comparator);
            return list.subList((int) first, (int) first + (int) count).iterator();
        }

        @Override
        public long size() {
            return restClient.list().size();
        }

        @Override
        public IModel<ReportTO> model(final ReportTO object) {
            return new CompoundPropertyModel<>(object);
        }
    }
}
