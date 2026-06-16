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

import io.github.guacsec.trustifyda.api.v5.Severity;
import io.github.guacsec.trustifyda.api.v5.Source;
import io.github.guacsec.trustifyda.api.v5.SourceSummary;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link DockerfileAnnotator#getSeverity(Source)} severity cascade logic.
 */
public class DockerfileAnnotatorTest {

    private static Source createSourceWithSummary(Integer critical, Integer high, Integer medium, Integer low, Integer unknown) {
        var summary = new SourceSummary();
        summary.setCritical(critical);
        summary.setHigh(high);
        summary.setMedium(medium);
        summary.setLow(low);
        summary.setUnknown(unknown);
        var source = new Source();
        source.setSummary(summary);
        return source;
    }

    /**
     * Verifies that critical severity takes precedence over all others.
     */
    @Test
    public void testGetSeverityCritical() {
        Source source = createSourceWithSummary(1, 2, 3, 4, 5);
        assertEquals(Severity.CRITICAL, DockerfileAnnotator.getSeverity(source));
    }

    /**
     * Verifies that high severity is returned when no critical vulnerabilities exist.
     */
    @Test
    public void testGetSeverityHigh() {
        Source source = createSourceWithSummary(0, 1, 2, 3, 4);
        assertEquals(Severity.HIGH, DockerfileAnnotator.getSeverity(source));
    }

    /**
     * Verifies that medium severity is returned when no critical or high vulnerabilities exist.
     */
    @Test
    public void testGetSeverityMedium() {
        Source source = createSourceWithSummary(0, 0, 1, 2, 3);
        assertEquals(Severity.MEDIUM, DockerfileAnnotator.getSeverity(source));
    }

    /**
     * Verifies that low severity is returned when no critical, high, or medium vulnerabilities exist.
     */
    @Test
    public void testGetSeverityLow() {
        Source source = createSourceWithSummary(0, 0, 0, 1, 2);
        assertEquals(Severity.LOW, DockerfileAnnotator.getSeverity(source));
    }

    /**
     * Verifies that unknown severity is returned when only unknown-severity vulnerabilities exist.
     */
    @Test
    public void testGetSeverityUnknown() {
        Source source = createSourceWithSummary(0, 0, 0, 0, 1);
        assertEquals(Severity.UNKNOWN, DockerfileAnnotator.getSeverity(source));
    }

    /**
     * Verifies that null is returned when no vulnerabilities of any severity exist.
     */
    @Test
    public void testGetSeverityNone() {
        Source source = createSourceWithSummary(0, 0, 0, 0, 0);
        assertNull(DockerfileAnnotator.getSeverity(source));
    }

    /**
     * Verifies that null is returned when summary is null.
     */
    @Test
    public void testGetSeverityNullSummary() {
        var source = new Source();
        assertNull(DockerfileAnnotator.getSeverity(source));
    }

    /**
     * Verifies that null is returned when all severity counts are null.
     */
    @Test
    public void testGetSeverityNullCounts() {
        Source source = createSourceWithSummary(null, null, null, null, null);
        assertNull(DockerfileAnnotator.getSeverity(source));
    }
}
