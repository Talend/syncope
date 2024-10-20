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
package org.apache.syncope.core.logic;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.common.lib.patch.AnyObjectPatch;
import org.apache.syncope.common.lib.patch.AnyPatch;
import org.apache.syncope.common.lib.patch.GroupPatch;
import org.apache.syncope.common.lib.patch.UserPatch;
import org.apache.syncope.common.lib.to.AnyObjectTO;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.syncope.common.lib.to.GroupTO;
import org.apache.syncope.common.lib.to.ProvisioningResult;
import org.apache.syncope.common.lib.to.RemediationTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.core.persistence.api.dao.RemediationDAO;
import org.apache.syncope.core.persistence.api.dao.search.OrderByClause;
import org.apache.syncope.core.persistence.api.entity.Remediation;
import org.apache.syncope.core.provisioning.api.data.RemediationDataBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RemediationLogic extends AbstractLogic<RemediationTO> {

    @Autowired
    private UserLogic userLogic;

    @Autowired
    private GroupLogic groupLogic;

    @Autowired
    private AnyObjectLogic anyObjectLogic;

    @Autowired
    private RemediationDataBinder binder;

    @Autowired
    private RemediationDAO remediationDAO;

    @PreAuthorize("hasRole('" + StandardEntitlement.REMEDIATION_LIST + "')")
    @Transactional(readOnly = true)
    public Pair<Integer, List<RemediationTO>> list(
            final Date before,
            final Date after,
            final int page,
            final int size,
            final List<OrderByClause> orderByClauses) {

        int count = remediationDAO.count(before, after);

        List<RemediationTO> result = remediationDAO.findAll(before, after, page, size, orderByClauses).stream().
                map(binder::getRemediationTO).collect(Collectors.toList());

        return Pair.of(count, result);
    }

    @PreAuthorize("hasRole('" + StandardEntitlement.REMEDIATION_READ + "')")
    @Transactional(readOnly = true)
    public RemediationTO read(final String key) {
        Remediation remediation = Optional.ofNullable(remediationDAO.find(key)).
                orElseThrow(() -> new NotFoundException(key));

        return binder.getRemediationTO(remediation);
    }

    @PreAuthorize("hasRole('" + StandardEntitlement.REMEDIATION_DELETE + "')")
    @Transactional
    public void delete(final String key) {
        Optional.ofNullable(remediationDAO.find(key)).
                orElseThrow(() -> new NotFoundException(key));

        remediationDAO.delete(key);
    }

    @PreAuthorize("hasRole('" + StandardEntitlement.REMEDIATION_REMEDY + "')")
    public ProvisioningResult<?> remedy(final String key, final AnyTO anyTO, final boolean nullPriorityAsync) {
        ProvisioningResult<?> result;
        switch (read(key).getAnyType()) {
            case "USER":
                result = userLogic.create((UserTO) anyTO, true, nullPriorityAsync);
                break;

            case "GROUP":
                result = groupLogic.create((GroupTO) anyTO, nullPriorityAsync);
                break;

            default:
                result = anyObjectLogic.create((AnyObjectTO) anyTO, nullPriorityAsync);
        }

        remediationDAO.delete(key);

        return result;
    }

    @PreAuthorize("hasRole('" + StandardEntitlement.REMEDIATION_REMEDY + "')")
    public ProvisioningResult<?> remedy(final String key, final AnyPatch anyPatch, final boolean nullPriorityAsync) {
        ProvisioningResult<?> result;
        switch (read(key).getAnyType()) {
            case "USER":
                result = userLogic.update((UserPatch) anyPatch, nullPriorityAsync);
                break;

            case "GROUP":
                result = groupLogic.update((GroupPatch) anyPatch, nullPriorityAsync);
                break;

            default:
                result = anyObjectLogic.update((AnyObjectPatch) anyPatch, nullPriorityAsync);
        }

        remediationDAO.delete(key);

        return result;
    }

    @PreAuthorize("hasRole('" + StandardEntitlement.REMEDIATION_REMEDY + "')")
    public ProvisioningResult<?> remedy(final String key, final String anyKey, final boolean nullPriorityAsync) {
        ProvisioningResult<?> result;
        switch (read(key).getAnyType()) {
            case "USER":
                result = userLogic.delete(anyKey, nullPriorityAsync);
                break;

            case "GROUP":
                result = groupLogic.delete(anyKey, nullPriorityAsync);
                break;

            default:
                result = anyObjectLogic.delete(anyKey, nullPriorityAsync);
        }

        remediationDAO.delete(key);

        return result;
    }

    @Override
    protected RemediationTO resolveReference(final Method method, final Object... args)
            throws UnresolvedReferenceException {

        String key = null;

        if (ArrayUtils.isNotEmpty(args)) {
            for (int i = 0; key == null && i < args.length; i++) {
                if (args[i] instanceof String) {
                    key = (String) args[i];
                } else if (args[i] instanceof RemediationTO) {
                    key = ((RemediationTO) args[i]).getKey();
                }
            }
        }

        if (StringUtils.isNotBlank(key)) {
            try {
                return binder.getRemediationTO(remediationDAO.find(key));
            } catch (Throwable ignore) {
                LOG.debug("Unresolved reference", ignore);
                throw new UnresolvedReferenceException(ignore);
            }
        }

        throw new UnresolvedReferenceException();
    }
}
