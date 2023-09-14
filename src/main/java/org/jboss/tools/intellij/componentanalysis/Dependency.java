/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.componentanalysis;

import com.github.packageurl.PackageURL;

import java.util.Objects;

public class Dependency {

    String type;
    String namespace;
    String name;
    String version;

    public Dependency(String type, String namespace, String name, String version) {
        this.type = type;
        this.namespace = namespace;
        this.name = name;
        this.version = version;
    }

    public Dependency(Dependency d, boolean version) {
        this.type = d.type;
        this.namespace = d.namespace;
        this.name = d.name;
        if (version) {
            this.version = d.version;
        }
    }

    public Dependency(PackageURL purl) {
        this(purl, true);
    }

    public Dependency(PackageURL purl, boolean version) {
        this.type = purl.getType();
        this.namespace = purl.getNamespace();
        this.name = purl.getName();
        if (version) {
            this.version = purl.getVersion();
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(type, that.type) && Objects.equals(namespace, that.namespace)
                && Objects.equals(name, that.name) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, namespace, name, version);
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "type='" + type + '\'' +
                ", namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
