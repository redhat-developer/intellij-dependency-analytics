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

import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jboss.tools.intellij.image.build.filetype.DockerfileFileType;
import org.jboss.tools.intellij.image.build.psi.DockerfileFromInstruction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class DockerfileParserTest extends BasePlatformTestCase {

    private PsiFile parseDockerfile(String content) {
        return PsiFileFactory.getInstance(getProject())
                .createFileFromText("Dockerfile", DockerfileFileType.INSTANCE, content);
    }

    private void assertNoParseErrors(String description, String content) {
        PsiFile psiFile = parseDockerfile(content);
        assertNotNull(description + ": PSI file should be created", psiFile);

        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(psiFile, PsiErrorElement.class);
        if (!errors.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder(description + ": Found parse errors:\n");
            for (PsiErrorElement error : errors) {
                int offset = error.getTextOffset();
                String textBefore = content.substring(Math.max(0, offset - 20), offset);
                String textAfter = content.substring(offset, Math.min(content.length(), offset + 30));
                errorMsg.append("  - ").append(error.getErrorDescription())
                        .append(" at offset ").append(offset)
                        .append(": ...").append(textBefore).append(">>>").append(textAfter).append("...\n");
            }
            fail(errorMsg.toString());
        }
    }

    private String loadResource(String name) throws IOException {
        String path = "/dockerfiles/" + name;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            assertNotNull("Test resource not found: " + path, is);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void assertResourceParses(String resourceName) throws IOException {
        String content = loadResource(resourceName);
        assertNoParseErrors(resourceName, content);
    }

    // --- Basic grammar tests ---

    public void testSimpleFrom() {
        assertNoParseErrors("simple FROM", "FROM ubuntu:latest\n");
    }

    public void testFromWithTag() {
        assertNoParseErrors("FROM with tag", "FROM python:3.10-slim-buster\n");
    }

    public void testFromWithSingleDigitTag() {
        assertNoParseErrors("FROM with single-digit tag", "FROM rust:1 AS builder\n");
    }

    public void testFromWithPlatformVariable() {
        assertNoParseErrors("FROM with platform variable",
                "FROM --platform=$BUILDPLATFORM node:17.0.1-bullseye-slim as builder\n");
    }

    public void testFromWithPlatformLiteral() {
        assertNoParseErrors("FROM with platform literal",
                "FROM --platform=linux/amd64 quay.io/keycloak/keycloak:23.0.1\n");
    }

    public void testFromWithAs() {
        assertNoParseErrors("FROM with AS", "FROM golang:1.19-alpine as builder\n");
    }

    public void testFromScratch() {
        assertNoParseErrors("FROM scratch", "FROM scratch\n");
    }

    public void testArgSimple() {
        assertNoParseErrors("simple ARG", "ARG FRAMEWORK=echo\n");
    }

    public void testArgWithPath() {
        assertNoParseErrors("ARG with path", "ARG MS_HOME=/app\n");
    }

    public void testArgWithVersion() {
        assertNoParseErrors("ARG with version", "ARG CONSUL_VERSION=1.12.2\n");
    }

    public void testArgWithVariableRef() {
        assertNoParseErrors("ARG with variable ref",
                "ARG VERSION=23.0.1\nFROM quay.io/keycloak/keycloak:${VERSION}\n");
    }

    public void testMaintainer() {
        assertNoParseErrors("MAINTAINER",
                "MAINTAINER Alen Komljen <alen.komljen@live.com>\n");
    }

    public void testMultiLineRun() {
        assertNoParseErrors("multi-line RUN",
                "RUN apt-get update && \\\n  apt-get install -y git\n");
    }

    public void testMultiLineRunWithTrailingSpaces() {
        assertNoParseErrors("multi-line RUN with trailing spaces after backslash",
                "RUN rm -rf \\   \n  /tmp/*\n");
    }

    public void testCommentInsideMultiLineRun() {
        assertNoParseErrors("comment inside multi-line RUN",
                "RUN apt-get update \\\n  # install git\n  && apt-get install -y git\n");
    }

    public void testHeredoc() {
        assertNoParseErrors("heredoc syntax",
                "RUN <<EOF\napt-get update\napt-get install -y git\nEOF\n");
    }

    public void testEnvWithColon() {
        assertNoParseErrors("ENV with colon",
                "ENV PATH /usr/local/bin:/usr/bin:/bin\n");
    }

    public void testCopyWithChown() {
        assertNoParseErrors("COPY with --chown",
                "COPY --chown=node:node . .\n");
    }

    public void testCopyWithFrom() {
        assertNoParseErrors("COPY --from",
                "COPY --from=builder /app/dist ./dist/\n");
    }

    public void testRunWithMount() {
        assertNoParseErrors("RUN with --mount",
                "RUN --mount=target=/var/lib/apt/lists,type=cache,sharing=locked \\\n  apt-get update\n");
    }

    public void testCmdExecForm() {
        assertNoParseErrors("CMD exec form",
                "CMD [\"node\", \"server.js\"]\n");
    }

    public void testHealthcheck() {
        assertNoParseErrors("HEALTHCHECK",
                "HEALTHCHECK --interval=30s CMD curl -f http://localhost/\n");
    }

    public void testShell() {
        assertNoParseErrors("SHELL",
                "SHELL [\"/bin/bash\", \"-c\"]\n");
    }

    public void testStopsignal() {
        assertNoParseErrors("STOPSIGNAL", "STOPSIGNAL SIGTERM\n");
    }

    public void testOnbuild() {
        assertNoParseErrors("ONBUILD", "ONBUILD RUN pip install -r requirements.txt\n");
    }

    public void testGlobInCopy() {
        assertNoParseErrors("glob in COPY", "COPY package*.json ./\n");
    }

    public void testCommentAtEndOfFile() {
        assertNoParseErrors("comment at EOF without trailing newline",
                "FROM ubuntu\n# last comment");
    }

    public void testCommentBeforeFrom() {
        assertNoParseErrors("comment before FROM",
                "# hadolint ignore=DL3006\nFROM rust:1 AS builder\n");
    }

    public void testShellSubcommands() {
        assertNoParseErrors("shell subcommands",
                "RUN mv /target/$(cat .env-id)-$(cat .env-version).jar /target/app.jar\n");
    }

    public void testUnicodeComments() {
        assertNoParseErrors("unicode comments",
                "# 使用官方 Python 3.9 作为基础镜像\nFROM python:3.9-slim\n");
    }

    // --- FROM extraction tests ---

    public void testFromExtractedCorrectly() {
        PsiFile psiFile = parseDockerfile("FROM ubuntu:trusty\nRUN apt-get update\nFROM alpine:3.17 AS build\n");
        Collection<DockerfileFromInstruction> froms = PsiTreeUtil.findChildrenOfType(psiFile, DockerfileFromInstruction.class);
        assertEquals("Should find 2 FROM instructions", 2, froms.size());
    }

    public void testFromWithArgVariable() {
        PsiFile psiFile = parseDockerfile("ARG VERSION=23.0.1\nFROM quay.io/keycloak/keycloak:${VERSION}\nFROM scratch\nFROM quay.io/fedora/fedora:36\n");
        Collection<DockerfileFromInstruction> froms = PsiTreeUtil.findChildrenOfType(psiFile, DockerfileFromInstruction.class);
        assertEquals("Should find 3 FROM instructions", 3, froms.size());
    }

    // --- Real-world Dockerfile resource tests ---

    public void testResourceAllInstructions() throws IOException {
        assertResourceParses("all-instructions.Dockerfile");
    }

    public void testResourceMultistageWithArgs() throws IOException {
        assertResourceParses("multistage-with-args.Dockerfile");
    }

    public void testResourceMaintainerAndMultiline() throws IOException {
        assertResourceParses("maintainer-and-multiline.Dockerfile");
    }

    public void testResourceCommentsInContinuation() throws IOException {
        assertResourceParses("comments-in-continuation.Dockerfile");
    }

    public void testResourceHeredocSyntax() throws IOException {
        assertResourceParses("heredoc-syntax.Dockerfile");
    }

    public void testResourceArgWithPath() throws IOException {
        assertResourceParses("arg-with-path.Dockerfile");
    }

    public void testResourceShellSubcommands() throws IOException {
        assertResourceParses("shell-subcommands.Dockerfile");
    }

    public void testResourceSimpleWithTag() throws IOException {
        assertResourceParses("simple-with-tag.Dockerfile");
    }

    public void testResourceUnicodeComments() throws IOException {
        assertResourceParses("unicode-comments.Dockerfile");
    }
}
