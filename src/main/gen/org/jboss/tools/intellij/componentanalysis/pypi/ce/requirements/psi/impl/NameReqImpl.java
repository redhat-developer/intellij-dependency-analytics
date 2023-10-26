// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NameReqImpl extends ASTWrapperPsiElement implements NameReq {

  public NameReqImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitNameReq(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ExtrasList getExtrasList() {
    return findChildByClass(ExtrasList.class);
  }

  @Override
  @Nullable
  public NameReqComment getNameReqComment() {
    return findChildByClass(NameReqComment.class);
  }

  @Override
  @NotNull
  public PkgName getPkgName() {
    return findNotNullChildByClass(PkgName.class);
  }

  @Override
  @Nullable
  public Versionspec getVersionspec() {
    return findChildByClass(Versionspec.class);
  }

}
