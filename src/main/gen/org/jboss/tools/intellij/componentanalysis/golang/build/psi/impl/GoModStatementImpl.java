// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.golang.build.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.jboss.tools.intellij.componentanalysis.golang.build.psi.GoModTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.jboss.tools.intellij.componentanalysis.golang.build.psi.*;

public class GoModStatementImpl extends ASTWrapperPsiElement implements GoModStatement {

  public GoModStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoModVisitor visitor) {
    visitor.visitStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoModVisitor) accept((GoModVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoModExcludeStatement getExcludeStatement() {
    return findChildByClass(GoModExcludeStatement.class);
  }

  @Override
  @Nullable
  public GoModGoStatement getGoStatement() {
    return findChildByClass(GoModGoStatement.class);
  }

  @Override
  @Nullable
  public GoModModuleStatement getModuleStatement() {
    return findChildByClass(GoModModuleStatement.class);
  }

  @Override
  @Nullable
  public GoModReplaceStatement getReplaceStatement() {
    return findChildByClass(GoModReplaceStatement.class);
  }

  @Override
  @Nullable
  public GoModRequireStatement getRequireStatement() {
    return findChildByClass(GoModRequireStatement.class);
  }

}
