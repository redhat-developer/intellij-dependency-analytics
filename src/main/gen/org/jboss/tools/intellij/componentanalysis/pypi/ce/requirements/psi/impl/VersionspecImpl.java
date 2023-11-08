// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.VersionOne;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.Versionspec;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VersionspecImpl extends ASTWrapperPsiElement implements Versionspec {

  public VersionspecImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitVersionspec(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<VersionOne> getVersionOneList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, VersionOne.class);
  }

}
