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

package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.lang.RequirementsLanguage;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.lexer.RequirementsLexerAdapter;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.parser.RequirementsParser;
import org.jetbrains.annotations.NotNull;

public class RequirementsParserDefinition implements ParserDefinition {

    private static final IFileElementType FILE = new IFileElementType(RequirementsLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new RequirementsLexerAdapter();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new RequirementsParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return RequirementsTokenSets.COMMENTS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return RequirementsTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new RequirementsFile(viewProvider);
    }
}
