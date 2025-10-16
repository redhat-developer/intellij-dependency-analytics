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

public class GoModExcludeSpecImpl extends ASTWrapperPsiElement implements GoModExcludeSpec {

  public GoModExcludeSpecImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GoModVisitor visitor) {
    visitor.visitExcludeSpec(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GoModVisitor) accept((GoModVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getIdentifier() {
    return findNotNullChildByType(IDENTIFIER);
  }

  @Override
  @Nullable
  public PsiElement getVersion() {
    return findChildByType(VERSION);
  }

}
