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
package org.apache.syncope.client.console.widgets;

import java.util.List;
import org.apache.syncope.client.console.SyncopeConsoleApplication;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.pages.BasePage;
import org.apache.syncope.client.console.rest.AnyTypeRestClient;
import org.apache.syncope.client.console.topology.TabularTopology;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.syncope.client.console.pages.Realms;
import org.apache.syncope.client.console.pages.Security;
import org.apache.syncope.client.console.topology.Topology;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class NumberWidget extends BaseWidget {

    private static final long serialVersionUID = -816175678514035085L;

    private int number;

    private final Label numberLabel;

    public NumberWidget(final String id, final String bg, final int number, final String label, final String icon) {
        super(id);
        this.number = number;
        setOutputMarkupId(true);

        WebMarkupContainer box = new WebMarkupContainer("box");
        box.add(new AttributeAppender("class", " " + bg));

        @SuppressWarnings("unchecked")
        Class<? extends BasePage> realmsPage =
                (Class<? extends BasePage>) SyncopeConsoleApplication.get().getPageClass("realms");
        if (realmsPage == null) {
            realmsPage = Realms.class;
        }

        boolean isAuthorized = true;
        PageParameters pageParameters = new PageParameters();
        Class<? extends BasePage> responsePage;
        List<String> anyTypes = new AnyTypeRestClient().list();
        switch (id) {
            case "totalUsers":
                pageParameters.add(Realms.SELECTED_INDEX, 1);
                responsePage = realmsPage;
                isAuthorized = SyncopeConsoleSession.get().owns(StandardEntitlement.USER_SEARCH);
                break;

            case "totalGroups":
                pageParameters.add(Realms.SELECTED_INDEX, 2);
                responsePage = realmsPage;
                isAuthorized = SyncopeConsoleSession.get().owns(StandardEntitlement.GROUP_SEARCH);
                break;

            case "totalAny1OrRoles":
                if (icon.equals("ion ion-gear-a")) {
                    Integer selectedIndex = null;
                    for (int i = 0; i < anyTypes.size() && selectedIndex == null; i++) {
                        if (anyTypes.get(i).equals(label)) {
                            selectedIndex = i + 1;
                            pageParameters.add(Realms.SELECTED_INDEX, selectedIndex);
                        }
                    }
                    responsePage = realmsPage;
                    isAuthorized = SyncopeConsoleSession.get().owns(label + "_SEARCH");
                } else {
                    responsePage = Security.class;
                    isAuthorized = SyncopeConsoleSession.get().owns(StandardEntitlement.ROLE_LIST);
                }
                break;

            case "totalAny2OrResources":
                if (icon.equals("ion ion-gear-a")) {
                    Integer selectedIndex = null;
                    for (int i = 0; i < anyTypes.size() && selectedIndex == null; i++) {
                        if (anyTypes.get(i).equals(label)) {
                            selectedIndex = i + 1;
                            pageParameters.add(Realms.SELECTED_INDEX, selectedIndex);
                        }
                    }
                    responsePage = Realms.class;
                    isAuthorized = SyncopeConsoleSession.get().owns(label + "_SEARCH");
                } else {
                    if (SyncopeConsoleApplication.get().getDefaultTopologyClass().contains("TabularTopology")) {
                        responsePage = TabularTopology.class;
                    } else {
                        responsePage = Topology.class;
                    }
                    isAuthorized = SyncopeConsoleSession.get().owns(StandardEntitlement.CONNECTOR_LIST)
                            && SyncopeConsoleSession.get().owns(StandardEntitlement.RESOURCE_LIST);
                }
                break;

            default:
                pageParameters.add(Realms.SELECTED_INDEX, 0);
                responsePage = realmsPage;
        }

        AjaxEventBehavior clickToRealms = new AjaxEventBehavior("mousedown") {

            private static final long serialVersionUID = -7133385027739964990L;

            @Override
            protected void onEvent(final AjaxRequestTarget target) {
                setResponsePage(responsePage, pageParameters);
            }
        };
        if (isAuthorized) {
            box.add(clickToRealms);
        }

        add(box);

        numberLabel = new Label("number", number);
        numberLabel.setOutputMarkupId(true);
        box.add(numberLabel);
        box.add(new Label("label", label));

        Label iconLabel = new Label("icon");
        iconLabel.add(new AttributeAppender("class", icon));
        box.add(iconLabel);
    }

    public boolean refresh(final int number) {
        if (this.number != number) {
            this.number = number;
            numberLabel.setDefaultModelObject(number);
            return true;
        }
        return false;
    }
}
