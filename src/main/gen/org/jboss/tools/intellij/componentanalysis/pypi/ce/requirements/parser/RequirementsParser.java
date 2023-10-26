// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import static org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.RequirementsTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class RequirementsParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return file(b, l + 1);
  }

  /* ********************************************************** */
  // LSBRACE extras_list? RSBRACE
  static boolean extras(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extras")) return false;
    if (!nextTokenIs(b, LSBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LSBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, extras_1(b, l + 1));
    r = p && consumeToken(b, RSBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // extras_list?
  private static boolean extras_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extras_1")) return false;
    extras_list(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER (COMMA IDENTIFIER)*
  public static boolean extras_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extras_list")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXTRAS_LIST, null);
    r = consumeToken(b, IDENTIFIER);
    p = r; // pin = 1
    r = r && extras_list_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMA IDENTIFIER)*
  private static boolean extras_list_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extras_list_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!extras_list_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "extras_list_1", c)) break;
    }
    return true;
  }

  // COMMA IDENTIFIER
  private static boolean extras_list_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extras_list_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, COMMA, IDENTIFIER);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // line*
  static boolean file(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file")) return false;
    while (true) {
      int c = current_position_(b);
      if (!line(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "file", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // spec | COMMENT | CRLF
  static boolean line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line")) return false;
    boolean r;
    r = spec(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, CRLF);
    return r;
  }

  /* ********************************************************** */
  // pkg_name extras? (versionspec | AT)? (REQ_PART | BACKSLASH)* name_req_comment?
  public static boolean name_req(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name_req")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = pkg_name(b, l + 1);
    r = r && name_req_1(b, l + 1);
    r = r && name_req_2(b, l + 1);
    r = r && name_req_3(b, l + 1);
    r = r && name_req_4(b, l + 1);
    exit_section_(b, m, NAME_REQ, r);
    return r;
  }

  // extras?
  private static boolean name_req_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name_req_1")) return false;
    extras(b, l + 1);
    return true;
  }

  // (versionspec | AT)?
  private static boolean name_req_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name_req_2")) return false;
    name_req_2_0(b, l + 1);
    return true;
  }

  // versionspec | AT
  private static boolean name_req_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name_req_2_0")) return false;
    boolean r;
    r = versionspec(b, l + 1);
    if (!r) r = consumeToken(b, AT);
    return r;
  }

  // (REQ_PART | BACKSLASH)*
  private static boolean name_req_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name_req_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!name_req_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "name_req_3", c)) break;
    }
    return true;
  }

  // REQ_PART | BACKSLASH
  private static boolean name_req_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name_req_3_0")) return false;
    boolean r;
    r = consumeToken(b, REQ_PART);
    if (!r) r = consumeToken(b, BACKSLASH);
    return r;
  }

  // name_req_comment?
  private static boolean name_req_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name_req_4")) return false;
    name_req_comment(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // REQ_COMMENT
  public static boolean name_req_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "name_req_comment")) return false;
    if (!nextTokenIs(b, REQ_COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REQ_COMMENT);
    exit_section_(b, m, NAME_REQ_COMMENT, r);
    return r;
  }

  /* ********************************************************** */
  // OTHER_PART (OTHER_PART | BACKSLASH)* COMMENT?
  public static boolean other_spec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "other_spec")) return false;
    if (!nextTokenIs(b, OTHER_PART)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OTHER_SPEC, null);
    r = consumeToken(b, OTHER_PART);
    p = r; // pin = 1
    r = r && report_error_(b, other_spec_1(b, l + 1));
    r = p && other_spec_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (OTHER_PART | BACKSLASH)*
  private static boolean other_spec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "other_spec_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!other_spec_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "other_spec_1", c)) break;
    }
    return true;
  }

  // OTHER_PART | BACKSLASH
  private static boolean other_spec_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "other_spec_1_0")) return false;
    boolean r;
    r = consumeToken(b, OTHER_PART);
    if (!r) r = consumeToken(b, BACKSLASH);
    return r;
  }

  // COMMENT?
  private static boolean other_spec_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "other_spec_2")) return false;
    consumeToken(b, COMMENT);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean pkg_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pkg_name")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, PKG_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // name_req | uri_req | other_spec
  static boolean spec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "spec")) return false;
    boolean r;
    r = name_req(b, l + 1);
    if (!r) r = uri_req(b, l + 1);
    if (!r) r = other_spec(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (pkg_name extras?)? URI_PART (URI_PART | BACKSLASH)* COMMENT?
  public static boolean uri_req(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uri_req")) return false;
    if (!nextTokenIs(b, "<uri req>", IDENTIFIER, URI_PART)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, URI_REQ, "<uri req>");
    r = uri_req_0(b, l + 1);
    r = r && consumeToken(b, URI_PART);
    p = r; // pin = 2
    r = r && report_error_(b, uri_req_2(b, l + 1));
    r = p && uri_req_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (pkg_name extras?)?
  private static boolean uri_req_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uri_req_0")) return false;
    uri_req_0_0(b, l + 1);
    return true;
  }

  // pkg_name extras?
  private static boolean uri_req_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uri_req_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = pkg_name(b, l + 1);
    r = r && uri_req_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // extras?
  private static boolean uri_req_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uri_req_0_0_1")) return false;
    extras(b, l + 1);
    return true;
  }

  // (URI_PART | BACKSLASH)*
  private static boolean uri_req_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uri_req_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!uri_req_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "uri_req_2", c)) break;
    }
    return true;
  }

  // URI_PART | BACKSLASH
  private static boolean uri_req_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uri_req_2_0")) return false;
    boolean r;
    r = consumeToken(b, URI_PART);
    if (!r) r = consumeToken(b, BACKSLASH);
    return r;
  }

  // COMMENT?
  private static boolean uri_req_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "uri_req_3")) return false;
    consumeToken(b, COMMENT);
    return true;
  }

  /* ********************************************************** */
  // VERSION_CMP
  public static boolean version_cmp_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_cmp_value")) return false;
    if (!nextTokenIs(b, VERSION_CMP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VERSION_CMP);
    exit_section_(b, m, VERSION_CMP_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // version_one (COMMA version_one)*
  static boolean version_many(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_many")) return false;
    if (!nextTokenIs(b, VERSION_CMP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = version_one(b, l + 1);
    r = r && version_many_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA version_one)*
  private static boolean version_many_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_many_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!version_many_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "version_many_1", c)) break;
    }
    return true;
  }

  // COMMA version_one
  private static boolean version_many_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_many_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && version_one(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // version_cmp_value version_value
  public static boolean version_one(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_one")) return false;
    if (!nextTokenIs(b, VERSION_CMP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = version_cmp_value(b, l + 1);
    r = r && version_value(b, l + 1);
    exit_section_(b, m, VERSION_ONE, r);
    return r;
  }

  /* ********************************************************** */
  // VERSION
  public static boolean version_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_value")) return false;
    if (!nextTokenIs(b, VERSION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VERSION);
    exit_section_(b, m, VERSION_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // LPARENTHESIS version_many RPARENTHESIS | version_many
  public static boolean versionspec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "versionspec")) return false;
    if (!nextTokenIs(b, "<versionspec>", LPARENTHESIS, VERSION_CMP)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VERSIONSPEC, "<versionspec>");
    r = versionspec_0(b, l + 1);
    if (!r) r = version_many(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LPARENTHESIS version_many RPARENTHESIS
  private static boolean versionspec_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "versionspec_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPARENTHESIS);
    r = r && version_many(b, l + 1);
    r = r && consumeToken(b, RPARENTHESIS);
    exit_section_(b, m, null, r);
    return r;
  }

}
