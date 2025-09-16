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

public class DockerfilePlatformValueImpl extends ASTWrapperPsiElement implements DockerfilePlatformValue {

  public DockerfilePlatformValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DockerfileVisitor visitor) {
    visitor.visitPlatformValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DockerfileVisitor) accept((DockerfileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DockerfileVariableRef getVariableRef() {
    return findChildByClass(DockerfileVariableRef.class);
  }

  @Override
  @Nullable
  public PsiElement getPlatform() {
    return findChildByType(PLATFORM);
  }

}
