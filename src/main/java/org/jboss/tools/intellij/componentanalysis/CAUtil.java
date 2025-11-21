/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.componentanalysis;


import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CAUtil {

    public static String PACKAGE_JSON = "package.json";
    public static String TRUSTIFY_DA_IGNORE = "trustify-da-ignore";
    public static String DEPENDENCIES = "dependencies";

    public static Map<Dependency, List<PsiElement>> getDependencyListMap(PsiFile file) {
        if (PACKAGE_JSON.equals(file.getName())) {
            Set<String> ignored = getIgnoredDependencies(file);
            return getDependencyListMap(file, ignored);
        }
        return Collections.emptyMap();
    }

    private static Map<Dependency, List<PsiElement>> getDependencyListMap(PsiFile file, Set<String> ignored) {
        Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
        Arrays.stream(file.getChildren())
                .filter(e -> e instanceof JsonObject)
                .flatMap(e -> Arrays.stream(e.getChildren()))
                .filter(e -> e instanceof JsonProperty && DEPENDENCIES.equals(((JsonProperty) e).getName()))
                .flatMap(e -> Arrays.stream(e.getChildren()))
                .filter(e -> e instanceof JsonObject)
                .flatMap(e -> Arrays.stream(e.getChildren()))
                .filter(c -> c instanceof JsonProperty && !ignored.contains(((JsonProperty) c).getName()))
                .forEach(c -> {
                    String name = ((JsonProperty) c).getName();
                    int index = name.lastIndexOf("/");
                    String namespace = null;
                    if (index > 0) {
                        namespace = name.substring(0, index);
                        name = name.substring(index + 1);
                    } else if (index == 0) {
                        name = name.substring(index + 1);
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

    private static Set<String> getIgnoredDependencies(PsiFile file) {
        Set<String> ignored = Arrays.stream(file.getChildren())
                .filter(e -> e instanceof JsonObject)
                .flatMap(e -> Arrays.stream(e.getChildren()))
                .filter(e -> e instanceof JsonProperty && TRUSTIFY_DA_IGNORE.equals(((JsonProperty) e).getName()))
                .flatMap(e -> Arrays.stream(e.getChildren()))
                .filter(e -> e instanceof JsonArray)
                .flatMap(e -> Arrays.stream(e.getChildren()))
                .filter(c -> c instanceof JsonStringLiteral)
                .map(c -> ((JsonStringLiteral) c).getValue())
                .collect(Collectors.toSet());
        return ignored;
    }
}
