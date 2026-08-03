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
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import io.github.guacsec.trustifyda.api.v5.Issue;
import io.github.guacsec.trustifyda.api.v5.RecommendationReport;
import org.jboss.tools.intellij.exhort.TelemetryService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class CAIntentionAction implements IntentionAction {

    protected @SafeFieldForPreview PsiElement element;
    protected @SafeFieldForPreview VulnerabilitySource source;
    protected @SafeFieldForPreview DependencyReport report;
    protected @SafeFieldForPreview String recommendationSourceName;
    protected @SafeFieldForPreview RecommendationReport recommendationReport;
    protected @SafeFieldForPreview String advisoryLabel;
    protected @SafeFieldForPreview String advisoryFixedIn;

    protected CAIntentionAction(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        this.element = element;
        this.source = source;
        this.report = report;
    }

    /** Sets provider-level recommendation data for source-attributed quick-fixes. */
    void setRecommendationData(String sourceName, RecommendationReport recReport) {
        this.recommendationSourceName = sourceName;
        this.recommendationReport = recReport;
    }

    /** Sets advisory-based fix data for vendor-specific quick-fixes. */
    void setAdvisoryData(String label, String fixedInVersion) {
        this.advisoryLabel = label;
        this.advisoryFixedIn = fixedInVersion;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        if (advisoryLabel != null && advisoryFixedIn != null) {
            return "Switch to version " + advisoryFixedIn + " (" + advisoryLabel + ")";
        }
        if (recommendationSourceName != null) {
            return getQuickFixTextForRecommendation(recommendationSourceName);
        }
        return getQuickFixText(this.source, this.report);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "RHDA";
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        String recommendedVersion;
        if (advisoryFixedIn != null) {
            recommendedVersion = advisoryFixedIn;
        } else if (recommendationReport != null && recommendationReport.getRecommendation() != null) {
            recommendedVersion = recommendationReport.getRecommendation().version();
        } else {
            recommendedVersion = getRecommendedVersion(this.report);
        }
        this.updateVersion(project, editor, file, recommendedVersion);
        TelemetryService.sendPackageUpdateEvent(file, recommendedVersion, this.report.getRef().name(), "recommendation-accepted");
    }

    private String getRecommendationsRepo(DependencyReport dependency) {
        // Check provider-level recommendation first
        if (recommendationReport != null && recommendationReport.getRecommendation() != null
                && recommendationReport.getRecommendation().purl() != null
                && recommendationReport.getRecommendation().purl().getQualifiers() != null) {
            return recommendationReport.getRecommendation().purl().getQualifiers().get("repository_url");
        }

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
        FileModifier modifier = this.createCAIntentionActionInCopy(PsiTreeUtil.findSameElementInCopy(this.element, target));
        if (modifier instanceof CAIntentionAction copy) {
            if (this.recommendationSourceName != null) {
                copy.setRecommendationData(this.recommendationSourceName, this.recommendationReport);
            }
            if (this.advisoryLabel != null) {
                copy.setAdvisoryData(this.advisoryLabel, this.advisoryFixedIn);
            }
        }
        return modifier;
    }

    protected abstract void updateVersion(@NotNull Project project, Editor editor, PsiFile file, String version);
    protected abstract @Nullable FileModifier createCAIntentionActionInCopy(PsiElement element);


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
        Optional<Issue> issue = dependency.getIssues().stream().filter(iss -> iss.getRemediation() != null && iss.getRemediation().getTrustedContent() != null).findFirst();
        if(issue.isPresent()) {
            return issue.get().getRemediation().getTrustedContent() != null;
        }
        else
        {
            return false;
        }
    }

    static boolean thereIsRecommendation(DependencyReport dependency) {
        return dependency.getRecommendation() != null && !dependency.getRecommendation().version().trim().isEmpty();
    }

    /** Checks if a provider-level recommendation report has a valid recommendation. */
    static boolean thereIsRecommendation(RecommendationReport recReport) {
        return recReport != null && recReport.getRecommendation() != null
                && recReport.getRecommendation().version() != null
                && !recReport.getRecommendation().version().trim().isEmpty();
    }

    static boolean thereAreNoIssues(DependencyReport dependency) {
        return dependency.getIssues() == null || dependency.getIssues().size() == 0;
    }

    private static @NotNull String getQuickFixText(VulnerabilitySource source, DependencyReport dependency) {
        String text="";
        if(thereAreNoIssues(dependency) && thereIsRecommendation(dependency))
        {
            text = "Quick-Fix suggestion - apply Red Hat Recommended version";
        }
        else
        {
            if(thereIsTcRemediation(dependency))
            {
                text = "Quick-Fix suggestion - apply Red Hat remediation version";
            }
        }
        return text;
    }

    private static @NotNull String getQuickFixTextForRecommendation(String recommendationSourceName) {
        return "Quick-Fix suggestion (" + recommendationSourceName + ") - apply Red Hat Recommended version";
    }

    static boolean isQuickFixAvailable(DependencyReport dependency) {
        if (thereAreNoIssues(dependency)) {
            return thereIsRecommendation(dependency);
        }
        return thereIsTcRemediation(dependency) || hasAdvisoryFixes(dependency);
    }

    /** Checks if a provider-level recommendation report has an available quick-fix. */
    static boolean isQuickFixAvailable(RecommendationReport recReport) {
        return thereIsRecommendation(recReport);
    }

    /** Checks if a dependency has advisory-based fix versions. */
    static boolean hasAdvisoryFixes(DependencyReport dependency) {
        if (thereAreNoIssues(dependency)) return false;
        return dependency.getIssues().stream()
                .filter(issue -> issue.getRemediation() != null && issue.getRemediation().getAdvisories() != null)
                .flatMap(issue -> issue.getRemediation().getAdvisories().stream())
                .anyMatch(ar -> ar.getFixedIn() != null && !ar.getFixedIn().trim().isEmpty());
    }

    /** Checks whether the source identifier indicates a Red Hat or RHLW source. */
    static boolean isRedHatSource(String sourceId) {
        if (sourceId == null) return false;
        String s = sourceId.toLowerCase();
        return s.contains("redhat") || s.contains("rhlw");
    }

    /** Checks whether the source identifier indicates an RHLW (Red Hat Lightwell) source. */
    static boolean isRhlwSource(String sourceId) {
        if (sourceId == null) return false;
        return sourceId.toLowerCase().contains("rhlw");
    }
}
