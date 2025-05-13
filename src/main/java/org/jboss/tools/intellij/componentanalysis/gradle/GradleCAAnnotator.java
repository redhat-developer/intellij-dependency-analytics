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

package org.jboss.tools.intellij.componentanalysis.gradle;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlComment;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.redhat.exhort.api.v4.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.*;
import org.jboss.tools.intellij.componentanalysis.gradle.build.psi.Artifact;
import org.jboss.tools.intellij.componentanalysis.maven.MavenCAUpdateManifestIntentionAction;

import java.util.*;
import java.util.stream.Collectors;

public class GradleCAAnnotator extends CAAnnotator {

    @Override
    protected String getInspectionShortName() {
        return GradleCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if ("build.gradle".equals(file.getName())) {
            Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
            List<Artifact> elements;
            Arrays.stream(file.getChildren())
                    .filter(e -> e instanceof Artifact)
                    .filter(artifact -> ((Artifact)artifact).getComment() == null || Objects.nonNull(((Artifact)artifact).getComment()) && !((Artifact)artifact).getComment().getText().contains("exhortignore"))
                    .map(dep -> (Artifact)dep)
                    .forEach(  dep -> {
                            Dependency dependency = new Dependency("maven", dep.getGroup().getText().replace("\"","") , dep.getArtifactId().getText(),dep.getVersion().getText());
                            resultMap.computeIfAbsent(dependency, k -> new LinkedList<>()).add(dep);
                        }
                    );

             return resultMap;
        }

        return Collections.emptyMap();
    }

    @Override
    protected CAIntentionAction createQuickFix(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        return new GradleCAIntentionAction(element, source, report);
    }

    @Override
    protected CAUpdateManifestIntentionAction patchManifest(PsiElement element, DependencyReport dependency) {
        return new GradleCAUpdateManifestIntentionAction(element, dependency);
    }

    @Override
    protected boolean isQuickFixApplicable(PsiElement element) {
        return true;
    }
}
