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

import com.intellij.json.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.Dependency;

import java.util.*;
import java.util.stream.Collectors;

public class NpmCAAnnotator extends CAAnnotator {

    @Override
    protected String getInspectionShortName() {
        return NpmCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if ("package.json".equals(file.getName())) {
            Set<String> ignored = Arrays.stream(file.getChildren())
                    .filter(e -> e instanceof JsonObject)
                    .flatMap(e -> Arrays.stream(e.getChildren()))
                    .filter(e -> e instanceof JsonProperty && "exhortignore".equals(((JsonProperty) e).getName()))
                    .flatMap(e -> Arrays.stream(e.getChildren()))
                    .filter(e -> e instanceof JsonArray)
                    .flatMap(e -> Arrays.stream(e.getChildren()))
                    .filter(c -> c instanceof JsonStringLiteral)
                    .map(c -> ((JsonStringLiteral) c).getValue())
                    .collect(Collectors.toSet());

            Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
            Arrays.stream(file.getChildren())
                    .filter(e -> e instanceof JsonObject)
                    .flatMap(e -> Arrays.stream(e.getChildren()))
                    .filter(e -> e instanceof JsonProperty && "dependencies".equals(((JsonProperty) e).getName()))
                    .flatMap(e -> Arrays.stream(e.getChildren()))
                    .filter(e -> e instanceof JsonObject)
                    .flatMap(e -> Arrays.stream(e.getChildren()))
                    .filter(c -> c instanceof JsonProperty && !ignored.contains(((JsonProperty) c).getName()))
                    .forEach(c -> {
                        String name = ((JsonProperty) c).getName();
                        String[] parts = name.split("/", 2);
                        String namespace = null;
                        if (parts.length == 2) {
                            namespace = parts[0];
                            name = parts[1];
                        }
                        JsonValue value = ((JsonProperty) c).getValue();
                        String version = value instanceof JsonStringLiteral
                                ? ((JsonStringLiteral) value).getValue()
                                : null;
                        Dependency dp = new Dependency("npm", namespace, name, version);
                        resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(c);
                    });
            return resultMap;
        }
        return Collections.emptyMap();
    }
}
