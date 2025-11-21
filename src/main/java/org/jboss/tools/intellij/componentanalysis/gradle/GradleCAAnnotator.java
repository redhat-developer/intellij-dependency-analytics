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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.CAUpdateManifestIntentionAction;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jboss.tools.intellij.componentanalysis.gradle.build.psi.Artifact;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.jboss.tools.intellij.componentanalysis.CAUtil.EXHORT_IGNORE;

public class GradleCAAnnotator extends CAAnnotator {

    @Override
    protected String getInspectionShortName() {
        return GradleCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if ("build.gradle".equals(file.getName())) {
            Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
            Arrays.stream(file.getChildren())
                    .filter(e -> e instanceof Artifact)
                    .filter(artifact -> ((Artifact)artifact).getComment() == null || Objects.nonNull(((Artifact)artifact).getComment()) && !((Artifact)artifact).getComment().getText().contains(EXHORT_IGNORE))
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
