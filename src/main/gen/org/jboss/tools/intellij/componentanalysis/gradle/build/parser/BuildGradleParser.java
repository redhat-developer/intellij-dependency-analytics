// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.componentanalysis.gradle.build.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.jboss.tools.intellij.componentanalysis.gradle.build.psi.BuildGradleTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class BuildGradleParser implements PsiParser, LightPsiParser {

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
  // (CONFIG_NAME)? (string_notation | map_notation ) (comment)?
  public static boolean artifact(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ARTIFACT, "<artifact>");
    r = artifact_0(b, l + 1);
    r = r && artifact_1(b, l + 1);
    p = r; // pin = 2
    r = r && artifact_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (CONFIG_NAME)?
  private static boolean artifact_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact_0")) return false;
    consumeToken(b, CONFIG_NAME);
    return true;
  }

  // string_notation | map_notation
  private static boolean artifact_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact_1")) return false;
    boolean r;
    r = string_notation(b, l + 1);
    if (!r) r = map_notation(b, l + 1);
    return r;
  }

  // (comment)?
  private static boolean artifact_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact_2")) return false;
    artifact_2_0(b, l + 1);
    return true;
  }

  // (comment)
  private static boolean artifact_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = comment(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ARTIFACT_ID
  public static boolean artifact_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact_id")) return false;
    if (!nextTokenIs(b, ARTIFACT_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ARTIFACT_ID);
    exit_section_(b, m, ARTIFACT_ID, r);
    return r;
  }

  /* ********************************************************** */
  // NAME_KEY (SPACE_CHARACTER)* (APOSTROPHE|QUATATION_MARK) artifact_id (APOSTROPHE|QUATATION_MARK)
  static boolean artifact_map(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact_map")) return false;
    if (!nextTokenIs(b, NAME_KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NAME_KEY);
    r = r && artifact_map_1(b, l + 1);
    r = r && artifact_map_2(b, l + 1);
    r = r && artifact_id(b, l + 1);
    r = r && artifact_map_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SPACE_CHARACTER)*
  private static boolean artifact_map_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact_map_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "artifact_map_1", c)) break;
    }
    return true;
  }

  // APOSTROPHE|QUATATION_MARK
  private static boolean artifact_map_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact_map_2")) return false;
    boolean r;
    r = consumeToken(b, APOSTROPHE);
    if (!r) r = consumeToken(b, QUATATION_MARK);
    return r;
  }

  // APOSTROPHE|QUATATION_MARK
  private static boolean artifact_map_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "artifact_map_4")) return false;
    boolean r;
    r = consumeToken(b, APOSTROPHE);
    if (!r) r = consumeToken(b, QUATATION_MARK);
    return r;
  }

  /* ********************************************************** */
  // LINE_COMMENT|COMMENT
  public static boolean comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comment")) return false;
    if (!nextTokenIs(b, "<comment>", COMMENT, LINE_COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMMENT, "<comment>");
    r = consumeToken(b, LINE_COMMENT);
    if (!r) r = consumeToken(b, COMMENT);
    exit_section_(b, l, m, r, false, null);
    return r;
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
  // (APOSTROPHE|QUATATION_MARK)? group ":" artifact_id ":" version (APOSTROPHE|QUATATION_MARK)?
  static boolean gav_colon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_colon")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = gav_colon_0(b, l + 1);
    r = r && group(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && artifact_id(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && version(b, l + 1);
    r = r && gav_colon_6(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (APOSTROPHE|QUATATION_MARK)?
  private static boolean gav_colon_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_colon_0")) return false;
    gav_colon_0_0(b, l + 1);
    return true;
  }

  // APOSTROPHE|QUATATION_MARK
  private static boolean gav_colon_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_colon_0_0")) return false;
    boolean r;
    r = consumeToken(b, APOSTROPHE);
    if (!r) r = consumeToken(b, QUATATION_MARK);
    return r;
  }

  // (APOSTROPHE|QUATATION_MARK)?
  private static boolean gav_colon_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_colon_6")) return false;
    gav_colon_6_0(b, l + 1);
    return true;
  }

  // APOSTROPHE|QUATATION_MARK
  private static boolean gav_colon_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_colon_6_0")) return false;
    boolean r;
    r = consumeToken(b, APOSTROPHE);
    if (!r) r = consumeToken(b, QUATATION_MARK);
    return r;
  }

  /* ********************************************************** */
  // group_map (SPACE_CHARACTER)* COMMA (SPACE_CHARACTER)* artifact_map (SPACE_CHARACTER)* COMMA (SPACE_CHARACTER)* version_map
  static boolean gav_map(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_map")) return false;
    if (!nextTokenIs(b, GROUP_KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = group_map(b, l + 1);
    r = r && gav_map_1(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && gav_map_3(b, l + 1);
    r = r && artifact_map(b, l + 1);
    r = r && gav_map_5(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && gav_map_7(b, l + 1);
    r = r && version_map(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SPACE_CHARACTER)*
  private static boolean gav_map_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_map_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "gav_map_1", c)) break;
    }
    return true;
  }

  // (SPACE_CHARACTER)*
  private static boolean gav_map_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_map_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "gav_map_3", c)) break;
    }
    return true;
  }

  // (SPACE_CHARACTER)*
  private static boolean gav_map_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_map_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "gav_map_5", c)) break;
    }
    return true;
  }

  // (SPACE_CHARACTER)*
  private static boolean gav_map_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "gav_map_7")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "gav_map_7", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // GROUP_ID
  public static boolean group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group")) return false;
    if (!nextTokenIs(b, GROUP_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, GROUP_ID);
    exit_section_(b, m, GROUP, r);
    return r;
  }

  /* ********************************************************** */
  // GROUP_KEY (SPACE_CHARACTER)*  (APOSTROPHE|QUATATION_MARK) group (APOSTROPHE|QUATATION_MARK)
  static boolean group_map(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_map")) return false;
    if (!nextTokenIs(b, GROUP_KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, GROUP_KEY);
    r = r && group_map_1(b, l + 1);
    r = r && group_map_2(b, l + 1);
    r = r && group(b, l + 1);
    r = r && group_map_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SPACE_CHARACTER)*
  private static boolean group_map_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_map_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "group_map_1", c)) break;
    }
    return true;
  }

  // APOSTROPHE|QUATATION_MARK
  private static boolean group_map_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_map_2")) return false;
    boolean r;
    r = consumeToken(b, APOSTROPHE);
    if (!r) r = consumeToken(b, QUATATION_MARK);
    return r;
  }

  // APOSTROPHE|QUATATION_MARK
  private static boolean group_map_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "group_map_4")) return false;
    boolean r;
    r = consumeToken(b, APOSTROPHE);
    if (!r) r = consumeToken(b, QUATATION_MARK);
    return r;
  }

  /* ********************************************************** */
  // (JLINK_COMPONENT)+ ((LCURBRACE) (JLINK_COMPONENT)+ (RCURBRACE) (JLINK_COMPONENT)*)* (JLINK_COMPONENT)* (LCURBRACE|RCURBRACE)* CRLF
  static boolean jlink_components(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "jlink_components")) return false;
    if (!nextTokenIs(b, JLINK_COMPONENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = jlink_components_0(b, l + 1);
    r = r && jlink_components_1(b, l + 1);
    r = r && jlink_components_2(b, l + 1);
    r = r && jlink_components_3(b, l + 1);
    r = r && consumeToken(b, CRLF);
    exit_section_(b, m, null, r);
    return r;
  }

  // (JLINK_COMPONENT)+
  private static boolean jlink_components_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "jlink_components_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, JLINK_COMPONENT);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, JLINK_COMPONENT)) break;
      if (!empty_element_parsed_guard_(b, "jlink_components_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // ((LCURBRACE) (JLINK_COMPONENT)+ (RCURBRACE) (JLINK_COMPONENT)*)*
  private static boolean jlink_components_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "jlink_components_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!jlink_components_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "jlink_components_1", c)) break;
    }
    return true;
  }

  // (LCURBRACE) (JLINK_COMPONENT)+ (RCURBRACE) (JLINK_COMPONENT)*
  private static boolean jlink_components_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "jlink_components_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LCURBRACE);
    r = r && jlink_components_1_0_1(b, l + 1);
    r = r && consumeToken(b, RCURBRACE);
    r = r && jlink_components_1_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (JLINK_COMPONENT)+
  private static boolean jlink_components_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "jlink_components_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, JLINK_COMPONENT);
    while (r) {
      int c = current_position_(b);
      if (!consumeToken(b, JLINK_COMPONENT)) break;
      if (!empty_element_parsed_guard_(b, "jlink_components_1_0_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (JLINK_COMPONENT)*
  private static boolean jlink_components_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "jlink_components_1_0_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, JLINK_COMPONENT)) break;
      if (!empty_element_parsed_guard_(b, "jlink_components_1_0_3", c)) break;
    }
    return true;
  }

  // (JLINK_COMPONENT)*
  private static boolean jlink_components_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "jlink_components_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, JLINK_COMPONENT)) break;
      if (!empty_element_parsed_guard_(b, "jlink_components_2", c)) break;
    }
    return true;
  }

  // (LCURBRACE|RCURBRACE)*
  private static boolean jlink_components_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "jlink_components_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!jlink_components_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "jlink_components_3", c)) break;
    }
    return true;
  }

  // LCURBRACE|RCURBRACE
  private static boolean jlink_components_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "jlink_components_3_0")) return false;
    boolean r;
    r = consumeToken(b, LCURBRACE);
    if (!r) r = consumeToken(b, RCURBRACE);
    return r;
  }

  /* ********************************************************** */
  // artifact | comment | CRLF| SPACE_CHARACTER | others
  static boolean line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line")) return false;
    boolean r;
    r = artifact(b, l + 1);
    if (!r) r = comment(b, l + 1);
    if (!r) r = consumeToken(b, CRLF);
    if (!r) r = consumeToken(b, SPACE_CHARACTER);
    if (!r) r = others(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (SPACE_CHARACTER | LPARENTHESIS)? (SPACE_CHARACTER)? (COMMA|LCURBRACE)?  (gav_map)  (WHITE_SPACE | RPARENTHESIS)?
  static boolean map_notation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_notation")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = map_notation_0(b, l + 1);
    r = r && map_notation_1(b, l + 1);
    r = r && map_notation_2(b, l + 1);
    r = r && map_notation_3(b, l + 1);
    p = r; // pin = 4
    r = r && map_notation_4(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (SPACE_CHARACTER | LPARENTHESIS)?
  private static boolean map_notation_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_notation_0")) return false;
    map_notation_0_0(b, l + 1);
    return true;
  }

  // SPACE_CHARACTER | LPARENTHESIS
  private static boolean map_notation_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_notation_0_0")) return false;
    boolean r;
    r = consumeToken(b, SPACE_CHARACTER);
    if (!r) r = consumeToken(b, LPARENTHESIS);
    return r;
  }

  // (SPACE_CHARACTER)?
  private static boolean map_notation_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_notation_1")) return false;
    consumeToken(b, SPACE_CHARACTER);
    return true;
  }

  // (COMMA|LCURBRACE)?
  private static boolean map_notation_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_notation_2")) return false;
    map_notation_2_0(b, l + 1);
    return true;
  }

  // COMMA|LCURBRACE
  private static boolean map_notation_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_notation_2_0")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, LCURBRACE);
    return r;
  }

  // (gav_map)
  private static boolean map_notation_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_notation_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = gav_map(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (WHITE_SPACE | RPARENTHESIS)?
  private static boolean map_notation_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_notation_4")) return false;
    map_notation_4_0(b, l + 1);
    return true;
  }

  // WHITE_SPACE | RPARENTHESIS
  private static boolean map_notation_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "map_notation_4_0")) return false;
    boolean r;
    r = consumeToken(b, WHITE_SPACE);
    if (!r) r = consumeToken(b, RPARENTHESIS);
    return r;
  }

  /* ********************************************************** */
  // PLUGINS | root_version | root_group  | root_generic_key | LCURBRACE | RCURBRACE | DEPENDENCIES | REPOSITORIES | ID_PREFIX | TEST | SOURCE_SETS | MAIN | JAVA | EXT | APPLICATION | JAVAFX | JLINK_START | jlink_components | JLINKZIP | INTELLIJ | RUNIDE | TASKS
  static boolean others(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "others")) return false;
    boolean r;
    r = consumeToken(b, PLUGINS);
    if (!r) r = root_version(b, l + 1);
    if (!r) r = root_group(b, l + 1);
    if (!r) r = root_generic_key(b, l + 1);
    if (!r) r = consumeToken(b, LCURBRACE);
    if (!r) r = consumeToken(b, RCURBRACE);
    if (!r) r = consumeToken(b, DEPENDENCIES);
    if (!r) r = consumeToken(b, REPOSITORIES);
    if (!r) r = consumeToken(b, ID_PREFIX);
    if (!r) r = consumeToken(b, TEST);
    if (!r) r = consumeToken(b, SOURCE_SETS);
    if (!r) r = consumeToken(b, MAIN);
    if (!r) r = consumeToken(b, JAVA);
    if (!r) r = consumeToken(b, EXT);
    if (!r) r = consumeToken(b, APPLICATION);
    if (!r) r = consumeToken(b, JAVAFX);
    if (!r) r = consumeToken(b, JLINK_START);
    if (!r) r = jlink_components(b, l + 1);
    if (!r) r = consumeToken(b, JLINKZIP);
    if (!r) r = consumeToken(b, INTELLIJ);
    if (!r) r = consumeToken(b, RUNIDE);
    if (!r) r = consumeToken(b, TASKS);
    return r;
  }

  /* ********************************************************** */
  // ROOT_GENERIC_KEY (SPACE_CHARACTER* EQUALS SPACE_CHARACTER*|SPACE_CHARACTER) ROOT_GENERIC_VALUE
  static boolean root_generic_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_generic_key")) return false;
    if (!nextTokenIs(b, ROOT_GENERIC_KEY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ROOT_GENERIC_KEY);
    p = r; // pin = 1
    r = r && report_error_(b, root_generic_key_1(b, l + 1));
    r = p && consumeToken(b, ROOT_GENERIC_VALUE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // SPACE_CHARACTER* EQUALS SPACE_CHARACTER*|SPACE_CHARACTER
  private static boolean root_generic_key_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_generic_key_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_generic_key_1_0(b, l + 1);
    if (!r) r = consumeToken(b, SPACE_CHARACTER);
    exit_section_(b, m, null, r);
    return r;
  }

  // SPACE_CHARACTER* EQUALS SPACE_CHARACTER*
  private static boolean root_generic_key_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_generic_key_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_generic_key_1_0_0(b, l + 1);
    r = r && consumeToken(b, EQUALS);
    r = r && root_generic_key_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SPACE_CHARACTER*
  private static boolean root_generic_key_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_generic_key_1_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "root_generic_key_1_0_0", c)) break;
    }
    return true;
  }

  // SPACE_CHARACTER*
  private static boolean root_generic_key_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_generic_key_1_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "root_generic_key_1_0_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ROOT_GROUP_KEY (SPACE_CHARACTER* EQUALS SPACE_CHARACTER*|SPACE_CHARACTER) ROOT_GROUP_VERSION_VALUE
  static boolean root_group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_group")) return false;
    if (!nextTokenIs(b, ROOT_GROUP_KEY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ROOT_GROUP_KEY);
    p = r; // pin = 1
    r = r && report_error_(b, root_group_1(b, l + 1));
    r = p && consumeToken(b, ROOT_GROUP_VERSION_VALUE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // SPACE_CHARACTER* EQUALS SPACE_CHARACTER*|SPACE_CHARACTER
  private static boolean root_group_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_group_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_group_1_0(b, l + 1);
    if (!r) r = consumeToken(b, SPACE_CHARACTER);
    exit_section_(b, m, null, r);
    return r;
  }

  // SPACE_CHARACTER* EQUALS SPACE_CHARACTER*
  private static boolean root_group_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_group_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_group_1_0_0(b, l + 1);
    r = r && consumeToken(b, EQUALS);
    r = r && root_group_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SPACE_CHARACTER*
  private static boolean root_group_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_group_1_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "root_group_1_0_0", c)) break;
    }
    return true;
  }

  // SPACE_CHARACTER*
  private static boolean root_group_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_group_1_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "root_group_1_0_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ROOT_VERSION_KEY (SPACE_CHARACTER* EQUALS SPACE_CHARACTER*|SPACE_CHARACTER) ROOT_GROUP_VERSION_VALUE
  static boolean root_version(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_version")) return false;
    if (!nextTokenIs(b, ROOT_VERSION_KEY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ROOT_VERSION_KEY);
    p = r; // pin = 1
    r = r && report_error_(b, root_version_1(b, l + 1));
    r = p && consumeToken(b, ROOT_GROUP_VERSION_VALUE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // SPACE_CHARACTER* EQUALS SPACE_CHARACTER*|SPACE_CHARACTER
  private static boolean root_version_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_version_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_version_1_0(b, l + 1);
    if (!r) r = consumeToken(b, SPACE_CHARACTER);
    exit_section_(b, m, null, r);
    return r;
  }

  // SPACE_CHARACTER* EQUALS SPACE_CHARACTER*
  private static boolean root_version_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_version_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_version_1_0_0(b, l + 1);
    r = r && consumeToken(b, EQUALS);
    r = r && root_version_1_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SPACE_CHARACTER*
  private static boolean root_version_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_version_1_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "root_version_1_0_0", c)) break;
    }
    return true;
  }

  // SPACE_CHARACTER*
  private static boolean root_version_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_version_1_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "root_version_1_0_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (SPACE_CHARACTER | LPARENTHESIS)? (gav_colon)
  static boolean string_notation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_notation")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = string_notation_0(b, l + 1);
    r = r && string_notation_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SPACE_CHARACTER | LPARENTHESIS)?
  private static boolean string_notation_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_notation_0")) return false;
    string_notation_0_0(b, l + 1);
    return true;
  }

  // SPACE_CHARACTER | LPARENTHESIS
  private static boolean string_notation_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_notation_0_0")) return false;
    boolean r;
    r = consumeToken(b, SPACE_CHARACTER);
    if (!r) r = consumeToken(b, LPARENTHESIS);
    return r;
  }

  // (gav_colon)
  private static boolean string_notation_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_notation_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = gav_colon(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // VERSION
  public static boolean version(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version")) return false;
    if (!nextTokenIs(b, VERSION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VERSION);
    exit_section_(b, m, VERSION, r);
    return r;
  }

  /* ********************************************************** */
  // VERSION_KEY (SPACE_CHARACTER)* (APOSTROPHE|QUATATION_MARK) version (APOSTROPHE|QUATATION_MARK)
  static boolean version_map(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_map")) return false;
    if (!nextTokenIs(b, VERSION_KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VERSION_KEY);
    r = r && version_map_1(b, l + 1);
    r = r && version_map_2(b, l + 1);
    r = r && version(b, l + 1);
    r = r && version_map_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SPACE_CHARACTER)*
  private static boolean version_map_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_map_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, SPACE_CHARACTER)) break;
      if (!empty_element_parsed_guard_(b, "version_map_1", c)) break;
    }
    return true;
  }

  // APOSTROPHE|QUATATION_MARK
  private static boolean version_map_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_map_2")) return false;
    boolean r;
    r = consumeToken(b, APOSTROPHE);
    if (!r) r = consumeToken(b, QUATATION_MARK);
    return r;
  }

  // APOSTROPHE|QUATATION_MARK
  private static boolean version_map_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "version_map_4")) return false;
    boolean r;
    r = consumeToken(b, APOSTROPHE);
    if (!r) r = consumeToken(b, QUATATION_MARK);
    return r;
  }

}
