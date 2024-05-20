// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.gradle.build.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.jboss.tools.intellij.componentanalysis.gradle.build.psi.impl.*;

public interface BuildGradleTypes {

  IElementType ARTIFACT = new BuildGradleElementType("ARTIFACT");
  IElementType ARTIFACT_ID = new BuildGradleElementType("ARTIFACT_ID");
  IElementType COMMENT = new BuildGradleElementType("COMMENT");
  IElementType GROUP = new BuildGradleElementType("GROUP");
  IElementType VERSION = new BuildGradleElementType("VERSION");

  IElementType APOSTROPHE = new BuildGradleTokenType("'");
  IElementType BACKSLASH = new BuildGradleTokenType("\\\\");
  IElementType COLON = new BuildGradleTokenType(":");
  IElementType COMMA = new BuildGradleTokenType(",");
  IElementType CONFIG_NAME = new BuildGradleTokenType("CONFIG_NAME");
  IElementType CRLF = new BuildGradleTokenType("CRLF");
  IElementType DEPENDENCIES = new BuildGradleTokenType("dependencies");
  IElementType GROUP_ID = new BuildGradleTokenType("GROUP_ID");
  IElementType GROUP_KEY = new BuildGradleTokenType("GROUP_KEY");
  IElementType ID_PREFIX = new BuildGradleTokenType("ID_PREFIX");
  IElementType JAVA = new BuildGradleTokenType("java");
  IElementType LCURBRACE = new BuildGradleTokenType("{");
  IElementType LINE_COMMENT = new BuildGradleTokenType("LINE_COMMENT");
  IElementType LPARENTHESIS = new BuildGradleTokenType("(");
  IElementType LSBRACE = new BuildGradleTokenType("[");
  IElementType MAIN = new BuildGradleTokenType("main");
  IElementType NAME_KEY = new BuildGradleTokenType("NAME_KEY");
  IElementType PLUGINS = new BuildGradleTokenType("PLUGINS");
  IElementType QUATATION_MARK = new BuildGradleTokenType("\"");
  IElementType RCURBRACE = new BuildGradleTokenType("RCURBRACE");
  IElementType REPOSITORIES = new BuildGradleTokenType("repositories");
  IElementType ROOT_GROUP_KEY = new BuildGradleTokenType("ROOT_GROUP_KEY");
  IElementType ROOT_GROUP_VERSION_VALUE = new BuildGradleTokenType("ROOT_GROUP_VERSION_VALUE");
  IElementType ROOT_VERSION_KEY = new BuildGradleTokenType("ROOT_VERSION_KEY");
  IElementType RPARENTHESIS = new BuildGradleTokenType(")");
  IElementType RSBRACE = new BuildGradleTokenType("]");
  IElementType SOURCE_SETS = new BuildGradleTokenType("sourceSets");
  IElementType SPACE_CHARACTER = new BuildGradleTokenType("SPACE_CHARACTER");
  IElementType TEST = new BuildGradleTokenType("test");
  IElementType VERSION_KEY = new BuildGradleTokenType("VERSION_KEY");
  IElementType WHITE_SPACE = new BuildGradleTokenType("WHITE_SPACE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ARTIFACT) {
        return new ArtifactImpl(node);
      }
      else if (type == ARTIFACT_ID) {
        return new ArtifactIdImpl(node);
      }
      else if (type == COMMENT) {
        return new CommentImpl(node);
      }
      else if (type == GROUP) {
        return new GroupImpl(node);
      }
      else if (type == VERSION) {
        return new VersionImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
