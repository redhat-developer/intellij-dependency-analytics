// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.VersionCmpValue;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.VersionOne;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.VersionValue;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.Visitor;
import org.jetbrains.annotations.NotNull;

public class VersionOneImpl extends ASTWrapperPsiElement implements VersionOne {

  public VersionOneImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitVersionOne(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public VersionCmpValue getVersionCmpValue() {
    return findNotNullChildByClass(VersionCmpValue.class);
  }

  @Override
  @NotNull
  public VersionValue getVersionValue() {
    return findNotNullChildByClass(VersionValue.class);
  }

}
