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

package org.jboss.tools.intellij.componentanalysis.golang.build.psi;

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
import org.jboss.tools.intellij.componentanalysis.golang.build.lang.GoModLanguage;
import org.jboss.tools.intellij.componentanalysis.golang.build.lexer.GoModLexerAdapter;
import org.jboss.tools.intellij.componentanalysis.golang.build.parser.GoModParser;
import org.jetbrains.annotations.NotNull;

public class GoModParserDefinition implements ParserDefinition {

    private static final IFileElementType FILE = new IFileElementType(GoModLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new GoModLexerAdapter();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new GoModParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return GoModTokenSets.COMMENTS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return GoModTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new GoModFile(viewProvider);
    }
}