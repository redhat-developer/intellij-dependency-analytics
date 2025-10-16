// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.image.build.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.jboss.tools.intellij.image.build.psi.DockerfileTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class DockerfileParser implements PsiParser, LightPsiParser {

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
    return dockerFile(b, l + 1);
  }

  /* ********************************************************** */
  // IDENTIFIER (EQUALS argValue)?
  public static boolean argDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argDeclaration")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    r = r && argDeclaration_1(b, l + 1);
    exit_section_(b, m, ARG_DECLARATION, r);
    return r;
  }

  // (EQUALS argValue)?
  private static boolean argDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argDeclaration_1")) return false;
    argDeclaration_1_0(b, l + 1);
    return true;
  }

  // EQUALS argValue
  private static boolean argDeclaration_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argDeclaration_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQUALS);
    r = r && argValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ARG argDeclaration
  public static boolean argInstruction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argInstruction")) return false;
    if (!nextTokenIs(b, ARG)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ARG);
    r = r && argDeclaration(b, l + 1);
    exit_section_(b, m, ARG_INSTRUCTION, r);
    return r;
  }

  /* ********************************************************** */
  // STRING | IDENTIFIER | VERSION | variableRef
  public static boolean argValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARG_VALUE, "<arg value>");
    r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, VERSION);
    if (!r) r = variableRef(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AS IDENTIFIER
  public static boolean asClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "asClause")) return false;
    if (!nextTokenIs(b, AS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AS, IDENTIFIER);
    exit_section_(b, m, AS_CLAUSE, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACE IDENTIFIER RBRACE
  public static boolean complexVar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "complexVar")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACE, IDENTIFIER, RBRACE);
    exit_section_(b, m, COMPLEX_VAR, r);
    return r;
  }

  /* ********************************************************** */
  // item*
  static boolean dockerFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dockerFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "dockerFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // FROM (platformOption)? imageName (asClause)?
  public static boolean fromInstruction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromInstruction")) return false;
    if (!nextTokenIs(b, FROM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FROM);
    r = r && fromInstruction_1(b, l + 1);
    r = r && imageName(b, l + 1);
    r = r && fromInstruction_3(b, l + 1);
    exit_section_(b, m, FROM_INSTRUCTION, r);
    return r;
  }

  // (platformOption)?
  private static boolean fromInstruction_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromInstruction_1")) return false;
    fromInstruction_1_0(b, l + 1);
    return true;
  }

  // (platformOption)
  private static boolean fromInstruction_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromInstruction_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = platformOption(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (asClause)?
  private static boolean fromInstruction_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromInstruction_3")) return false;
    fromInstruction_3_0(b, l + 1);
    return true;
  }

  // (asClause)
  private static boolean fromInstruction_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fromInstruction_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = asClause(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // imageNamePart+
  public static boolean imageName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "imageName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IMAGE_NAME, "<image name>");
    r = imageNamePart(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!imageNamePart(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "imageName", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IMAGE_NAME_TOKEN | IDENTIFIER
  public static boolean imageNameLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "imageNameLiteral")) return false;
    if (!nextTokenIs(b, "<image name literal>", IDENTIFIER, IMAGE_NAME_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IMAGE_NAME_LITERAL, "<image name literal>");
    r = consumeToken(b, IMAGE_NAME_TOKEN);
    if (!r) r = consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // imageNameLiteral | variableRef | COLON | VERSION
  public static boolean imageNamePart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "imageNamePart")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IMAGE_NAME_PART, "<image name part>");
    r = imageNameLiteral(b, l + 1);
    if (!r) r = variableRef(b, l + 1);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, VERSION);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // fromInstruction | argInstruction | otherInstruction
  public static boolean instruction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INSTRUCTION, "<instruction>");
    r = fromInstruction(b, l + 1);
    if (!r) r = argInstruction(b, l + 1);
    if (!r) r = otherInstruction(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // STRING | IDENTIFIER | VERSION | IMAGE_NAME_TOKEN | OTHER_TOKEN | COLON | EQUALS | DOLLAR | LBRACE | RBRACE | PLATFORM_FLAG | PLATFORM | ANY_CHAR | variableRef
  public static boolean instructionArgs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instructionArgs")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INSTRUCTION_ARGS, "<instruction args>");
    r = consumeToken(b, STRING);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, VERSION);
    if (!r) r = consumeToken(b, IMAGE_NAME_TOKEN);
    if (!r) r = consumeToken(b, OTHER_TOKEN);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, EQUALS);
    if (!r) r = consumeToken(b, DOLLAR);
    if (!r) r = consumeToken(b, LBRACE);
    if (!r) r = consumeToken(b, RBRACE);
    if (!r) r = consumeToken(b, PLATFORM_FLAG);
    if (!r) r = consumeToken(b, PLATFORM);
    if (!r) r = consumeToken(b, ANY_CHAR);
    if (!r) r = variableRef(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // instruction | COMMENT | NEWLINE
  public static boolean item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ITEM, "<item>");
    r = instruction(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, NEWLINE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (RUN | COPY | ADD | WORKDIR | CMD | ENTRYPOINT | ENV | EXPOSE | VOLUME | USER | LABEL) instructionArgs*
  public static boolean otherInstruction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "otherInstruction")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OTHER_INSTRUCTION, "<other instruction>");
    r = otherInstruction_0(b, l + 1);
    r = r && otherInstruction_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // RUN | COPY | ADD | WORKDIR | CMD | ENTRYPOINT | ENV | EXPOSE | VOLUME | USER | LABEL
  private static boolean otherInstruction_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "otherInstruction_0")) return false;
    boolean r;
    r = consumeToken(b, RUN);
    if (!r) r = consumeToken(b, COPY);
    if (!r) r = consumeToken(b, ADD);
    if (!r) r = consumeToken(b, WORKDIR);
    if (!r) r = consumeToken(b, CMD);
    if (!r) r = consumeToken(b, ENTRYPOINT);
    if (!r) r = consumeToken(b, ENV);
    if (!r) r = consumeToken(b, EXPOSE);
    if (!r) r = consumeToken(b, VOLUME);
    if (!r) r = consumeToken(b, USER);
    if (!r) r = consumeToken(b, LABEL);
    return r;
  }

  // instructionArgs*
  private static boolean otherInstruction_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "otherInstruction_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!instructionArgs(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "otherInstruction_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // PLATFORM_FLAG EQUALS platformValue
  public static boolean platformOption(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "platformOption")) return false;
    if (!nextTokenIs(b, PLATFORM_FLAG)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PLATFORM_FLAG, EQUALS);
    r = r && platformValue(b, l + 1);
    exit_section_(b, m, PLATFORM_OPTION, r);
    return r;
  }

  /* ********************************************************** */
  // PLATFORM | variableRef
  public static boolean platformValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "platformValue")) return false;
    if (!nextTokenIs(b, "<platform value>", DOLLAR, PLATFORM)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PLATFORM_VALUE, "<platform value>");
    r = consumeToken(b, PLATFORM);
    if (!r) r = variableRef(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean simpleVar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleVar")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, SIMPLE_VAR, r);
    return r;
  }

  /* ********************************************************** */
  // DOLLAR (simpleVar | complexVar)
  public static boolean variableRef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableRef")) return false;
    if (!nextTokenIs(b, DOLLAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOLLAR);
    r = r && variableRef_1(b, l + 1);
    exit_section_(b, m, VARIABLE_REF, r);
    return r;
  }

  // simpleVar | complexVar
  private static boolean variableRef_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variableRef_1")) return false;
    boolean r;
    r = simpleVar(b, l + 1);
    if (!r) r = complexVar(b, l + 1);
    return r;
  }

}
