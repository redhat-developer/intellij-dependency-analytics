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

package org.jboss.tools.intellij.componentanalysis.pypi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.redhat.exhort.api.v4.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.CAUpdateManifestIntentionAction;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jboss.tools.intellij.componentanalysis.pypi.requirements.psi.NameReq;
import org.jboss.tools.intellij.componentanalysis.pypi.requirements.psi.NameReqComment;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jboss.tools.intellij.componentanalysis.CAUtil.EXHORT_IGNORE;

public class PipCAAnnotator extends CAAnnotator {
    @Override
    protected String getInspectionShortName() {
        return PipCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if ("requirements.txt".equals(file.getName())) {
            Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
            Arrays.stream(file.getChildren())
                    .filter(e -> e instanceof NameReq)
                    .map(e -> (NameReq) e)
                    .filter(r -> Arrays.stream(r.getChildren())
                            .filter(c -> c instanceof NameReqComment)
                            .map(c -> (NameReqComment) c)
                            .noneMatch(c -> {
                                String comment = c.getText().trim();
                                if (!comment.isEmpty() && '#' == comment.charAt(0)) {
                                    return EXHORT_IGNORE.equals(comment.substring(1).trim());
                                }
                                return false;
                            }))
                    .forEach(r -> {
                        String name = r.getPkgName().getText().toLowerCase();
                        String version = r.getVersionspec() != null ? r.getVersionspec().getText() : null;
                        Dependency dp = new Dependency("pypi", null, name, version);
                        resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(r);
                    });
            return resultMap;
        }
        return Collections.emptyMap();
    }

    @Override
    protected CAIntentionAction createQuickFix(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        return new PipCAIntentionAction(element, source, report);
    }

    @Override
    protected CAUpdateManifestIntentionAction patchManifest(PsiElement element, DependencyReport report) {
        return null;
    }

    @Override
    protected boolean isQuickFixApplicable(PsiElement element) {
        return element instanceof NameReq && ((NameReq) element).getVersionspec() != null;
    }
}
