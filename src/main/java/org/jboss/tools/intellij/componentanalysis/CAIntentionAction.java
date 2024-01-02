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

package org.jboss.tools.intellij.componentanalysis;

import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.redhat.exhort.api.DependencyReport;
import com.redhat.exhort.api.Issue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class CAIntentionAction implements IntentionAction {

    protected @SafeFieldForPreview PsiElement element;
    protected @SafeFieldForPreview VulnerabilitySource source;
    protected @SafeFieldForPreview DependencyReport report;

    protected CAIntentionAction(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        this.element = element;
        this.source = source;
        this.report = report;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return getQuickFixText(this.source, this.report);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "RHDA";
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        this.updateVersion(project, editor, file, getRecommendedVersion(this.report));
    }

    private String getRecommendationsRepo(DependencyReport dependency) {
        String repo=null;
        if(thereAreNoIssues(dependency))
        {
            if(thereIsRecommendation(dependency))
                repo = dependency.getRecommendation().purl().getQualifiers().get("repository_url");
        }
        else
        {
            Optional<Issue> issue = dependency.getIssues().stream().findFirst();
            if(issue.isPresent())
            {
                if(thereIsTcRemediation(dependency)) {
                    repo =  issue.get().getRemediation().getTrustedContent().getRef().version();
                }
            }

        }
        return repo;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Override
    public @Nullable FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
        return this.createCAIntentionActionInCopy(PsiTreeUtil.findSameElementInCopy(this.element, target));
    }

    protected abstract void updateVersion(@NotNull Project project, Editor editor, PsiFile file, String version);

    protected abstract @Nullable FileModifier createCAIntentionActionInCopy(PsiElement element);

    //TODO
    private static @NotNull String getRecommendedVersion(DependencyReport dependency) {
        String version=null;
        if(thereAreNoIssues(dependency))
        {
            if(thereIsRecommendation(dependency))
            version = dependency.getRecommendation().version();
        }
        else
        {
            Optional<Issue> issue = dependency.getIssues().stream().findFirst();
            if(issue.isPresent())
            {
                if(thereIsTcRemediation(dependency)) {
                   version =  issue.get().getRemediation().getTrustedContent().getRef().version();
                }
            }

        }
       return version;
    }

    private static boolean thereIsTcRemediation(DependencyReport dependency) {
        Optional<Issue> issue = dependency.getIssues().stream().filter(iss -> iss.getRemediation().getTrustedContent() != null).findFirst();
        if(issue.isPresent()) {
            return issue.get().getRemediation().getTrustedContent() != null;
        }
        else
        {
            return false;
        }
    }

    static boolean thereIsRecommendation(DependencyReport dependency) {
        return dependency.getRecommendation() != null && !dependency.getRecommendation().version().trim().equals("");
    }

    static boolean thereAreNoIssues(DependencyReport dependency) {
        return dependency.getIssues() == null || dependency.getIssues().size() == 0;
    }

    //TODO
    private static @NotNull String getQuickFixText(VulnerabilitySource source, DependencyReport dependency) {
        String text="";
        if(thereAreNoIssues(dependency) && thereIsRecommendation(dependency))
        {
            text = "Quick-Fix suggestion - apply redhat Recommended version";
        }
        else
        {
            if(thereIsTcRemediation(dependency))
            {
                text = "Quick-Fix suggestion - apply redhat remediation version";
            }
        }
        return text;
    }

    //TODO
    static boolean isQuickFixAvailable(DependencyReport dependency) {
        boolean result=false;
        if(thereAreNoIssues(dependency))
        {
            if(thereIsRecommendation(dependency))
            {
                result = true;
            }
        }
        else
        {
            if(thereIsTcRemediation(dependency))
            {
                result = true;
            }
        }
     return result;
    }
}
