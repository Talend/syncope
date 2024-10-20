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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.PathParam;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement(name = "attribute")
@XmlType
public class AttrTO implements Comparable<AttrTO>, Serializable {

    private static final long serialVersionUID = 4941691338796323623L;

    public static class Builder {

        private final AttrTO instance = new AttrTO();

        public Builder schema(final String schema) {
            instance.setSchema(schema);
            return this;
        }

        public Builder value(final String value) {
            instance.getValues().add(value);
            return this;
        }

        public Builder values(final String... values) {
            instance.getValues().addAll(Arrays.asList(values));
            return this;
        }

        public Builder values(final Collection<String> values) {
            instance.getValues().addAll(values);
            return this;
        }

        public AttrTO build() {
            return instance;
        }
    }

    /**
     * Name of the schema that this attribute is referring to.
     */
    private String schema;

    /**
     * Set of (string) values of this attribute.
     */
    private final List<String> values = new ArrayList<>();

    /**
     * @return the name of the schema that this attribute is referring to
     */
    @JsonProperty(required = true)
    @XmlElement(required = true)
    public String getSchema() {
        return schema;
    }

    /**
     * @param schema name to be set
     */
    @PathParam("schema")
    public void setSchema(final String schema) {
        this.schema = schema;

    }

    /**
     * @return attribute values as strings
     */
    @XmlElementWrapper(name = "values", required = true)
    @XmlElement(name = "value", required = true)
    @JsonProperty(value = "values", required = true)
    public List<String> getValues() {
        return values;
    }

    @Override
    public int compareTo(final AttrTO other) {
        return equals(other)
                ? 0
                : new CompareToBuilder().
                        append(schema, other.schema).
                        append(values.toArray(), other.values.toArray()).
                        toComparison();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().
                append(schema).
                append(values).
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
        final AttrTO other = (AttrTO) obj;
        return new EqualsBuilder().
                append(schema, other.schema).
                append(values, other.values).
                build();
    }
}
