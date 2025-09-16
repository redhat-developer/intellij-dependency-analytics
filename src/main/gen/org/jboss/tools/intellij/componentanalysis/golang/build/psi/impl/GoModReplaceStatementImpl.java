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

public class GoModReplaceStatementImpl extends ASTWrapperPsiElement implements GoModReplaceStatement {

  public GoModReplaceStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoModVisitor visitor) {
    visitor.visitReplaceStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoModVisitor) accept((GoModVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GoModReplaceBlock getReplaceBlock() {
    return findChildByClass(GoModReplaceBlock.class);
  }

  @Override
  @Nullable
  public GoModReplaceSpec getReplaceSpec() {
    return findChildByClass(GoModReplaceSpec.class);
  }

}
