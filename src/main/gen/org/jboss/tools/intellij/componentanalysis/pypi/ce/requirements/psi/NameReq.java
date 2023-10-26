// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NameReq extends PsiElement {

  @Nullable
  ExtrasList getExtrasList();

  @Nullable
  NameReqComment getNameReqComment();

  @NotNull
  PkgName getPkgName();

  @Nullable
  Versionspec getVersionspec();

}
