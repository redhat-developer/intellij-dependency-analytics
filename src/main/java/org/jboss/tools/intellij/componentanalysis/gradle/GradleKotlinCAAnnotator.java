package org.jboss.tools.intellij.componentanalysis.gradle;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtCallExpression;
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;
import org.jetbrains.kotlin.psi.KtStringTemplateEntry;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;
import org.jetbrains.kotlin.psi.KtValueArgument;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jboss.tools.intellij.componentanalysis.CAUtil.EXHORT_IGNORE;
import static org.jboss.tools.intellij.componentanalysis.CAUtil.TRUSTIFY_DA_IGNORE;

public final class GradleKotlinCAAnnotator extends GradleCAAnnotator {
    @Override
    protected String getInspectionShortName() {
        return GradleKotlinCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if (!"build.gradle.kts".equals(file.getName())) {
            return Collections.emptyMap();
        }

        Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        Set<Integer> ignoredLines = collectIgnoredLines(file, document);

        Map<Dependency, List<PsiElement>> dependencies = new HashMap<>();
        for (KtCallExpression call : PsiTreeUtil.findChildrenOfType(file, KtCallExpression.class)) {
            if (call.getCalleeExpression() == null || !isDependencyConfiguration(call.getCalleeExpression().getText())) {
                continue;
            }
            if (document != null && ignoredLines.contains(document.getLineNumber(call.getTextRange().getStartOffset()))) {
                continue;
            }
            Parsed parsed = parseCall(file, call);
            if (parsed != null) {
                dependencies.computeIfAbsent(parsed.dependency(), ignored -> new LinkedList<>()).add(parsed.anchor());
            }
        }
        return dependencies;
    }

    private static @Nullable Parsed parseCall(PsiFile file, KtCallExpression call) {
        List<KtValueArgument> args = call.getValueArguments();
        if (args.size() == 1) {
            KtExpression arg = args.get(0).getArgumentExpression();
            if (arg instanceof KtStringTemplateExpression literal) {
                // implementation("group:artifact:version")
                String coordinate = literalText(literal);
                if (coordinate == null) {
                    return null;
                }
                String[] parts = coordinate.split(":", -1);
                if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
                    return null;
                }
                return new Parsed(new Dependency("maven", parts[0], parts[1], parts[2]), literal);
            }
            // implementation(libs.commons.text) -> version catalog
            List<String> accessor = catalogAccessorPath(arg);
            if (accessor != null) {
                GradleVersionCatalog.Entry entry = GradleVersionCatalog.resolve(file, accessor);
                if (entry != null) {
                    return new Parsed(new Dependency("maven", entry.group(), entry.artifact(), entry.version()), arg);
                }
            }
            return null;
        }
        // implementation(group = "g", name = "a", version = "v")  |  implementation(group = "g", name = "a", version = "v", ...)
        return parseNamedArguments(args);
    }

    private static @Nullable Parsed parseNamedArguments(List<KtValueArgument> args) {
        String group = null, name = null, version = null;
        KtStringTemplateExpression versionLiteral = null;
        for (KtValueArgument arg : args) {
            if (arg.getArgumentName() == null || !(arg.getArgumentExpression() instanceof KtStringTemplateExpression literal)) {
                continue;
            }
            String value = literalText(literal);
            if (value == null) {
                continue;
            }
            switch (arg.getArgumentName().getAsName().asString()) {
                case "group" -> group = value;
                case "name" -> name = value;
                case "version" -> {
                    version = value;
                    versionLiteral = literal;
                }
                case "module" -> {
                    String[] gm = value.split(":", -1);
                    if (gm.length == 2) {
                        group = gm[0];
                        name = gm[1];
                    }
                }
                default -> { /* ignore classifier, ext, etc. */ }
            }
        }
        if (group == null || name == null || version == null
                || group.isBlank() || name.isBlank() || version.isBlank()) {
            return null;
        }
        return new Parsed(new Dependency("maven", group, name, version), versionLiteral);
    }

    /** Returns the concatenated literal text, or {@code null} if the string contains interpolation. */
    private static @Nullable String literalText(KtStringTemplateExpression literal) {
        StringBuilder coordinate = new StringBuilder();
        for (KtStringTemplateEntry entry : literal.getEntries()) {
            if (!(entry instanceof KtLiteralStringTemplateEntry)) {
                return null;
            }
            coordinate.append(entry.getText());
        }
        return coordinate.toString();
    }

    /**
     * Returns the accessor segments after the {@code libs} root for a version-catalog reference
     * such as {@code libs.commons.text} (yielding {@code [commons, text]}), or {@code null} if the
     * expression is not a {@code libs.*} catalog accessor.
     */
    static @Nullable List<String> catalogAccessorPath(@Nullable PsiElement expression) {
        LinkedList<String> segments = new LinkedList<>();
        PsiElement current = expression;
        while (current instanceof KtDotQualifiedExpression dotted) {
            if (!(dotted.getSelectorExpression() instanceof KtNameReferenceExpression selector)) {
                return null;
            }
            segments.addFirst(selector.getReferencedName());
            current = dotted.getReceiverExpression();
        }
        if (!(current instanceof KtNameReferenceExpression root) || !"libs".equals(root.getReferencedName())) {
            return null;
        }
        return segments.isEmpty() ? null : segments;
    }

    /** Lines carrying a {@code trustify-da-ignore} / {@code exhortignore} comment; those dependencies are skipped. */
    private static Set<Integer> collectIgnoredLines(PsiFile file, @Nullable Document document) {
        if (document == null) {
            return Collections.emptySet();
        }
        Set<Integer> lines = new HashSet<>();
        for (PsiComment comment : PsiTreeUtil.collectElementsOfType(file, PsiComment.class)) {
            String text = comment.getText();
            if (text.contains(TRUSTIFY_DA_IGNORE) || text.contains(EXHORT_IGNORE)) {
                lines.add(document.getLineNumber(comment.getTextRange().getStartOffset()));
            }
        }
        return lines;
    }

    private static boolean isDependencyConfiguration(String configuration) {
        return configuration.equals("implementation") || configuration.equals("api")
                || configuration.endsWith("Implementation") || configuration.endsWith("Only")
                || configuration.endsWith("Api");
    }

    private record Parsed(Dependency dependency, PsiElement anchor) {}
}
