// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.image.build.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DockerfileFromInstruction extends PsiElement {

  @Nullable
  DockerfileAsClause getAsClause();

  @NotNull
  DockerfileImageName getImageName();

  @Nullable
  DockerfilePlatformOption getPlatformOption();

}
