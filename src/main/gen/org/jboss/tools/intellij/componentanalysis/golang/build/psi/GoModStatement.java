// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.golang.build.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GoModStatement extends PsiElement {

  @Nullable
  GoModExcludeStatement getExcludeStatement();

  @Nullable
  GoModGoStatement getGoStatement();

  @Nullable
  GoModModuleStatement getModuleStatement();

  @Nullable
  GoModReplaceStatement getReplaceStatement();

  @Nullable
  GoModRequireStatement getRequireStatement();

}
