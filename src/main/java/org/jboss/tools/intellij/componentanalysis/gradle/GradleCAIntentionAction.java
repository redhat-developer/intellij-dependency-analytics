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

import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jboss.tools.intellij.componentanalysis.gradle.build.filetype.BuildGradleFileType;
import org.jboss.tools.intellij.componentanalysis.gradle.build.psi.Artifact;
import org.jboss.tools.intellij.componentanalysis.gradle.build.psi.BuildGradleFile;
import org.jboss.tools.intellij.componentanalysis.gradle.build.psi.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class GradleCAIntentionAction extends CAIntentionAction {
    GradleCAIntentionAction(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        super(element, source, report);
    }

    @Override
    protected void updateVersion(@NotNull Project project, Editor editor, PsiFile file, String version) {
        String groupIdTemp = ((Artifact) element).getGroup().getText();
        if(groupIdTemp.contains("'")) {
            groupIdTemp = groupIdTemp.replace("'", "\"");
        }
        if(!groupIdTemp.startsWith("\"")) {
            groupIdTemp = "\"" + groupIdTemp;
        }
        BuildGradleFile dummyFile = (BuildGradleFile) PsiFileFactory.getInstance(project).createFileFromText(
                "dummy-build.gradle", BuildGradleFileType.INSTANCE,
                String.format("dependencies { %s    %s %s:%s:%s %s}", System.lineSeparator(), ((Artifact) element).getConfigName().getText(), groupIdTemp, ((Artifact) element).getArtifactId().getText(), version + "\"", System.lineSeparator()));
        Artifact modifiedArtifact = (Artifact) Arrays.stream(dummyFile.getChildren()).filter(psi -> psi instanceof Artifact).findAny().get();
        Arrays.stream(element.getChildren())
                .filter(child -> child instanceof Version)
                .map(artifactVersion -> (Version)artifactVersion)
                .findAny()
                .ifPresent(artifactVersion -> artifactVersion.replace(Arrays.stream(modifiedArtifact.getChildren()).filter(psi -> psi instanceof Version).findAny().get()));
    }

    @Override
    protected @Nullable FileModifier createCAIntentionActionInCopy(PsiElement element) {
        return new GradleCAIntentionAction(element, this.source, this.report);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && "build.gradle".equals(file.getName());
    }
}
