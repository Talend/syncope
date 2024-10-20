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
package org.apache.syncope.core.provisioning.java.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.syncope.common.lib.patch.MembershipPatch;
import org.apache.syncope.common.lib.patch.UserPatch;
import org.apache.syncope.common.lib.types.PatchOperation;
import org.apache.syncope.core.provisioning.api.UserProvisioningManager;
import org.apache.syncope.core.provisioning.api.job.JobManager;
import org.apache.syncope.core.spring.ApplicationContextProvider;
import org.apache.syncope.core.spring.security.AuthContextUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Quartz Job used for setting user memberships asynchronously, after the completion of
 * {@link org.apache.syncope.core.provisioning.api.pushpull.PullActions}.
 */
public class SetUMembershipsJob extends AbstractInterruptableJob {

    private static final Logger LOG = LoggerFactory.getLogger(SetUMembershipsJob.class);

    public static final String MEMBERSHIPS_BEFORE_KEY = "membershipsBefore";

    public static final String MEMBERSHIPS_AFTER_KEY = "membershipsAfter";

    @Autowired
    private UserProvisioningManager userProvisioningManager;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            AuthContextUtils.execWithAuthContext(context.getMergedJobDataMap().getString(JobManager.DOMAIN_KEY), () -> {

                @SuppressWarnings("unchecked")
                Map<String, Set<String>> membershipsBefore =
                        (Map<String, Set<String>>) context.getMergedJobDataMap().get(MEMBERSHIPS_BEFORE_KEY);
                LOG.debug("Memberships before pull (User -> Groups) {}", membershipsBefore);

                @SuppressWarnings("unchecked")
                Map<String, Set<String>> membershipsAfter =
                        (Map<String, Set<String>>) context.getMergedJobDataMap().get(MEMBERSHIPS_AFTER_KEY);
                LOG.debug("Memberships after pull (User -> Groups) {}", membershipsAfter);

                List<UserPatch> patches = new ArrayList<>();

                membershipsAfter.forEach((user, groups) -> {
                    UserPatch userPatch = new UserPatch();
                    userPatch.setKey(user);
                    patches.add(userPatch);

                    groups.forEach(group -> {
                        Set<String> before = membershipsBefore.get(user);
                        if (before == null || !before.contains(group)) {
                            userPatch.getMemberships().add(
                                    new MembershipPatch.Builder().
                                            operation(PatchOperation.ADD_REPLACE).
                                            group(group).
                                            build());
                        }
                    });
                });

                membershipsBefore.forEach((user, groups) -> {
                    UserPatch userPatch = patches.stream().
                            filter(patch -> user.equals(patch.getKey())).findFirst().
                            orElseGet(() -> {
                                UserPatch patch = new UserPatch();
                                patch.setKey(user);
                                patches.add(patch);
                                return patch;
                            });

                    groups.forEach(group -> {
                        Set<String> after = membershipsAfter.get(user);
                        if (after == null || !after.contains(group)) {
                            userPatch.getMemberships().add(
                                    new MembershipPatch.Builder().
                                            operation(PatchOperation.DELETE).
                                            group(group).
                                            build());
                        }
                    });
                });

                patches.stream().filter(patch -> !patch.isEmpty()).forEach(patch -> {
                    LOG.debug("About to update User {}", patch);
                    userProvisioningManager.update(patch, true);
                });

                return null;
            });
        } catch (RuntimeException e) {
            LOG.error("While setting memberships", e);
            throw new JobExecutionException("While executing memberships", e);
        } finally {
            ApplicationContextProvider.getBeanFactory().destroySingleton(context.getJobDetail().getKey().getName());
        }
    }
}
