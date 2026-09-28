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

import io.github.guacsec.trustifyda.api.PackageRef;
import io.github.guacsec.trustifyda.api.v5.AdvisoryRemediation;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import io.github.guacsec.trustifyda.api.v5.Issue;
import io.github.guacsec.trustifyda.api.v5.RecommendationReport;
import io.github.guacsec.trustifyda.api.v5.Remediation;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for provider-level recommendation support in CAIntentionAction.
 * Placed in the base package to access package-private methods.
 */
public class CAIntentionActionRecommendationTest {

    private static final String NEW_VERSION = "4.0.0";

    private RecommendationReport createProviderLevelRecommendation() {
        RecommendationReport recReport = new RecommendationReport();
        recReport.setRef(new PackageRef("pkg:maven/com.example/test@1.0.0"));
        recReport.setRecommendation(new PackageRef("pkg:maven/com.example/test@" + NEW_VERSION));
        return recReport;
    }

    private DependencyReport createReportWithLegacyRecommendation() {
        DependencyReport report = new DependencyReport();
        report.setRef(new PackageRef("pkg:maven/com.example/test@1.0.0"));
        report.setRecommendation(new PackageRef("pkg:maven/com.example/test@" + NEW_VERSION));
        return report;
    }

    private DependencyReport createReportWithoutRecommendation() {
        DependencyReport report = new DependencyReport();
        report.setRef(new PackageRef("pkg:maven/com.example/test@1.0.0"));
        return report;
    }

    // ── thereIsRecommendation tests ────────────────────────────────────────────

    /** Verifies that thereIsRecommendation detects a valid provider-level recommendation. */
    @Test
    public void testThereIsRecommendationWithProviderLevel() {
        RecommendationReport recReport = createProviderLevelRecommendation();
        assertTrue("Should detect provider-level recommendation",
                CAIntentionAction.thereIsRecommendation(recReport));
    }

    /** Verifies that thereIsRecommendation returns false for null RecommendationReport. */
    @Test
    public void testThereIsRecommendationWithNull() {
        assertFalse("Should return false for null",
                CAIntentionAction.thereIsRecommendation((RecommendationReport) null));
    }

    /** Verifies that thereIsRecommendation returns false for empty version. */
    @Test
    public void testThereIsRecommendationWithEmptyVersion() {
        RecommendationReport recReport = new RecommendationReport();
        recReport.setRef(new PackageRef("pkg:maven/com.example/test@1.0.0"));
        recReport.setRecommendation(new PackageRef("pkg:maven/com.example/test@"));
        assertFalse("Should return false for empty version",
                CAIntentionAction.thereIsRecommendation(recReport));
    }

    /** Verifies that thereIsRecommendation still works with legacy DependencyReport path. */
    @Test
    public void testThereIsRecommendationWithLegacyReport() {
        DependencyReport report = createReportWithLegacyRecommendation();
        assertTrue("Should detect legacy recommendation",
                CAIntentionAction.thereIsRecommendation(report));
    }

    /** Verifies that thereIsRecommendation returns false for DependencyReport without recommendation. */
    @Test
    public void testThereIsRecommendationFalseWithLegacyReport() {
        DependencyReport report = createReportWithoutRecommendation();
        assertFalse("Should return false when no recommendation set",
                CAIntentionAction.thereIsRecommendation(report));
    }

    // ── isQuickFixAvailable tests ──────────────────────────────────────────────

    /** Verifies that isQuickFixAvailable returns true for a valid provider-level recommendation. */
    @Test
    public void testIsQuickFixAvailableForProviderLevel() {
        RecommendationReport recReport = createProviderLevelRecommendation();
        assertTrue("Should be available for provider-level recommendation",
                CAIntentionAction.isQuickFixAvailable(recReport));
    }

    /** Verifies that isQuickFixAvailable returns false for null RecommendationReport. */
    @Test
    public void testIsQuickFixAvailableForNull() {
        assertFalse("Should not be available for null",
                CAIntentionAction.isQuickFixAvailable((RecommendationReport) null));
    }

    /** Verifies that isQuickFixAvailable returns true for legacy recommendation (no issues). */
    @Test
    public void testIsQuickFixAvailableForLegacyRecommendation() {
        DependencyReport report = createReportWithLegacyRecommendation();
        assertTrue("Should be available for legacy recommendation",
                CAIntentionAction.isQuickFixAvailable(report));
    }

    // ── Version extraction tests ───────────────────────────────────────────────

    /** Verifies that provider-level recommendation version is correctly extractable. */
    @Test
    public void testRecommendationVersionExtraction() {
        RecommendationReport recReport = createProviderLevelRecommendation();
        assertNotNull("Recommendation should not be null", recReport.getRecommendation());
        assertEquals("Version should match", NEW_VERSION, recReport.getRecommendation().version());
    }

    // ── hasBaseVulnerabilityFix (TC-6474 regression) ───────────────────────────

    /**
     * A report whose issue has an advisory-based fix (fixedIn) but NO trusted
     * content remediation and no recommendation. The base quick-fix cannot render text or produce a
     * version for such reports, so it must not be admitted — otherwise the "More Actions" menu shows
     * an empty option that throws a null @NotNull exception on click.
     */
    @Test
    public void testAdvisoryOnlyReportHasNoBaseVulnerabilityFix() {
        AdvisoryRemediation advisory = new AdvisoryRemediation();
        advisory.setFixedIn("1.0.1");

        Remediation remediation = new Remediation();
        remediation.setAdvisories(List.of(advisory));
        // no trusted content set → not a TC remediation

        Issue issue = new Issue();
        issue.setId("CVE-2024-58264");
        issue.setRemediation(remediation);

        DependencyReport report = new DependencyReport();
        report.setRef(new PackageRef("pkg:cargo/serde-json-wasm@1.0.0"));
        report.setIssues(List.of(issue));

        assertTrue("Advisory fix should still count as an available quick-fix",
                CAIntentionAction.isQuickFixAvailable(report));
        assertFalse("Base vulnerability fix must NOT be offered for advisory-only reports",
                CAIntentionAction.hasBaseVulnerabilityFix(report));
    }
}
