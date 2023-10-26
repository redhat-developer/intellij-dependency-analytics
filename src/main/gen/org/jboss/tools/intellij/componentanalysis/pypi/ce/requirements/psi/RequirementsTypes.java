// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.impl.*;

public interface RequirementsTypes {

  IElementType EXTRAS_LIST = new RequirementsElementType("EXTRAS_LIST");
  IElementType NAME_REQ = new RequirementsElementType("NAME_REQ");
  IElementType NAME_REQ_COMMENT = new RequirementsElementType("NAME_REQ_COMMENT");
  IElementType OTHER_SPEC = new RequirementsElementType("OTHER_SPEC");
  IElementType PKG_NAME = new RequirementsElementType("PKG_NAME");
  IElementType URI_REQ = new RequirementsElementType("URI_REQ");
  IElementType VERSIONSPEC = new RequirementsElementType("VERSIONSPEC");
  IElementType VERSION_CMP_VALUE = new RequirementsElementType("VERSION_CMP_VALUE");
  IElementType VERSION_ONE = new RequirementsElementType("VERSION_ONE");
  IElementType VERSION_VALUE = new RequirementsElementType("VERSION_VALUE");

  IElementType AT = new RequirementsTokenType("AT");
  IElementType BACKSLASH = new RequirementsTokenType("BACKSLASH");
  IElementType COMMA = new RequirementsTokenType("COMMA");
  IElementType COMMENT = new RequirementsTokenType("COMMENT");
  IElementType CRLF = new RequirementsTokenType("CRLF");
  IElementType IDENTIFIER = new RequirementsTokenType("IDENTIFIER");
  IElementType LPARENTHESIS = new RequirementsTokenType("LPARENTHESIS");
  IElementType LSBRACE = new RequirementsTokenType("LSBRACE");
  IElementType OTHER_PART = new RequirementsTokenType("OTHER_PART");
  IElementType REQ_COMMENT = new RequirementsTokenType("REQ_COMMENT");
  IElementType REQ_PART = new RequirementsTokenType("REQ_PART");
  IElementType RPARENTHESIS = new RequirementsTokenType("RPARENTHESIS");
  IElementType RSBRACE = new RequirementsTokenType("RSBRACE");
  IElementType URI_PART = new RequirementsTokenType("URI_PART");
  IElementType VERSION = new RequirementsTokenType("VERSION");
  IElementType VERSION_CMP = new RequirementsTokenType("VERSION_CMP");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == EXTRAS_LIST) {
        return new ExtrasListImpl(node);
      }
      else if (type == NAME_REQ) {
        return new NameReqImpl(node);
      }
      else if (type == NAME_REQ_COMMENT) {
        return new NameReqCommentImpl(node);
      }
      else if (type == OTHER_SPEC) {
        return new OtherSpecImpl(node);
      }
      else if (type == PKG_NAME) {
        return new PkgNameImpl(node);
      }
      else if (type == URI_REQ) {
        return new UriReqImpl(node);
      }
      else if (type == VERSIONSPEC) {
        return new VersionspecImpl(node);
      }
      else if (type == VERSION_CMP_VALUE) {
        return new VersionCmpValueImpl(node);
      }
      else if (type == VERSION_ONE) {
        return new VersionOneImpl(node);
      }
      else if (type == VERSION_VALUE) {
        return new VersionValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
