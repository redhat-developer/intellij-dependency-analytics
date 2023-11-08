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

package org.jboss.tools.intellij.componentanalysis.pypi.ce;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.pypi.PipCAInspection;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.NameReq;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.NameReqComment;

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
            Arrays.stream(file.getChildren())
                    .filter(e -> e instanceof NameReq)
                    .map(e -> (NameReq) e)
                    .filter(r -> Arrays.stream(r.getChildren())
                            .filter(c -> c instanceof NameReqComment)
                            .map(c -> (NameReqComment) c)
                            .noneMatch(c -> {
                                String comment = c.getText().trim();
                                if (!comment.isEmpty() && '#' == comment.charAt(0)) {
                                    return "exhortignore".equals(comment.substring(1).trim());
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
}
