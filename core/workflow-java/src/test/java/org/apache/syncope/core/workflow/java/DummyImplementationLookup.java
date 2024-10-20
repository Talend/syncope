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
package org.apache.syncope.core.workflow.java;

import java.util.Collections;
import java.util.Set;
import org.apache.syncope.common.lib.policy.AccountRuleConf;
import org.apache.syncope.common.lib.policy.PasswordRuleConf;
import org.apache.syncope.common.lib.policy.PullCorrelationRuleConf;
import org.apache.syncope.common.lib.policy.PushCorrelationRuleConf;
import org.apache.syncope.common.lib.report.ReportletConf;
import org.apache.syncope.common.lib.types.ImplementationType;
import org.apache.syncope.core.persistence.api.ImplementationLookup;
import org.apache.syncope.core.persistence.api.dao.AccountRule;
import org.apache.syncope.core.persistence.api.dao.PasswordRule;
import org.apache.syncope.core.persistence.api.dao.PullCorrelationRule;
import org.apache.syncope.core.persistence.api.dao.PushCorrelationRule;
import org.apache.syncope.core.persistence.api.dao.Reportlet;
import org.apache.syncope.core.persistence.jpa.dao.DefaultPullCorrelationRule;
import org.apache.syncope.core.persistence.jpa.dao.DefaultPushCorrelationRule;
import org.apache.syncope.core.spring.policy.DefaultAccountRule;
import org.apache.syncope.core.spring.policy.DefaultPasswordRule;

public class DummyImplementationLookup implements ImplementationLookup {

    @Override
    public Integer getPriority() {
        return -1;
    }

    @Override
    public void load() {
        // do nothing
    }

    @Override
    public Set<String> getClassNames(final ImplementationType type) {
        return Collections.emptySet();
    }

    @Override
    public Set<Class<?>> getJWTSSOProviderClasses() {
        return Collections.emptySet();
    }

    @Override
    public Class<Reportlet> getReportletClass(
            final Class<? extends ReportletConf> reportletConfClass) {

        return null;
    }

    @Override
    public Class<? extends AccountRule> getAccountRuleClass(
            final Class<? extends AccountRuleConf> accountRuleConfClass) {

        return DefaultAccountRule.class;
    }

    @Override
    public Class<? extends PasswordRule> getPasswordRuleClass(
            final Class<? extends PasswordRuleConf> passwordRuleConfClass) {

        return DefaultPasswordRule.class;
    }

    @Override
    public Class<? extends PullCorrelationRule> getPullCorrelationRuleClass(
            final Class<? extends PullCorrelationRuleConf> pullCorrelationRuleConfClass) {

        return DefaultPullCorrelationRule.class;
    }

    @Override
    public Class<? extends PushCorrelationRule> getPushCorrelationRuleClass(
            final Class<? extends PushCorrelationRuleConf> pushCorrelationRuleConfClass) {

        return DefaultPushCorrelationRule.class;
    }

    @Override
    public Set<Class<?>> getAuditAppenderClasses() {
        return Collections.emptySet();
    }
}
