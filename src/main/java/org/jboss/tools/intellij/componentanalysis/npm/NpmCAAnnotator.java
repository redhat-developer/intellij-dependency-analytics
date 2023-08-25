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

package org.jboss.tools.intellij.componentanalysis.npm;

import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.Dependency;

import java.util.*;

public class NpmCAAnnotator extends CAAnnotator {

    @Override
    protected String getInspectionShortName() {
        return NpmCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if ("package.json".equals(file.getName())) {
            Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
            PsiTreeUtil.findChildrenOfType(file, JsonProperty.class)
                    .stream()
                    .filter(c -> {
                        PsiElement p = c.getParent();
                        if (p != null) {
                            PsiElement gp = p.getParent();
                            return gp instanceof JsonProperty && "dependencies".equals(((JsonProperty) gp).getName());
                        }
                        return false;
                    })
                    .forEach(c -> {
                        String name = c.getName();
                        String[] parts = name.split("/", 2);
                        String namespace = null;
                        if (parts.length == 2) {
                            namespace = parts[0];
                            name = parts[1];
                        }
                        String version = c.getValue() != null ? c.getValue().getText() : null;
                        Dependency dp = new Dependency("npm", namespace, name, version);
                        resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(c);
                    });
            return resultMap;
        }
        return Collections.emptyMap();
    }
}
