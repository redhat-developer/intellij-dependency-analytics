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

package org.jboss.tools.intellij.image;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import io.github.guacsec.trustifyda.api.PackageRef;
import io.github.guacsec.trustifyda.api.v5.AnalysisReport;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import io.github.guacsec.trustifyda.api.v5.ProviderReport;
import io.github.guacsec.trustifyda.api.v5.RecommendationReport;
import io.github.guacsec.trustifyda.api.v5.RecommendationSource;
import io.github.guacsec.trustifyda.api.v5.Source;
import io.github.guacsec.trustifyda.api.v5.SourceSummary;
import io.github.guacsec.trustifyda.image.ImageRef;
import org.junit.Test;

import java.util.List;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link HardenedImageIntentionAction} and the hardened recommendation
 * extraction logic in {@link DockerfileAnnotator}.
 */
public class HardenedImageIntentionActionTest {

    private static final String IMAGE_DIGEST = "sha256:a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2";
    private static final String HARDENED_DIGEST_1 = "sha256:c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b20000";
    private static final String HARDENED_DIGEST_2 = "sha256:d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2000000";
    private static final String UBI_DIGEST = "sha256:b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b200";

    // ── HardenedImageIntentionAction unit tests ─────────────────────────────

    /** Verifies that getText() includes the target image reference. */
    @Test
    public void testGetText_containsImageReference() {
        var action = new HardenedImageIntentionAction("quay.io/hardened/nginx");
        assertEquals("Replace with Red Hat Hardened Image: quay.io/hardened/nginx", action.getText());
    }

    /** Verifies that the family name matches the RHDA convention. */
    @Test
    public void testGetFamilyName() {
        var action = new HardenedImageIntentionAction("quay.io/hardened/nginx");
        assertEquals("RHDA", action.getFamilyName());
    }

    /** Verifies that startInWriteAction returns true since the action modifies the document. */
    @Test
    public void testStartInWriteAction() {
        var action = new HardenedImageIntentionAction("quay.io/hardened/nginx");
        assertTrue("Should return true for write action", action.startInWriteAction());
    }

    /** Verifies that different image references produce distinct action text. */
    @Test
    public void testGetText_distinctForDifferentImages() {
        var action1 = new HardenedImageIntentionAction("quay.io/hardened/nginx");
        var action2 = new HardenedImageIntentionAction("quay.io/hardened/golang");
        assertFalse("Different images should produce different text",
                action1.getText().equals(action2.getText()));
    }

    // ── getHardenedRecommendations tests ────────────────────────────────────

    /** Verifies extraction of a single hardened recommendation from provider-level data. */
    @Test
    public void testGetHardenedRecommendations_singleRecommendation() throws MalformedPackageURLException {
        // Given an image with one hardened recommendation
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);
        AnalysisReport report = buildReportWithHardenedRecommendation(imagePurl,
                buildOciPurl("hardened-nginx", HARDENED_DIGEST_1, "quay.io/hardened/nginx"));

        // When extracting hardened recommendations
        List<String> recommendations = DockerfileAnnotator.getHardenedRecommendations(report, imageRef);

        // Then one recommendation should be present
        assertNotNull("Recommendations list should not be null", recommendations);
        assertEquals("Should have exactly one recommendation", 1, recommendations.size());
        assertTrue("Should contain hardened image path",
                recommendations.get(0).contains("quay.io/hardened/nginx"));
    }

    /** Verifies extraction of multiple hardened recommendations (1→N case). */
    @Test
    public void testGetHardenedRecommendations_multipleRecommendations() throws MalformedPackageURLException {
        // Given an image with two hardened recommendations
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);

        RecommendationReport rec1 = new RecommendationReport();
        rec1.setRef(new PackageRef(imagePurl));
        rec1.setRecommendation(new PackageRef(
                buildOciPurl("hardened-nginx", HARDENED_DIGEST_1, "quay.io/hardened/nginx")));

        RecommendationReport rec2 = new RecommendationReport();
        rec2.setRef(new PackageRef(imagePurl));
        rec2.setRecommendation(new PackageRef(
                buildOciPurl("hardened-nginx-alt", HARDENED_DIGEST_2, "quay.io/hardened/nginx-alt")));

        RecommendationSource recSource = new RecommendationSource();
        recSource.addDependenciesItem(rec1);
        recSource.addDependenciesItem(rec2);

        ProviderReport providerReport = new ProviderReport();
        providerReport.putRecommendationsItem("hardened", recSource);

        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("rhtpa", providerReport);

        // When extracting hardened recommendations
        List<String> recommendations = DockerfileAnnotator.getHardenedRecommendations(report, imageRef);

        // Then two recommendations should be present
        assertEquals("Should have two recommendations", 2, recommendations.size());
        assertTrue("Should contain first hardened image",
                recommendations.stream().anyMatch(r -> r.contains("quay.io/hardened/nginx")));
        assertTrue("Should contain second hardened image",
                recommendations.stream().anyMatch(r -> r.contains("quay.io/hardened/nginx-alt")));
    }

    /** Verifies that duplicate hardened recommendations are deduplicated. */
    @Test
    public void testGetHardenedRecommendations_deduplicatesSameImage() throws MalformedPackageURLException {
        // Given an image with the same hardened recommendation appearing in two providers
        PackageURL imagePurl = buildOciPurl("golang", IMAGE_DIGEST, "docker.io/library/golang");
        ImageRef imageRef = new ImageRef(imagePurl);

        PackageURL hardenedPurl = buildOciPurl("go", HARDENED_DIGEST_1, "quay.io/hummingbird/go");

        // First provider recommends the same image
        RecommendationReport rec1 = new RecommendationReport();
        rec1.setRef(new PackageRef(imagePurl));
        rec1.setRecommendation(new PackageRef(hardenedPurl));
        RecommendationSource recSource1 = new RecommendationSource();
        recSource1.addDependenciesItem(rec1);

        ProviderReport provider1 = new ProviderReport();
        provider1.putRecommendationsItem("hardened", recSource1);

        // Second provider also recommends the same image
        RecommendationReport rec2 = new RecommendationReport();
        rec2.setRef(new PackageRef(imagePurl));
        rec2.setRecommendation(new PackageRef(hardenedPurl));
        RecommendationSource recSource2 = new RecommendationSource();
        recSource2.addDependenciesItem(rec2);

        ProviderReport provider2 = new ProviderReport();
        provider2.putRecommendationsItem("hardened", recSource2);

        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("provider1", provider1);
        report.putProvidersItem("provider2", provider2);

        // When extracting hardened recommendations
        List<String> recommendations = DockerfileAnnotator.getHardenedRecommendations(report, imageRef);

        // Then only one unique recommendation should be present
        assertEquals("Should deduplicate identical recommendations", 1, recommendations.size());
        assertTrue("Should contain hardened image path",
                recommendations.get(0).contains("quay.io/hummingbird/go"));
    }

    /** Verifies that an empty list is returned when no provider-level recommendations exist. */
    @Test
    public void testGetHardenedRecommendations_emptyWhenNoRecommendations() throws MalformedPackageURLException {
        // Given an image with only source-level UBI recommendation, no provider-level
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);

        DependencyReport dep = new DependencyReport();
        dep.setRef(new PackageRef(imagePurl));
        dep.setRecommendation(new PackageRef(
                buildOciPurl("ubi", UBI_DIGEST, "registry.access.redhat.com/ubi9/ubi")));
        Source source = new Source();
        source.addDependenciesItem(dep);
        ProviderReport providerReport = new ProviderReport();
        providerReport.putSourcesItem("ubi", source);
        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("rhtpa", providerReport);

        // When extracting hardened recommendations
        List<String> recommendations = DockerfileAnnotator.getHardenedRecommendations(report, imageRef);

        // Then the list should be empty
        assertTrue("Should return empty list when no hardened recommendations",
                recommendations.isEmpty());
    }

    /** Verifies that an empty list is returned for an empty report. */
    @Test
    public void testGetHardenedRecommendations_emptyForEmptyReport() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);
        AnalysisReport report = new AnalysisReport();

        List<String> recommendations = DockerfileAnnotator.getHardenedRecommendations(report, imageRef);

        assertTrue("Should return empty list for empty report", recommendations.isEmpty());
    }

    // ── isReportAvailable with recommendations tests ────────────────────────

    /** Verifies that isReportAvailable returns true when only hardened recommendations exist. */
    @Test
    public void testIsReportAvailable_trueForRecommendationsOnly() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        AnalysisReport report = buildReportWithHardenedRecommendation(imagePurl,
                buildOciPurl("hardened-nginx", HARDENED_DIGEST_1, "quay.io/hardened/nginx"));

        assertTrue("Should be available when provider-level recommendations exist",
                DockerfileAnnotator.isReportAvailable(report));
    }

    /** Verifies that isReportAvailable returns true when both vulnerabilities and recommendations exist. */
    @Test
    public void testIsReportAvailable_trueForBothVulnsAndRecommendations() throws MalformedPackageURLException {
        // Given a report with both vulnerabilities and recommendations
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");

        SourceSummary summary = new SourceSummary();
        summary.setTotal(3);
        Source source = new Source();
        source.setSummary(summary);

        RecommendationReport recReport = new RecommendationReport();
        recReport.setRef(new PackageRef(imagePurl));
        recReport.setRecommendation(new PackageRef(
                buildOciPurl("hardened-nginx", HARDENED_DIGEST_1, "quay.io/hardened/nginx")));
        RecommendationSource recSource = new RecommendationSource();
        recSource.addDependenciesItem(recReport);

        ProviderReport providerReport = new ProviderReport();
        providerReport.putSourcesItem("snyk", source);
        providerReport.putRecommendationsItem("hardened", recSource);

        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("rhtpa", providerReport);

        assertTrue("Should be available when both vulns and recommendations exist",
                DockerfileAnnotator.isReportAvailable(report));
    }

    // ── generateMessage with hardened recommendations tests ─────────────────

    /** Verifies message includes hardened recommendation text. */
    @Test
    public void testGenerateMessage_withHardenedRecommendation() {
        AnalysisReport report = new AnalysisReport();
        String message = DockerfileAnnotator.generateMessage("nginx:latest", report,
                null, List.of("quay.io/hardened/nginx"));

        assertTrue("Should contain hardened recommendation",
                message.contains("Red Hat Hardened Image available: quay.io/hardened/nginx"));
    }

    /** Verifies message includes both UBI and hardened recommendation text. */
    @Test
    public void testGenerateMessage_withBothRecommendations() {
        AnalysisReport report = new AnalysisReport();
        String message = DockerfileAnnotator.generateMessage("nginx:latest", report,
                "ubi9/ubi", List.of("quay.io/hardened/nginx"));

        assertTrue("Should contain UBI recommendation",
                message.contains("Replace your image with RedHat UBI: ubi9/ubi"));
        assertTrue("Should contain hardened recommendation",
                message.contains("Red Hat Hardened Image available: quay.io/hardened/nginx"));
    }

    /** Verifies message with multiple hardened recommendations lists them comma-separated. */
    @Test
    public void testGenerateMessage_withMultipleHardenedRecommendations() {
        AnalysisReport report = new AnalysisReport();
        String message = DockerfileAnnotator.generateMessage("nginx:latest", report,
                null, List.of("quay.io/hardened/nginx", "quay.io/hardened/nginx-alt"));

        assertTrue("Should contain both hardened recommendations",
                message.contains("quay.io/hardened/nginx, quay.io/hardened/nginx-alt"));
    }

    /** Verifies message contains only image name when no recommendations exist. */
    @Test
    public void testGenerateMessage_withNoRecommendations() {
        AnalysisReport report = new AnalysisReport();
        String message = DockerfileAnnotator.generateMessage("nginx:latest", report,
                null, List.of());

        assertEquals("Should only contain image name", "nginx:latest", message);
    }

    // ── generateTooltip with hardened recommendations tests ──────────────────

    /** Verifies tooltip includes hardened recommendation HTML. */
    @Test
    public void testGenerateTooltip_withHardenedRecommendation() {
        AnalysisReport report = new AnalysisReport();
        String tooltip = DockerfileAnnotator.generateTooltip("nginx:latest", report,
                null, List.of("quay.io/hardened/nginx"));

        assertTrue("Should contain hardened recommendation",
                tooltip.contains("Red Hat Hardened Image available: quay.io/hardened/nginx"));
    }

    // ── Helper methods ──────────────────────────────────────────────────────

    private static PackageURL buildOciPurl(String name, String digest, String repositoryUrl)
            throws MalformedPackageURLException {
        TreeMap<String, String> qualifiers = new TreeMap<>();
        if (repositoryUrl != null && !repositoryUrl.equalsIgnoreCase(name)) {
            qualifiers.put("repository_url", repositoryUrl.toLowerCase());
        }
        return new PackageURL("oci", null, name.toLowerCase(), digest, qualifiers, null);
    }

    private static AnalysisReport buildReportWithHardenedRecommendation(
            PackageURL imagePurl, PackageURL hardenedPurl) {
        RecommendationReport recReport = new RecommendationReport();
        recReport.setRef(new PackageRef(imagePurl));
        recReport.setRecommendation(new PackageRef(hardenedPurl));

        RecommendationSource recSource = new RecommendationSource();
        recSource.addDependenciesItem(recReport);

        ProviderReport providerReport = new ProviderReport();
        providerReport.putRecommendationsItem("hardened", recSource);

        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("rhtpa", providerReport);
        return report;
    }
}
