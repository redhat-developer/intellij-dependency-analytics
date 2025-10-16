// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.image.build.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DockerfileItem extends PsiElement {

  @Nullable
  DockerfileInstruction getInstruction();

  @Nullable
  PsiElement getComment();

  @Nullable
  PsiElement getNewline();

}
