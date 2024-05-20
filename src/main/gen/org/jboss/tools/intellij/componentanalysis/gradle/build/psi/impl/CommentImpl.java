// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.gradle.build.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.jboss.tools.intellij.componentanalysis.gradle.build.psi.BuildGradleTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.jboss.tools.intellij.componentanalysis.gradle.build.psi.*;

public class CommentImpl extends ASTWrapperPsiElement implements Comment {

  public CommentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitComment(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getComment() {
    return findChildByType(COMMENT);
  }

  @Override
  @Nullable
  public PsiElement getLineComment() {
    return findChildByType(LINE_COMMENT);
  }

}
