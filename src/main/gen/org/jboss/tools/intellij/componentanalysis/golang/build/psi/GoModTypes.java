// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.golang.build.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.jboss.tools.intellij.componentanalysis.golang.build.psi.impl.*;

public interface GoModTypes {

  IElementType EXCLUDE_BLOCK = new GoModElementType("EXCLUDE_BLOCK");
  IElementType EXCLUDE_SPEC = new GoModElementType("EXCLUDE_SPEC");
  IElementType EXCLUDE_STATEMENT = new GoModElementType("EXCLUDE_STATEMENT");
  IElementType GO_STATEMENT = new GoModElementType("GO_STATEMENT");
  IElementType ITEM = new GoModElementType("ITEM");
  IElementType MODULE_STATEMENT = new GoModElementType("MODULE_STATEMENT");
  IElementType REPLACE_BLOCK = new GoModElementType("REPLACE_BLOCK");
  IElementType REPLACE_SPEC = new GoModElementType("REPLACE_SPEC");
  IElementType REPLACE_STATEMENT = new GoModElementType("REPLACE_STATEMENT");
  IElementType REQUIRE_BLOCK = new GoModElementType("REQUIRE_BLOCK");
  IElementType REQUIRE_SPEC = new GoModElementType("REQUIRE_SPEC");
  IElementType REQUIRE_STATEMENT = new GoModElementType("REQUIRE_STATEMENT");
  IElementType STATEMENT = new GoModElementType("STATEMENT");

  IElementType ARROW = new GoModTokenType("=>");
  IElementType COMMENT = new GoModTokenType("COMMENT");
  IElementType EXCLUDE = new GoModTokenType("exclude");
  IElementType GO = new GoModTokenType("go");
  IElementType IDENTIFIER = new GoModTokenType("IDENTIFIER");
  IElementType INDIRECT = new GoModTokenType("indirect");
  IElementType LPAREN = new GoModTokenType("(");
  IElementType MODULE = new GoModTokenType("module");
  IElementType NEWLINE = new GoModTokenType("NEWLINE");
  IElementType REPLACE = new GoModTokenType("replace");
  IElementType REQUIRE = new GoModTokenType("require");
  IElementType RPAREN = new GoModTokenType(")");
  IElementType VERSION = new GoModTokenType("VERSION");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == EXCLUDE_BLOCK) {
        return new GoModExcludeBlockImpl(node);
      }
      else if (type == EXCLUDE_SPEC) {
        return new GoModExcludeSpecImpl(node);
      }
      else if (type == EXCLUDE_STATEMENT) {
        return new GoModExcludeStatementImpl(node);
      }
      else if (type == GO_STATEMENT) {
        return new GoModGoStatementImpl(node);
      }
      else if (type == ITEM) {
        return new GoModItemImpl(node);
      }
      else if (type == MODULE_STATEMENT) {
        return new GoModModuleStatementImpl(node);
      }
      else if (type == REPLACE_BLOCK) {
        return new GoModReplaceBlockImpl(node);
      }
      else if (type == REPLACE_SPEC) {
        return new GoModReplaceSpecImpl(node);
      }
      else if (type == REPLACE_STATEMENT) {
        return new GoModReplaceStatementImpl(node);
      }
      else if (type == REQUIRE_BLOCK) {
        return new GoModRequireBlockImpl(node);
      }
      else if (type == REQUIRE_SPEC) {
        return new GoModRequireSpecImpl(node);
      }
      else if (type == REQUIRE_STATEMENT) {
        return new GoModRequireStatementImpl(node);
      }
      else if (type == STATEMENT) {
        return new GoModStatementImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
