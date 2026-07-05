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

import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for recommendation message and tooltip generation in DockerfileAnnotator,
 * covering UBI, hardened image, and disabled recommendation scenarios.
 */
public class DockerfileAnnotatorRecommendationTest {

    private static final String IMAGE_DIGEST = "sha256:a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2";
    private static final String UBI_DIGEST = "sha256:b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b200";
    private static final String HARDENED_DIGEST = "sha256:c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b20000";

    // ── generateMessage tests ─────────────────────────────────────────────────

    @Test
    public void testGenerateMessageWithUbiRecommendation() {
        AnalysisReport report = new AnalysisReport();
        String message = DockerfileAnnotator.generateMessage("nginx:latest", report, "ubi9/ubi", null);

        assertTrue("Should contain UBI recommendation",
                message.contains("Replace your image with RedHat UBI: ubi9/ubi"));
        assertFalse("Should not contain hardened recommendation",
                message.contains("Hardened Image"));
    }

    @Test
    public void testGenerateMessageWithHardenedRecommendation() {
        AnalysisReport report = new AnalysisReport();
        String message = DockerfileAnnotator.generateMessage("nginx:latest", report, null, "hardened-nginx");

        assertTrue("Should contain hardened recommendation",
                message.contains("A Red Hat Hardened Image is available: hardened-nginx"));
        assertFalse("Should not contain UBI recommendation",
                message.contains("Replace your image with RedHat UBI"));
    }

    @Test
    public void testGenerateMessageWithBothRecommendations() {
        AnalysisReport report = new AnalysisReport();
        String message = DockerfileAnnotator.generateMessage("nginx:latest", report, "ubi9/ubi", "hardened-nginx");

        assertTrue("Should contain UBI recommendation",
                message.contains("Replace your image with RedHat UBI: ubi9/ubi"));
        assertTrue("Should contain hardened recommendation",
                message.contains("A Red Hat Hardened Image is available: hardened-nginx"));
    }

    @Test
    public void testGenerateMessageWithNoRecommendations() {
        AnalysisReport report = new AnalysisReport();
        String message = DockerfileAnnotator.generateMessage("nginx:latest", report, null, null);

        assertEquals("Should only contain image name", "nginx:latest", message);
    }

    // ── generateTooltip tests ─────────────────────────────────────────────────

    @Test
    public void testGenerateTooltipWithHardenedRecommendation() {
        AnalysisReport report = new AnalysisReport();
        String tooltip = DockerfileAnnotator.generateTooltip("nginx:latest", report, null, "hardened-nginx");

        assertTrue("Should contain hardened recommendation",
                tooltip.contains("A Red Hat Hardened Image is available: hardened-nginx"));
    }

    @Test
    public void testGenerateTooltipWithBothRecommendations() {
        AnalysisReport report = new AnalysisReport();
        String tooltip = DockerfileAnnotator.generateTooltip("nginx:latest", report, "ubi9/ubi", "hardened-nginx");

        assertTrue("Should contain UBI recommendation",
                tooltip.contains("Replace your image with RedHat UBI: ubi9/ubi"));
        assertTrue("Should contain hardened recommendation",
                tooltip.contains("A Red Hat Hardened Image is available: hardened-nginx"));
    }

    @Test
    public void testGenerateTooltipWithNoRecommendations() {
        AnalysisReport report = new AnalysisReport();
        String tooltip = DockerfileAnnotator.generateTooltip("nginx:latest", report, null, null);

        assertFalse("Should not contain UBI text", tooltip.contains("Replace your image"));
        assertFalse("Should not contain hardened text", tooltip.contains("Hardened Image"));
        assertTrue("Should contain image name", tooltip.contains("nginx:latest"));
    }

    // ── getRecommendation tests (reads from source-level dependencies) ───────

    @Test
    public void testGetRecommendationFromSources() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);

        AnalysisReport report = buildReportWithUbiSource(imagePurl,
                buildOciPurl("ubi", UBI_DIGEST, "registry.access.redhat.com/ubi9/ubi"));

        String recommendation = DockerfileAnnotator.getRecommendation(report, imageRef);

        assertNotNull("UBI recommendation should be present", recommendation);
        assertTrue("Should contain ubi path", recommendation.contains("ubi9/ubi"));
    }

    @Test
    public void testGetRecommendationReturnsNullWhenNoSources() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);

        // Report with only provider-level recommendations, no sources
        AnalysisReport report = buildReportWithHardenedRecommendation(imagePurl,
                buildOciPurl("hardened-nginx", HARDENED_DIGEST, "registry.access.redhat.com/hardened/nginx"));

        String recommendation = DockerfileAnnotator.getRecommendation(report, imageRef);

        assertNull("UBI recommendation should be null when no sources exist", recommendation);
    }

    @Test
    public void testGetRecommendationHandlesNullSourceValue() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);

        ProviderReport providerReport = new ProviderReport();
        providerReport.putSourcesItem("ubi", null);

        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("rhtpa", providerReport);

        String recommendation = DockerfileAnnotator.getRecommendation(report, imageRef);

        assertNull("Should return null for null source value", recommendation);
    }

    // ── getHardenedRecommendation tests (reads from provider-level recommendations) ──

    @Test
    public void testGetHardenedRecommendationFromProviderRecommendations() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);

        AnalysisReport report = buildReportWithHardenedRecommendation(imagePurl,
                buildOciPurl("hardened-nginx", HARDENED_DIGEST, "registry.access.redhat.com/hardened/nginx"));

        String recommendation = DockerfileAnnotator.getHardenedRecommendation(report, imageRef);

        assertNotNull("Hardened recommendation should be present", recommendation);
        assertTrue("Should contain hardened path", recommendation.contains("hardened"));
    }

    @Test
    public void testGetHardenedRecommendationReturnsNullWhenOnlySourcesExist() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);

        // Report with only source-level dependencies, no provider-level recommendations
        AnalysisReport report = buildReportWithUbiSource(imagePurl,
                buildOciPurl("ubi", UBI_DIGEST, "registry.access.redhat.com/ubi9/ubi"));

        String recommendation = DockerfileAnnotator.getHardenedRecommendation(report, imageRef);

        assertNull("Hardened recommendation should be null when only sources exist", recommendation);
    }

    @Test
    public void testGetBothRecommendationsFromMixedReport() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);

        // Build a report with both source-level UBI and provider-level hardened
        PackageURL ubiPurl = buildOciPurl("ubi", UBI_DIGEST, "registry.access.redhat.com/ubi9/ubi");
        PackageURL hardenedPurl = buildOciPurl("hardened-nginx", HARDENED_DIGEST, "registry.access.redhat.com/hardened/nginx");

        DependencyReport dep = new DependencyReport();
        dep.setRef(new PackageRef(imagePurl));
        dep.setRecommendation(new PackageRef(ubiPurl));
        Source source = new Source();
        source.addDependenciesItem(dep);

        RecommendationReport recReport = new RecommendationReport();
        recReport.setRef(new PackageRef(imagePurl));
        recReport.setRecommendation(new PackageRef(hardenedPurl));
        RecommendationSource recSource = new RecommendationSource();
        recSource.addDependenciesItem(recReport);

        ProviderReport providerReport = new ProviderReport();
        providerReport.putSourcesItem("ubi", source);
        providerReport.putRecommendationsItem("hardened", recSource);

        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("rhtpa", providerReport);

        String ubiRec = DockerfileAnnotator.getRecommendation(report, imageRef);
        String hardenedRec = DockerfileAnnotator.getHardenedRecommendation(report, imageRef);

        assertNotNull("UBI recommendation should be present", ubiRec);
        assertTrue("Should contain ubi path", ubiRec.contains("ubi9/ubi"));
        assertNotNull("Hardened recommendation should be present", hardenedRec);
        assertTrue("Should contain hardened path", hardenedRec.contains("hardened"));
    }

    @Test
    public void testGetRecommendationsReturnNullForEmptyReport() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);
        AnalysisReport report = new AnalysisReport();

        String ubiRec = DockerfileAnnotator.getRecommendation(report, imageRef);
        String hardenedRec = DockerfileAnnotator.getHardenedRecommendation(report, imageRef);

        assertNull("UBI recommendation should be null for empty report", ubiRec);
        assertNull("Hardened recommendation should be null for empty report", hardenedRec);
    }

    // ── isReportAvailable tests ──────────────────────────────────────────────

    @Test
    public void testIsReportAvailableWithVulnerabilities() {
        SourceSummary summary = new SourceSummary();
        summary.setTotal(5);
        Source source = new Source();
        source.setSummary(summary);

        ProviderReport providerReport = new ProviderReport();
        providerReport.putSourcesItem("snyk", source);

        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("rhtpa", providerReport);

        assertTrue("Should be available when vulnerabilities exist",
                DockerfileAnnotator.isReportAvailable(report));
    }

    @Test
    public void testIsReportAvailableWithOnlyRecommendations() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        PackageURL hardenedPurl = buildOciPurl("hardened-nginx", HARDENED_DIGEST, "registry.access.redhat.com/hardened/nginx");

        AnalysisReport report = buildReportWithHardenedRecommendation(imagePurl, hardenedPurl);

        assertTrue("Should be available when provider-level recommendations exist",
                DockerfileAnnotator.isReportAvailable(report));
    }

    @Test
    public void testIsReportAvailableReturnsFalseForEmptyReport() {
        AnalysisReport report = new AnalysisReport();

        assertFalse("Should not be available for empty report",
                DockerfileAnnotator.isReportAvailable(report));
    }

    @Test
    public void testIsReportAvailableReturnsFalseForZeroVulnsAndNoRecommendations() {
        SourceSummary summary = new SourceSummary();
        summary.setTotal(0);
        Source source = new Source();
        source.setSummary(summary);

        ProviderReport providerReport = new ProviderReport();
        providerReport.putSourcesItem("snyk", source);

        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("rhtpa", providerReport);

        assertFalse("Should not be available when zero vulns and no recommendations",
                DockerfileAnnotator.isReportAvailable(report));
    }

    // ── Double-encoded PURL tests ─────────────────────────────────────────────

    @Test
    public void testGetHardenedRecommendationWithDoubleEncodedPurl() throws MalformedPackageURLException {
        PackageURL imagePurl = buildOciPurl("nginx", IMAGE_DIGEST, "docker.io/library/nginx");
        ImageRef imageRef = new ImageRef(imagePurl);

        // Simulate backend sending double-encoded repository_url (e.g., quay.io%252Fhummingbird%252Fgo)
        TreeMap<String, String> qualifiers = new TreeMap<>();
        qualifiers.put("repository_url", "quay.io%2Fhummingbird%2Fgo");
        PackageURL hardenedPurl = new PackageURL("oci", null, "go", HARDENED_DIGEST, qualifiers, null);

        AnalysisReport report = buildReportWithHardenedRecommendation(imagePurl, hardenedPurl);

        String recommendation = DockerfileAnnotator.getHardenedRecommendation(report, imageRef);

        assertNotNull("Should parse double-encoded PURL successfully", recommendation);
        assertTrue("Should contain decoded path", recommendation.contains("quay.io/hummingbird/go"));
    }

    // ── Helper methods ───────────────────────────────────────────────────────

    private static PackageURL buildOciPurl(String name, String digest, String repositoryUrl)
            throws MalformedPackageURLException {
        TreeMap<String, String> qualifiers = new TreeMap<>();
        if (repositoryUrl != null && !repositoryUrl.equalsIgnoreCase(name)) {
            qualifiers.put("repository_url", repositoryUrl.toLowerCase());
        }
        return new PackageURL("oci", null, name.toLowerCase(), digest, qualifiers, null);
    }

    /** Builds a report with a UBI recommendation in source-level dependencies. */
    private static AnalysisReport buildReportWithUbiSource(PackageURL imagePurl, PackageURL ubiPurl) {
        DependencyReport dep = new DependencyReport();
        dep.setRef(new PackageRef(imagePurl));
        dep.setRecommendation(new PackageRef(ubiPurl));

        Source source = new Source();
        source.addDependenciesItem(dep);

        ProviderReport providerReport = new ProviderReport();
        providerReport.putSourcesItem("ubi", source);

        AnalysisReport report = new AnalysisReport();
        report.putProvidersItem("rhtpa", providerReport);
        return report;
    }

    /** Builds a report with a hardened recommendation in provider-level recommendations. */
    private static AnalysisReport buildReportWithHardenedRecommendation(PackageURL imagePurl, PackageURL hardenedPurl) {
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
