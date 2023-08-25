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

package org.jboss.tools.intellij.componentanalysis.maven;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;

import java.util.*;

public class MavenCAAnnotator extends CAAnnotator {

    @Override
    protected String getInspectionShortName() {
        return MavenCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        MavenDomProjectModel projectModel = MavenDomUtil.getMavenDomModel(file, MavenDomProjectModel.class);
        if (projectModel != null) {
            List<MavenDomDependency> dependencies = projectModel.getDependencies().getDependencies();
            Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
            dependencies.forEach(d -> {
                Dependency dp = new Dependency(
                        "maven",
                        d.getGroupId().getStringValue(),
                        d.getArtifactId().getStringValue(),
                        d.getVersion().getStringValue());
                resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(d.getXmlElement());
            });
            return resultMap;
        }
        return Collections.emptyMap();
    }
}
