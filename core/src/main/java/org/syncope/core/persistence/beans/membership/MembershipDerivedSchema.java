/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.syncope.core.persistence.beans.membership;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import org.syncope.core.persistence.beans.AbstractDerivedAttribute;
import org.syncope.core.persistence.beans.AbstractDerivedSchema;
import org.syncope.core.persistence.beans.AbstractSchema;
import org.syncope.core.persistence.beans.SchemaMapping;

@Entity
public class MembershipDerivedSchema extends AbstractDerivedSchema {

    @ManyToMany
    private Set<MembershipSchema> schemas;
    @OneToMany(mappedBy = "derivedSchema")
    private List<MembershipDerivedAttribute> derivedAttributes;

    public MembershipDerivedSchema() {
        schemas = new HashSet<MembershipSchema>();
        derivedAttributes = new ArrayList<MembershipDerivedAttribute>();
    }

    @Override
    public <T extends AbstractSchema> boolean addSchema(T schema) {
        return schemas.add((MembershipSchema) schema);
    }

    @Override
    public <T extends AbstractSchema> boolean removeSchema(T schema) {
        return schemas.remove((MembershipSchema) schema);
    }

    @Override
    public Set<MembershipSchema> getSchemas() {
        return schemas;
    }

    @Override
    public void setSchemas(Set<? extends AbstractSchema> schemas) {
        this.schemas = (Set<MembershipSchema>) schemas;
    }

    @Override
    public <T extends AbstractDerivedAttribute> boolean addDerivedAttribute(
            T derivedAttribute) {

        return derivedAttributes.add((MembershipDerivedAttribute) derivedAttribute);
    }

    @Override
    public <T extends AbstractDerivedAttribute> boolean removeDerivedAttribute(
            T derivedAttribute) {

        return derivedAttributes.remove((MembershipDerivedAttribute) derivedAttribute);
    }

    @Override
    public List<? extends AbstractDerivedAttribute> getDerivedAttributes() {
        return derivedAttributes;
    }

    @Override
    public <T extends AbstractDerivedAttribute> void setDerivedAttributes(
            List<T> derivedAttributes) {

        this.derivedAttributes = (List<MembershipDerivedAttribute>) derivedAttributes;
    }

    /**
     * TODO: https://code.google.com/p/syncope/issues/detail?id=27
     * @param mapping
     * @return
     */
    @Override
    public boolean addMapping(SchemaMapping mapping) {
        return true;
    }

    /**
     * TODO: https://code.google.com/p/syncope/issues/detail?id=27
     * @param mapping
     * @return
     */
    @Override
    public boolean removeMapping(SchemaMapping mapping) {
        return true;
    }

    /**
     * TODO: https://code.google.com/p/syncope/issues/detail?id=27
     * @return
     */
    @Override
    public List<SchemaMapping> getMappings() {
        return Collections.EMPTY_LIST;
    }

    /**
     * TODO: https://code.google.com/p/syncope/issues/detail?id=27
     * @param mappings
     */
    @Override
    public void setMappings(List<SchemaMapping> mappings) {
    }
}
