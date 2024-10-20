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
package org.apache.syncope.fit.core;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.syncope.client.lib.SyncopeClient;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.log.AuditEntry;
import org.apache.syncope.common.lib.to.AnyObjectTO;
import org.apache.syncope.common.lib.to.ConnInstanceTO;
import org.apache.syncope.common.lib.to.GroupTO;
import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.AuditElements;
import org.apache.syncope.common.lib.types.ConnConfProperty;
import org.apache.syncope.common.lib.types.ConnectorCapability;
import org.apache.syncope.common.rest.api.beans.AnyQuery;
import org.apache.syncope.common.rest.api.beans.AuditQuery;
import org.apache.syncope.core.logic.ConnectorLogic;
import org.apache.syncope.core.logic.UserLogic;
import org.apache.syncope.fit.AbstractITCase;
import org.junit.jupiter.api.Test;

public class AuditITCase extends AbstractITCase {

    private static AuditEntry queryWithFailure(final AuditQuery query, final int maxWaitSeconds) {
        List<AuditEntry> results = query(query, maxWaitSeconds);
        if (results.isEmpty()) {
            fail("Timeout when executing query for key " + query.getEntityKey());
            return null;
        }
        return results.get(0);
    }

    @Test
    public void userReadAndSearchYieldsNoAudit() {
        UserTO userTO = createUser(UserITCase.getUniqueSampleTO("audit@syncope.org")).getEntity();
        assertNotNull(userTO.getKey());

        AuditQuery query = new AuditQuery.Builder().entityKey(userTO.getKey()).build();
        int entriesBefore = query(query, MAX_WAIT_SECONDS).size();

        PagedResult<UserTO> usersTOs = userService.search(
                new AnyQuery.Builder().realm(SyncopeConstants.ROOT_REALM).
                        fiql(SyncopeClient.getUserSearchConditionBuilder().
                                is("username").equalTo(userTO.getUsername()).query()).
                        build());
        assertNotNull(usersTOs);
        assertFalse(usersTOs.getResult().isEmpty());

        int entriesAfter = query(query, MAX_WAIT_SECONDS).size();
        assertEquals(entriesBefore, entriesAfter);
    }

    @Test
    public void findByUser() {
        UserTO userTO = createUser(UserITCase.getUniqueSampleTO("audit@syncope.org")).getEntity();
        assertNotNull(userTO.getKey());

        AuditQuery query = new AuditQuery.Builder().
                entityKey(userTO.getKey()).
                before(DateUtils.addSeconds(new Date(), 30)).
                page(1).
                size(1).
                orderBy("event_date desc").
                build();
        AuditEntry entry = queryWithFailure(query, MAX_WAIT_SECONDS);
        assertNotNull(entry);
        userService.delete(userTO.getKey());
    }

    @Test
    public void findByUserAndOther() {
        UserTO userTO = createUser(UserITCase.getUniqueSampleTO("audit-2@syncope.org")).getEntity();
        assertNotNull(userTO.getKey());

        AuditQuery query = new AuditQuery.Builder().
                entityKey(userTO.getKey()).
                orderBy("event_date desc").
                page(1).
                size(1).
                type(AuditElements.EventCategoryType.LOGIC).
                category(UserLogic.class.getSimpleName()).
                event("create").
                result(AuditElements.Result.SUCCESS).
                after(DateUtils.addSeconds(new Date(), -30)).
                build();
        AuditEntry entry = queryWithFailure(query, MAX_WAIT_SECONDS);
        assertNotNull(entry);
        userService.delete(userTO.getKey());
    }

    @Test
    public void findByGroup() {
        GroupTO groupTO = createGroup(GroupITCase.getBasicSampleTO("AuditGroup")).getEntity();
        assertNotNull(groupTO.getKey());

        AuditQuery query = new AuditQuery.Builder().entityKey(groupTO.getKey()).orderBy("event_date desc").
                page(1).size(1).build();
        AuditEntry entry = queryWithFailure(query, MAX_WAIT_SECONDS);
        assertNotNull(entry);
        groupService.delete(groupTO.getKey());
    }

    @Test
    public void groupReadAndSearchYieldsNoAudit() {
        GroupTO groupTO = createGroup(GroupITCase.getBasicSampleTO("AuditGroupSearch")).getEntity();
        assertNotNull(groupTO.getKey());

        AuditQuery query = new AuditQuery.Builder().entityKey(groupTO.getKey()).build();
        int entriesBefore = query(query, MAX_WAIT_SECONDS).size();

        PagedResult<GroupTO> groups = groupService.search(new AnyQuery.Builder().realm(SyncopeConstants.ROOT_REALM).
                fiql(SyncopeClient.getGroupSearchConditionBuilder().is("name").equalTo(groupTO.getName()).query()).
                build());
        assertNotNull(groups);
        assertFalse(groups.getResult().isEmpty());

        int entriesAfter = query(query, MAX_WAIT_SECONDS).size();
        assertEquals(entriesBefore, entriesAfter);
    }

    @Test
    public void findByAnyObject() {
        AnyObjectTO anyObjectTO = createAnyObject(AnyObjectITCase.getSampleTO("Italy")).getEntity();
        assertNotNull(anyObjectTO.getKey());
        AuditQuery query = new AuditQuery.Builder().entityKey(anyObjectTO.getKey()).
                orderBy("event_date desc").page(1).size(1).build();
        AuditEntry entry = queryWithFailure(query, MAX_WAIT_SECONDS);
        assertNotNull(entry);
        anyObjectService.delete(anyObjectTO.getKey());
    }

    @Test
    public void anyObjectReadAndSearchYieldsNoAudit() {
        AnyObjectTO anyObjectTO = AnyObjectITCase.getSampleTO("USA");
        anyObjectTO = createAnyObject(anyObjectTO).getEntity();
        assertNotNull(anyObjectTO);

        AuditQuery query = new AuditQuery.Builder().entityKey(anyObjectTO.getKey()).build();
        int entriesBefore = query(query, MAX_WAIT_SECONDS).size();

        PagedResult<AnyObjectTO> anyObjects = anyObjectService.search(
                new AnyQuery.Builder().realm(SyncopeConstants.ROOT_REALM).
                        fiql(SyncopeClient.getAnyObjectSearchConditionBuilder(anyObjectTO.getType()).query()).
                        build());
        assertNotNull(anyObjects);
        assertFalse(anyObjects.getResult().isEmpty());

        int entriesAfter = query(query, MAX_WAIT_SECONDS).size();
        assertEquals(entriesBefore, entriesAfter);
    }

    @Test
    public void findByConnector() throws JsonProcessingException {
        String connectorKey = "74141a3b-0762-4720-a4aa-fc3e374ef3ef";

        AuditQuery query = new AuditQuery.Builder().
                entityKey(connectorKey).
                orderBy("event_date desc").
                type(AuditElements.EventCategoryType.LOGIC).
                category(ConnectorLogic.class.getSimpleName()).
                event("update").
                result(AuditElements.Result.SUCCESS).
                build();
        List<AuditEntry> entries = query(query, 0);
        int pre = entries.size();

        ConnInstanceTO ldapConn = connectorService.read(connectorKey, null);
        String originalDisplayName = ldapConn.getDisplayName();
        Set<ConnectorCapability> originalCapabilities = new HashSet<>(ldapConn.getCapabilities());
        ConnConfProperty originalConfProp = SerializationUtils.clone(
                ldapConn.getConf("maintainPosixGroupMembership").get());
        assertEquals(1, originalConfProp.getValues().size());
        assertEquals("false", originalConfProp.getValues().get(0));

        ldapConn.setDisplayName(originalDisplayName + " modified");
        ldapConn.getCapabilities().clear();
        ldapConn.getConf("maintainPosixGroupMembership").get().getValues().set(0, "true");
        connectorService.update(ldapConn);

        ldapConn = connectorService.read(connectorKey, null);
        assertNotEquals(originalDisplayName, ldapConn.getDisplayName());
        assertNotEquals(originalCapabilities, ldapConn.getCapabilities());
        assertNotEquals(originalConfProp, ldapConn.getConf("maintainPosixGroupMembership"));

        entries = query(query, MAX_WAIT_SECONDS);
        assertEquals(pre + 1, entries.size());

        ConnInstanceTO restore = OBJECT_MAPPER.readValue(entries.get(0).getBefore(), ConnInstanceTO.class);
        connectorService.update(restore);

        ldapConn = connectorService.read(connectorKey, null);
        assertEquals(originalDisplayName, ldapConn.getDisplayName());
        assertEquals(originalCapabilities, ldapConn.getCapabilities());
        assertEquals(originalConfProp, ldapConn.getConf("maintainPosixGroupMembership").get());
    }
}
