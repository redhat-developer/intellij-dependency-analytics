// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.image.build.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.jboss.tools.intellij.image.build.psi.DockerfileTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.jboss.tools.intellij.image.build.psi.*;

public class DockerfileImageNameLiteralImpl extends ASTWrapperPsiElement implements DockerfileImageNameLiteral {

  public DockerfileImageNameLiteralImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DockerfileVisitor visitor) {
    visitor.visitImageNameLiteral(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DockerfileVisitor) accept((DockerfileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(IDENTIFIER);
  }

  @Override
  @Nullable
  public PsiElement getImageNameToken() {
    return findChildByType(IMAGE_NAME_TOKEN);
  }

}
