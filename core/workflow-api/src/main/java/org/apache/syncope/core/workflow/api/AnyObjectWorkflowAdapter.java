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
package org.apache.syncope.core.workflow.api;

import org.apache.syncope.common.lib.patch.AnyObjectPatch;
import org.apache.syncope.core.provisioning.api.WorkflowResult;
import org.apache.syncope.common.lib.to.AnyObjectTO;

/**
 * Interface for calling underlying workflow implementations.
 */
public interface AnyObjectWorkflowAdapter extends WorkflowAdapter {

    /**
     * Create an anyObject.
     *
     * @param anyObjectTO anyObject to be created and whether to propagate it as active
     * @return anyObject just created
     */
    WorkflowResult<String> create(AnyObjectTO anyObjectTO);

    /**
     * Update an anyObject.
     *
     * @param anyObjectPatch modification set to be performed
     * @return anyObject just updated and propagations to be performed
     */
    WorkflowResult<AnyObjectPatch> update(AnyObjectPatch anyObjectPatch);

    /**
     * Delete an anyObject.
     *
     * @param anyObjectKey anyObject to be deleted
     */
    void delete(String anyObjectKey);
}
