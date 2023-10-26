// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.OtherSpec;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.Visitor;
import org.jetbrains.annotations.NotNull;

public class OtherSpecImpl extends ASTWrapperPsiElement implements OtherSpec {

  public OtherSpecImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitOtherSpec(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

}
