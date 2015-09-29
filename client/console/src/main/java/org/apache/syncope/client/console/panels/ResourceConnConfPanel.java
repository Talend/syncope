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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.syncope.client.console.rest.ConnectorRestClient;
import org.apache.syncope.common.lib.to.ResourceTO;
import org.apache.syncope.common.lib.types.ConnConfProperty;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public abstract class ResourceConnConfPanel extends AbstractConnectorConfPanel<ResourceTO> {

    private static final long serialVersionUID = -7982691107029848579L;

    @SpringBean
    private ConnectorRestClient restClient;

    private final boolean createFlag;

    public ResourceConnConfPanel(final String id, final IModel<ResourceTO> model, final boolean createFlag) {
        super(id, model);

        this.createFlag = createFlag;

        final List<ConnConfProperty> connConfProperties = getConnProperties(model.getObject());
        model.getObject().getConnConfProperties().clear();
        model.getObject().getConnConfProperties().addAll(connConfProperties);

        setConfPropertyListView("connConfProperties", false);

        check.setEnabled(!connConfProperties.isEmpty());
        check.setVisible(!connConfProperties.isEmpty());
    }

    /**
     * Get overridable properties.
     *
     * @param resourceTO resource instance.
     * @return overridable properties.
     */
    @Override
    protected final List<ConnConfProperty> getConnProperties(final ResourceTO resourceTO) {
        List<ConnConfProperty> props = new ArrayList<>();
        Long connectorKey = resourceTO.getConnector();
        if (connectorKey != null && connectorKey > 0) {
            for (ConnConfProperty property : restClient.getConnectorProperties(connectorKey)) {
                if (property.isOverridable()) {
                    props.add(property);
                }
            }
        }
        if (createFlag || resourceTO.getConnConfProperties().isEmpty()) {
            resourceTO.getConnConfProperties().clear();
        } else {
            Map<String, ConnConfProperty> valuedProps = new HashMap<>();
            for (ConnConfProperty prop : resourceTO.getConnConfProperties()) {
                valuedProps.put(prop.getSchema().getName(), prop);
            }

            for (int i = 0; i < props.size(); i++) {
                if (valuedProps.containsKey(props.get(i).getSchema().getName())) {
                    props.set(i, valuedProps.get(props.get(i).getSchema().getName()));
                }
            }
        }

        return props;
    }
}