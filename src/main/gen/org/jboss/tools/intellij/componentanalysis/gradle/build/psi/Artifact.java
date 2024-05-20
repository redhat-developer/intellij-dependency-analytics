// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.gradle.build.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface Artifact extends PsiElement {

  @NotNull
  ArtifactId getArtifactId();

  @Nullable
  Comment getComment();

  @NotNull
  Group getGroup();

  @NotNull
  Version getVersion();

  @Nullable
  PsiElement getConfigName();

  @Nullable
  PsiElement getWhiteSpace();

}
