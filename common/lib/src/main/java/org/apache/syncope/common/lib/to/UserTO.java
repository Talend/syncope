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
package org.apache.syncope.common.lib.to;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.syncope.common.lib.types.AnyTypeKind;

@XmlRootElement(name = "user")
@XmlType
@Schema(allOf = { AnyTO.class })
public class UserTO extends AnyTO implements GroupableRelatableTO {

    private static final long serialVersionUID = 7791304495192615740L;

    private String password;

    private final List<String> roles = new ArrayList<>();

    private final List<String> dynRoles = new ArrayList<>();

    private final Set<String> privileges = new TreeSet<>();

    private String token;

    private Date tokenExpireTime;

    private String username;

    private Date lastLoginDate;

    private Date changePwdDate;

    private Integer failedLogins;

    private String securityQuestion;

    private String securityAnswer;

    private boolean suspended;

    private boolean mustChangePassword;

    private final List<RelationshipTO> relationships = new ArrayList<>();

    private final List<MembershipTO> memberships = new ArrayList<>();

    private final List<MembershipTO> dynMemberships = new ArrayList<>();

    private final List<LinkedAccountTO> linkedAccounts = new ArrayList<>();

    private final List<String> delegatingDelegations = new ArrayList<>();

    private final List<String> delegatedDelegations = new ArrayList<>();

    @JsonProperty("@class")
    @Schema(name = "@class", required = true, example = "org.apache.syncope.common.lib.to.UserTO")
    @Override
    public String getDiscriminator() {
        return getClass().getName();
    }

    @Override
    public String getType() {
        return AnyTypeKind.USER.name();
    }

    @Override
    public void setType(final String type) {
        // fixed
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    @JsonProperty("roles")
    public List<String> getRoles() {
        return roles;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @XmlElementWrapper(name = "dynRoles")
    @XmlElement(name = "role")
    @JsonProperty("dynRoles")
    public List<String> getDynRoles() {
        return dynRoles;
    }

    @XmlElementWrapper(name = "privileges")
    @XmlElement(name = "privilege")
    @JsonProperty("privileges")
    public Set<String> getPrivileges() {
        return privileges;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    public Date getTokenExpireTime() {
        if (tokenExpireTime != null) {
            return new Date(tokenExpireTime.getTime());
        }
        return null;
    }

    public void setTokenExpireTime(final Date tokenExpireTime) {
        if (tokenExpireTime != null) {
            this.tokenExpireTime = new Date(tokenExpireTime.getTime());
        } else {
            this.tokenExpireTime = null;
        }
    }

    @JsonProperty(required = true)
    @XmlElement(required = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    public Date getChangePwdDate() {
        if (changePwdDate != null) {
            return new Date(changePwdDate.getTime());
        }
        return null;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    public Integer getFailedLogins() {
        return failedLogins;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    public Date getLastLoginDate() {
        if (lastLoginDate != null) {
            return new Date(lastLoginDate.getTime());
        }
        return null;
    }

    public void setChangePwdDate(final Date changePwdDate) {
        if (changePwdDate != null) {
            this.changePwdDate = new Date(changePwdDate.getTime());
        } else {
            this.changePwdDate = null;
        }
    }

    public void setFailedLogins(final Integer failedLogins) {
        this.failedLogins = failedLogins;
    }

    public void setLastLoginDate(final Date lastLoginDate) {
        if (lastLoginDate != null) {
            this.lastLoginDate = new Date(lastLoginDate.getTime());
        } else {
            this.lastLoginDate = null;
        }
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(final String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(final String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(final boolean suspended) {
        this.suspended = suspended;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(final boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    @JsonIgnore
    @Override
    public Optional<RelationshipTO> getRelationship(final String type, final String otherKey) {
        return relationships.stream().filter(
                relationship -> type.equals(relationship.getType()) && otherKey.equals(relationship.getOtherEndKey())).
                findFirst();
    }

    @XmlElementWrapper(name = "relationships")
    @XmlElement(name = "relationship")
    @JsonProperty("relationships")
    @Override
    public List<RelationshipTO> getRelationships() {
        return relationships;
    }

    @JsonIgnore
    @Override
    public Optional<MembershipTO> getMembership(final String groupKey) {
        return memberships.stream().filter(membership -> groupKey.equals(membership.getGroupKey())).findFirst();
    }

    @XmlElementWrapper(name = "memberships")
    @XmlElement(name = "membership")
    @JsonProperty("memberships")
    @Override
    public List<MembershipTO> getMemberships() {
        return memberships;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @XmlElementWrapper(name = "dynMemberships")
    @XmlElement(name = "dynMembership")
    @JsonProperty("dynMemberships")
    @Override
    public List<MembershipTO> getDynMemberships() {
        return dynMemberships;
    }

    @XmlElementWrapper(name = "linkedAccounts")
    @XmlElement(name = "linkedAccount")
    @JsonProperty("linkedAccounts")
    public List<LinkedAccountTO> getLinkedAccounts() {
        return linkedAccounts;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @XmlElementWrapper(name = "delegatingDelegations")
    @XmlElement(name = "delegatingDelegation")
    @JsonProperty("delegatingDelegations")
    public List<String> getDelegatingDelegations() {
        return delegatingDelegations;
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @XmlElementWrapper(name = "delegatedDelegations")
    @XmlElement(name = "delegatedDelegation")
    @JsonProperty("delegatedDelegations")
    public List<String> getDelegatedDelegations() {
        return delegatedDelegations;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().
                appendSuper(super.hashCode()).
                append(username).
                append(roles).
                append(dynRoles).
                append(privileges).
                append(token).
                append(tokenExpireTime).
                append(lastLoginDate).
                append(changePwdDate).
                append(failedLogins).
                append(securityQuestion).
                append(securityAnswer).
                append(suspended).
                append(mustChangePassword).
                append(relationships).
                append(memberships).
                append(dynMemberships).
                append(linkedAccounts).
                append(delegatingDelegations).
                append(delegatedDelegations).
                build();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserTO other = (UserTO) obj;
        return new EqualsBuilder().
                appendSuper(super.equals(obj)).
                append(username, other.username).
                append(roles, other.roles).
                append(dynRoles, other.dynRoles).
                append(privileges, other.privileges).
                append(token, other.token).
                append(tokenExpireTime, other.tokenExpireTime).
                append(lastLoginDate, other.lastLoginDate).
                append(changePwdDate, other.changePwdDate).
                append(failedLogins, other.failedLogins).
                append(securityQuestion, other.securityQuestion).
                append(securityAnswer, other.securityAnswer).
                append(suspended, other.suspended).
                append(mustChangePassword, other.mustChangePassword).
                append(relationships, other.relationships).
                append(memberships, other.memberships).
                append(dynMemberships, other.dynMemberships).
                append(linkedAccounts, other.linkedAccounts).
                append(delegatingDelegations, other.delegatingDelegations).
                append(delegatedDelegations, other.delegatedDelegations).
                build();
    }
}
