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

package org.jboss.tools.intellij.componentanalysis.pypi.pro;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.redhat.exhort.api.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jboss.tools.intellij.componentanalysis.pypi.PipCAInspection;
import org.jetbrains.annotations.Nullable;
import ru.meanmail.psi.NameReq;
import ru.meanmail.psi.UrlReq;

import java.util.*;

public class PipCAAnnotator extends CAAnnotator {
    @Override
    protected String getInspectionShortName() {
        return PipCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if ("requirements.txt".equals(file.getName())) {
            Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();

            PsiElement[] children = file.getChildren();
            int i = 0;

            while (i < children.length) {
                PsiElement child = children[i];
                Dependency dep = getDependency(child);

                if (dep != null) {
                    while (++i <= children.length) {
                        if (i == children.length) {
                            resultMap.computeIfAbsent(dep, k -> new LinkedList<>()).add(child);
                            break;
                        }

                        PsiElement next = children[i];
                        if (next instanceof PsiComment) {
                            PsiComment c = (PsiComment) next;
                            String comment = c.getText().trim();
                            if (!comment.isEmpty() &&
                                    '#' == comment.charAt(0) &&
                                    "exhortignore".equals(comment.substring(1).trim())) {
                                break;
                            }
                        } else if (next instanceof LeafPsiElement) {
                            LeafPsiElement ele = (LeafPsiElement) next;
                            if ("RequirementsTokenType.EOL".equals(ele.getElementType().toString())) {
                                resultMap.computeIfAbsent(dep, k -> new LinkedList<>()).add(child);
                                break;
                            }
                        }
                    }
                }

                i++;
            }

            return resultMap;
        }
        return Collections.emptyMap();
    }

    @Override
    protected CAIntentionAction createQuickFix(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        return new PipCAIntentionAction(element, source, report);
    }

    @Override
    protected boolean isQuickFixApplicable(PsiElement element) {
        return element instanceof NameReq && ((NameReq) element).getVersionspec() != null;
    }

    @Nullable
    private static Dependency getDependency(PsiElement child) {
        Dependency dep = null;

        if (child instanceof NameReq) {
            NameReq nameReq = (NameReq) child;
            String name = nameReq.getName().getText().toLowerCase();
            String version = nameReq.getVersionspec() != null ? nameReq.getVersionspec().getText() : null;
            dep = new Dependency("pypi", null, name, version);
        } else if (child instanceof UrlReq) {
            UrlReq urlReq = (UrlReq) child;
            String name = urlReq.getName().getText().toLowerCase();
            String version = urlReq.getUrlspec().getText();
            dep = new Dependency("pypi", null, name, version);
        }
        return dep;
    }
}
