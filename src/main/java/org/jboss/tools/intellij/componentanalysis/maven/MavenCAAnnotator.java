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
import com.intellij.psi.xml.XmlComment;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.redhat.exhort.api.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;

import java.util.*;
import java.util.stream.Collectors;

public class MavenCAAnnotator extends CAAnnotator {

    @Override
    protected String getInspectionShortName() {
        return MavenCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if ("pom.xml".equals(file.getName())) {
            Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();

            Arrays.stream(file.getChildren())
                    .filter(e -> e instanceof XmlDocument)
                    .flatMap(e -> Arrays.stream(e.getChildren()))
                    .filter(e -> e instanceof XmlTag && "project".equals(((XmlTag) e).getName()))
                    .flatMap(e -> Arrays.stream(e.getChildren()))
                    .filter(e -> e instanceof XmlTag && "dependencies".equals(((XmlTag) e).getName()))
                    .flatMap(e -> Arrays.stream(e.getChildren()))
                    .filter(e -> e instanceof XmlTag && "dependency".equals(((XmlTag) e).getName()))
                    .filter(e -> Arrays.stream(e.getChildren())
                            .noneMatch(c -> c instanceof XmlComment
                                    && "exhortignore".equals(((XmlComment) c).getCommentText().trim())))
                    .map(e -> (XmlTag) e)
                    .forEach(d -> {
                        List<XmlTag> elements = Arrays.stream(d.getChildren())
                                .filter(c -> c instanceof XmlTag)
                                .map(c -> (XmlTag) c)
                                .collect(Collectors.toList());

                        String groupId = null;
                        String artifactId = null;
                        String version = null;

                        for (XmlTag e : elements) {
                            if ("groupId".equals(e.getName())) {
                                Optional<String> result = Arrays.stream(e.getValue().getChildren())
                                        .filter(c -> c instanceof XmlText)
                                        .map(PsiElement::getText)
                                        .findAny();
                                if (result.isPresent()) {
                                    groupId = result.get();
                                } else {
                                    return;
                                }
                            } else if ("artifactId".equals(e.getName())) {
                                Optional<String> result = Arrays.stream(e.getValue().getChildren())
                                        .filter(c -> c instanceof XmlText)
                                        .map(PsiElement::getText)
                                        .findAny();
                                if (result.isPresent()) {
                                    artifactId = result.get();
                                } else {
                                    return;
                                }
                            } else if ("version".equals(e.getName())) {
                                Optional<String> result = Arrays.stream(e.getValue().getChildren())
                                        .filter(c -> c instanceof XmlText)
                                        .map(PsiElement::getText)
                                        .findAny();
                                if (result.isPresent()) {
                                    version = result.get();
                                }
                            } else if ("scope".equals(e.getName())) {
                                Optional<String> result = Arrays.stream(e.getValue().getChildren())
                                        .filter(c -> c instanceof XmlText)
                                        .map(PsiElement::getText)
                                        .findAny();
                                if (result.isPresent() && "test".equals(result.get())) {
                                    return;
                                }
                            }
                        }

                        if (groupId != null && artifactId != null) {
                            Dependency dp = new Dependency("maven", groupId, artifactId, version);
                            resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(d);
                        }
                    });

            return resultMap;
        }

        return Collections.emptyMap();
    }

    @Override
    protected CAIntentionAction createQuickFix(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        return new MavenCAIntentionAction(element, source, report);
    }

    @Override
    protected boolean isQuickFixApplicable(PsiElement element) {
        if (element instanceof XmlTag) {
            return Arrays.stream(element.getChildren())
                    .filter(c -> c instanceof XmlTag)
                    .map(c -> (XmlTag) c)
                    .filter(c -> "version".equals(c.getName()))
                    .flatMap(c -> Arrays.stream(c.getValue().getChildren()))
                    .anyMatch(c -> c instanceof XmlText);
        }
        return false;
    }
}
