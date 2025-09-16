// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.golang.build.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.jboss.tools.intellij.componentanalysis.golang.build.psi.GoModTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class GoModParser implements PsiParser, LightPsiParser {

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
    return goModFile(b, l + 1);
  }

  /* ********************************************************** */
  // LPAREN (excludeSpec | COMMENT | NEWLINE)* RPAREN
  public static boolean excludeBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "excludeBlock")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && excludeBlock_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, EXCLUDE_BLOCK, r);
    return r;
  }

  // (excludeSpec | COMMENT | NEWLINE)*
  private static boolean excludeBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "excludeBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!excludeBlock_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "excludeBlock_1", c)) break;
    }
    return true;
  }

  // excludeSpec | COMMENT | NEWLINE
  private static boolean excludeBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "excludeBlock_1_0")) return false;
    boolean r;
    r = excludeSpec(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, NEWLINE);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER VERSION? (COMMENT)*
  public static boolean excludeSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "excludeSpec")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && excludeSpec_1(b, l + 1);
    r = r && excludeSpec_2(b, l + 1);
    exit_section_(b, m, EXCLUDE_SPEC, r);
    return r;
  }

  // VERSION?
  private static boolean excludeSpec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "excludeSpec_1")) return false;
    consumeToken(b, VERSION);
    return true;
  }

  // (COMMENT)*
  private static boolean excludeSpec_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "excludeSpec_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "excludeSpec_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // EXCLUDE (excludeSpec | excludeBlock)
  public static boolean excludeStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "excludeStatement")) return false;
    if (!nextTokenIs(b, EXCLUDE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXCLUDE);
    r = r && excludeStatement_1(b, l + 1);
    exit_section_(b, m, EXCLUDE_STATEMENT, r);
    return r;
  }

  // excludeSpec | excludeBlock
  private static boolean excludeStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "excludeStatement_1")) return false;
    boolean r;
    r = excludeSpec(b, l + 1);
    if (!r) r = excludeBlock(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // item*
  static boolean goModFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "goModFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "goModFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // GO VERSION
  public static boolean goStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "goStatement")) return false;
    if (!nextTokenIs(b, GO)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GO, VERSION);
    exit_section_(b, m, GO_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // statement | COMMENT | NEWLINE
  public static boolean item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ITEM, "<item>");
    r = statement(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, NEWLINE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // MODULE IDENTIFIER
  public static boolean moduleStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "moduleStatement")) return false;
    if (!nextTokenIs(b, MODULE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, MODULE, IDENTIFIER);
    exit_section_(b, m, MODULE_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // LPAREN (replaceSpec | COMMENT | NEWLINE)* RPAREN
  public static boolean replaceBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replaceBlock")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && replaceBlock_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, REPLACE_BLOCK, r);
    return r;
  }

  // (replaceSpec | COMMENT | NEWLINE)*
  private static boolean replaceBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replaceBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!replaceBlock_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "replaceBlock_1", c)) break;
    }
    return true;
  }

  // replaceSpec | COMMENT | NEWLINE
  private static boolean replaceBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replaceBlock_1_0")) return false;
    boolean r;
    r = replaceSpec(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, NEWLINE);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER VERSION? ARROW IDENTIFIER VERSION? (COMMENT)*
  public static boolean replaceSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replaceSpec")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && replaceSpec_1(b, l + 1);
    r = r && consumeTokens(b, 0, ARROW, IDENTIFIER);
    r = r && replaceSpec_4(b, l + 1);
    r = r && replaceSpec_5(b, l + 1);
    exit_section_(b, m, REPLACE_SPEC, r);
    return r;
  }

  // VERSION?
  private static boolean replaceSpec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replaceSpec_1")) return false;
    consumeToken(b, VERSION);
    return true;
  }

  // VERSION?
  private static boolean replaceSpec_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replaceSpec_4")) return false;
    consumeToken(b, VERSION);
    return true;
  }

  // (COMMENT)*
  private static boolean replaceSpec_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replaceSpec_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "replaceSpec_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // REPLACE (replaceSpec | replaceBlock)
  public static boolean replaceStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replaceStatement")) return false;
    if (!nextTokenIs(b, REPLACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REPLACE);
    r = r && replaceStatement_1(b, l + 1);
    exit_section_(b, m, REPLACE_STATEMENT, r);
    return r;
  }

  // replaceSpec | replaceBlock
  private static boolean replaceStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "replaceStatement_1")) return false;
    boolean r;
    r = replaceSpec(b, l + 1);
    if (!r) r = replaceBlock(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // LPAREN (requireSpec | COMMENT | NEWLINE)* RPAREN
  public static boolean requireBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requireBlock")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && requireBlock_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, REQUIRE_BLOCK, r);
    return r;
  }

  // (requireSpec | COMMENT | NEWLINE)*
  private static boolean requireBlock_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requireBlock_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!requireBlock_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "requireBlock_1", c)) break;
    }
    return true;
  }

  // requireSpec | COMMENT | NEWLINE
  private static boolean requireBlock_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requireBlock_1_0")) return false;
    boolean r;
    r = requireSpec(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, NEWLINE);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER VERSION (INDIRECT | COMMENT)*
  public static boolean requireSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requireSpec")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, IDENTIFIER, VERSION);
    r = r && requireSpec_2(b, l + 1);
    exit_section_(b, m, REQUIRE_SPEC, r);
    return r;
  }

  // (INDIRECT | COMMENT)*
  private static boolean requireSpec_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requireSpec_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!requireSpec_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "requireSpec_2", c)) break;
    }
    return true;
  }

  // INDIRECT | COMMENT
  private static boolean requireSpec_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requireSpec_2_0")) return false;
    boolean r;
    r = consumeToken(b, INDIRECT);
    if (!r) r = consumeToken(b, COMMENT);
    return r;
  }

  /* ********************************************************** */
  // REQUIRE (requireSpec | requireBlock)
  public static boolean requireStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requireStatement")) return false;
    if (!nextTokenIs(b, REQUIRE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REQUIRE);
    r = r && requireStatement_1(b, l + 1);
    exit_section_(b, m, REQUIRE_STATEMENT, r);
    return r;
  }

  // requireSpec | requireBlock
  private static boolean requireStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "requireStatement_1")) return false;
    boolean r;
    r = requireSpec(b, l + 1);
    if (!r) r = requireBlock(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // moduleStatement | goStatement | requireStatement | replaceStatement | excludeStatement
  public static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = moduleStatement(b, l + 1);
    if (!r) r = goStatement(b, l + 1);
    if (!r) r = requireStatement(b, l + 1);
    if (!r) r = replaceStatement(b, l + 1);
    if (!r) r = excludeStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
