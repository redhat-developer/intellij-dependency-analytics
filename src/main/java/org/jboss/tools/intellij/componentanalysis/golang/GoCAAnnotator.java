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

package org.jboss.tools.intellij.componentanalysis.golang;

import com.goide.vgo.mod.psi.VgoModuleSpec;
import com.goide.vgo.mod.psi.VgoReplaceDirective;
import com.goide.vgo.mod.psi.VgoRequireDirective;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.redhat.exhort.api.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;

import java.util.*;

public class GoCAAnnotator extends CAAnnotator {
    @Override
    protected String getInspectionShortName() {
        return GoCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if ("go.mod".equals(file.getName())) {
            Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();

            VgoRequireDirective[] requires = PsiTreeUtil.getChildrenOfType(file, VgoRequireDirective.class);
            if (requires != null) {
                Arrays.stream(requires)
                        .flatMap(r -> r.getModuleSpecList().stream())
                        .filter(m -> {
                            PsiComment[] comments = PsiTreeUtil.getChildrenOfType(m, PsiComment.class);
                            if (comments != null) {
                                return Arrays.stream(comments)
                                        .noneMatch(c -> c.getText().contains("exhortignore"));
                            }
                            return true;
                        })
                        .forEach(m -> resultMap.computeIfAbsent(createDependency(m), k -> new LinkedList<>()).add(m));
            }

            VgoReplaceDirective[] replaces = PsiTreeUtil.getChildrenOfType(file, VgoReplaceDirective.class);
            if (replaces != null) {
                Arrays.stream(replaces)
                        .flatMap(r -> r.getReplacementList().stream())
                        .filter(r -> {
                            PsiComment[] comments = PsiTreeUtil.getChildrenOfType(r, PsiComment.class);
                            if (comments != null) {
                                return Arrays.stream(comments)
                                        .noneMatch(c -> c.getText().contains("exhortignore"));
                            }
                            return true;
                        })
                        .map(r -> r.getTarget())
                        .filter(Objects::nonNull)
                        .filter(t -> t.getModuleVersion() != null)
                        .forEach(m -> resultMap.computeIfAbsent(createDependency(m), k -> new LinkedList<>()).add(m));
            }

            return resultMap;
        }

        return Collections.emptyMap();
    }

    @Override
    protected CAIntentionAction createQuickFix(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        return new GoCAIntentionAction(element, source, report);
    }

    @Override
    protected boolean isQuickFixApplicable(PsiElement element) {
        return element instanceof VgoModuleSpec && ((VgoModuleSpec) element).getModuleVersion() != null;
    }

    private static Dependency createDependency(final VgoModuleSpec m) {
        String name = m.getIdentifier().getText();
        int index = name.lastIndexOf("/");
        String namespace = null;
        if (index > 0) {
            namespace = name.substring(0, index);
            name = name.substring(index + 1);
        } else if (index == 0) {
            name = name.substring(index + 1);
        }
        PsiElement mv = m.getModuleVersion();
        String version = mv != null
                ? mv.getText()
                : null;
        return new Dependency("golang", namespace, name, version);
    }
}
