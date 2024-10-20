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
package org.apache.syncope.core.persistence.api.dao;

import java.util.Date;
import java.util.List;
import org.apache.syncope.core.persistence.api.dao.search.OrderByClause;
import org.apache.syncope.core.persistence.api.entity.AnyType;
import org.apache.syncope.core.persistence.api.entity.Remediation;
import org.apache.syncope.core.persistence.api.entity.task.PullTask;

public interface RemediationDAO extends DAO<Remediation> {

    Remediation find(String key);

    List<Remediation> findByAnyType(AnyType anyType);

    List<Remediation> findByPullTask(PullTask pullTask);

    int count(Date before, Date after);

    List<Remediation> findAll(
            Date before,
            Date after,
            int page,
            int itemsPerPage,
            List<OrderByClause> orderByClauses);

    Remediation save(Remediation remediation);

    void delete(Remediation remediation);

    void delete(String key);

}
