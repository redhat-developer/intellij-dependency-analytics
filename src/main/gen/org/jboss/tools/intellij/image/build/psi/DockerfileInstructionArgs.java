// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.image.build.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DockerfileInstructionArgs extends PsiElement {

  @Nullable
  DockerfileVariableRef getVariableRef();

  @Nullable
  PsiElement getAnyChar();

  @Nullable
  PsiElement getIdentifier();

  @Nullable
  PsiElement getImageNameToken();

  @Nullable
  PsiElement getOtherToken();

  @Nullable
  PsiElement getPlatform();

  @Nullable
  PsiElement getString();

  @Nullable
  PsiElement getVersion();

}
