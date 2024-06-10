// Generated by JFlex 1.9.1 http://jflex.de/  (tweaked for IntelliJ platform)
// source: buildGradle.flex

package org.jboss.tools.intellij.componentanalysis.gradle.build.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static org.jboss.tools.intellij.componentanalysis.gradle.build.psi.BuildGradleTypes.*;


public class BuildGradleLexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int STRING_DEPENDENCY = 2;
  public static final int HANDLE_DEPENDENCIES = 4;
  public static final int MAP_DEPENDENCY = 6;
  public static final int HANDLE_GROUP = 8;
  public static final int HANDLE_ARTIFACT = 10;
  public static final int HANDLE_VERSION = 12;
  public static final int EXTRACT_GROUP_STRING = 14;
  public static final int EXTRACT_ARTIFACT_STRING = 16;
  public static final int EXTRACT_VERSION_STRING = 18;
  public static final int GET_ROOT_VALUES = 20;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5,  5,  6,  6,  7,  7, 
     8,  8,  9,  9, 10, 10
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\25\u0100\1\u0200\11\u0100\1\u0300\17\u0100\1\u0400\247\u0100"+
    "\10\u0500\u1020\u0100";

  private static int [] zzUnpackcmap_top() {
    int [] result = new int[4352];
    int offset = 0;
    offset = zzUnpackcmap_top(ZZ_CMAP_TOP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_top(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Second-level tables for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_BLOCKS = zzUnpackcmap_blocks();

  private static final String ZZ_CMAP_BLOCKS_PACKED_0 =
    "\11\0\1\1\1\2\2\3\1\2\22\0\1\1\1\0"+
    "\1\4\4\0\1\5\1\6\1\7\2\0\1\10\1\11"+
    "\1\12\1\13\12\14\1\15\2\0\1\16\3\0\2\11"+
    "\1\17\13\11\1\20\3\11\1\21\7\11\1\22\1\23"+
    "\1\24\1\0\1\25\1\0\1\26\1\11\1\27\1\30"+
    "\1\31\1\11\1\32\1\33\1\34\1\35\1\11\1\36"+
    "\1\37\1\40\1\41\1\42\1\11\1\43\1\44\1\45"+
    "\1\46\1\47\2\11\1\50\1\11\1\51\1\52\1\53"+
    "\7\0\1\3\32\0\1\1\u01df\0\1\1\177\0\13\1"+
    "\35\0\2\3\5\0\1\1\57\0\1\1\240\0\1\1"+
    "\377\0\u0100\54";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[1536];
    int offset = 0;
    offset = zzUnpackcmap_blocks(ZZ_CMAP_BLOCKS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_blocks(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /**
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\13\0\1\1\1\2\1\3\1\4\1\5\11\1\1\6"+
    "\1\7\1\10\1\2\1\11\1\12\1\13\1\14\2\10"+
    "\1\15\1\2\1\16\5\0\1\17\1\20\5\10\1\2"+
    "\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\1"+
    "\1\30\1\31\1\32\1\33\1\34\1\0\1\35\13\0"+
    "\1\36\1\37\6\0\1\40\6\0\1\41\11\0\1\42"+
    "\1\43\7\0\1\44\1\45\2\0\1\46\1\47\15\0"+
    "\1\45\14\0\1\50\1\0\3\45\1\0\1\51\6\0"+
    "\1\52\1\0\1\53\6\0\1\54\1\0\1\43\6\0"+
    "\1\55\3\0\1\56\10\0\1\57\1\0\1\60\7\0"+
    "\1\61\10\0\1\62\1\0";

  private static int [] zzUnpackAction() {
    int [] result = new int[208];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\55\0\132\0\207\0\264\0\341\0\u010e\0\u013b"+
    "\0\u0168\0\u0195\0\u01c2\0\u01ef\0\u021c\0\u01ef\0\u0249\0\u01ef"+
    "\0\u0276\0\u02a3\0\u02d0\0\u02fd\0\u032a\0\u0357\0\u0384\0\u03b1"+
    "\0\u03de\0\u01ef\0\u01ef\0\u01ef\0\u040b\0\u01ef\0\u01ef\0\u01ef"+
    "\0\u01ef\0\u0438\0\u0465\0\u01ef\0\u0492\0\u01ef\0\u04bf\0\u04ec"+
    "\0\u0519\0\u0546\0\u0573\0\u01ef\0\u01ef\0\u05a0\0\u0249\0\u05cd"+
    "\0\u05fa\0\u0627\0\u0654\0\u01ef\0\u01ef\0\u01ef\0\u0681\0\u06ae"+
    "\0\u01ef\0\u01ef\0\u06db\0\u01ef\0\u01ef\0\u01ef\0\u01ef\0\u01c2"+
    "\0\u0708\0\u01ef\0\u0735\0\u0762\0\u078f\0\u07bc\0\u07e9\0\u0816"+
    "\0\u0843\0\u0870\0\u089d\0\u08ca\0\u0438\0\u01ef\0\u08f7\0\u0924"+
    "\0\u0951\0\u097e\0\u09ab\0\u09d8\0\u05a0\0\u01ef\0\u0a05\0\u0a32"+
    "\0\u0a5f\0\u0a8c\0\u0ab9\0\u06db\0\u0ae6\0\u0b13\0\u0b40\0\u0b6d"+
    "\0\u0b9a\0\u0bc7\0\u0bf4\0\u0c21\0\u0c4e\0\u0c7b\0\u0ca8\0\u01ef"+
    "\0\u0cd5\0\u0d02\0\u0d2f\0\u0d5c\0\u0d89\0\u0db6\0\u0de3\0\u0e10"+
    "\0\u0e3d\0\u0e6a\0\u0e97\0\u01ef\0\u01ef\0\u0ec4\0\u0ef1\0\u0f1e"+
    "\0\u0f4b\0\u0f78\0\u0fa5\0\u0fd2\0\u0fff\0\u102c\0\u1059\0\u1086"+
    "\0\u10b3\0\u10e0\0\u110d\0\u113a\0\u1167\0\u1194\0\u11c1\0\u11ee"+
    "\0\u121b\0\u1248\0\u1275\0\u12a2\0\u12cf\0\u12fc\0\u1329\0\u01ef"+
    "\0\u1356\0\u1383\0\u13b0\0\u10e0\0\u13dd\0\u140a\0\u1437\0\u1464"+
    "\0\u1491\0\u14be\0\u14eb\0\u1518\0\u01ef\0\u1545\0\u01ef\0\u1572"+
    "\0\u1383\0\u159f\0\u15cc\0\u15f9\0\u1626\0\u01ef\0\u1653\0\u1680"+
    "\0\u16ad\0\u16da\0\u1707\0\u1734\0\u1761\0\u178e\0\u17bb\0\u17e8"+
    "\0\u1815\0\u1842\0\u01ef\0\u186f\0\u189c\0\u18c9\0\u18f6\0\u1923"+
    "\0\u1950\0\u197d\0\u19aa\0\u01ef\0\u19d7\0\u01ef\0\u1a04\0\u1a31"+
    "\0\u1a5e\0\u1a8b\0\u1ab8\0\u1ae5\0\u1b12\0\u01ef\0\u1b3f\0\u1b6c"+
    "\0\u1b99\0\u1bc6\0\u1bf3\0\u1c20\0\u1c4d\0\u1c7a\0\u01ef\0\u1ca7";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[208];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length() - 1;
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpacktrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\14\1\15\1\16\1\15\17\14\1\17\1\20\3\14"+
    "\1\21\1\14\1\22\2\14\1\23\1\14\1\24\2\14"+
    "\1\25\1\26\1\27\1\30\1\14\1\31\1\14\1\32"+
    "\1\16\1\33\1\14\1\34\1\35\1\36\1\35\2\37"+
    "\1\40\1\41\1\42\2\34\1\43\6\34\1\44\1\34"+
    "\1\20\25\34\1\36\2\34\1\0\1\45\1\16\1\45"+
    "\2\46\20\0\1\47\1\50\2\0\1\51\1\0\1\52"+
    "\6\0\1\53\5\0\1\54\1\16\1\55\1\0\1\34"+
    "\1\15\1\36\1\15\2\34\1\40\1\41\1\56\2\34"+
    "\1\43\6\34\1\44\1\57\1\20\5\34\1\60\5\34"+
    "\1\61\6\34\1\62\2\34\1\36\2\34\1\14\1\63"+
    "\1\14\1\63\1\64\1\65\2\14\1\66\2\67\1\14"+
    "\1\67\2\14\3\67\1\14\1\17\1\14\24\67\5\14"+
    "\1\63\1\14\1\63\1\64\1\65\2\14\1\66\2\70"+
    "\1\14\1\70\2\14\3\70\1\14\1\17\1\14\24\70"+
    "\5\14\1\63\1\14\1\63\1\71\1\72\6\14\1\73"+
    "\6\14\1\17\32\14\1\63\1\14\1\63\5\14\2\67"+
    "\1\14\1\67\1\74\1\14\3\67\1\14\1\17\1\14"+
    "\24\67\5\14\1\63\1\14\1\63\5\14\2\70\1\14"+
    "\1\70\1\75\1\14\3\70\1\14\1\17\1\14\24\70"+
    "\5\14\1\63\1\14\1\63\1\76\1\77\6\14\1\73"+
    "\6\14\1\17\31\14\5\0\1\100\3\0\2\100\1\0"+
    "\1\100\2\0\3\100\4\0\23\100\62\0\1\15\1\0"+
    "\1\15\7\0\1\101\43\0\1\102\47\0\1\102\33\0"+
    "\1\103\66\0\1\104\37\0\1\105\54\0\1\106\64\0"+
    "\1\107\47\0\1\110\64\0\1\111\44\0\1\112\54\0"+
    "\1\113\24\0\1\35\1\0\1\35\7\0\1\114\42\0"+
    "\1\115\1\116\1\115\46\0\1\116\15\0\1\117\42\0"+
    "\1\45\1\0\1\45\113\0\1\120\53\0\1\121\56\0"+
    "\1\122\50\0\1\123\63\0\1\124\7\0\1\125\1\126"+
    "\1\125\46\0\1\126\45\0\1\127\37\0\1\130\57\0"+
    "\1\131\24\0\1\63\1\0\1\63\7\0\1\132\52\0"+
    "\2\67\1\0\1\67\2\0\3\67\3\0\24\67\15\0"+
    "\2\70\1\0\1\70\2\0\3\70\3\0\24\70\16\0"+
    "\1\133\1\0\1\134\53\0\1\135\103\0\1\136\53\0"+
    "\1\137\62\0\1\140\41\0\1\141\66\0\1\142\50\0"+
    "\1\143\60\0\1\144\52\0\1\145\53\0\1\146\24\0"+
    "\1\147\41\0\2\117\2\0\50\117\35\0\1\150\57\0"+
    "\1\151\56\0\1\152\55\0\1\153\52\0\1\154\55\0"+
    "\1\155\52\0\1\156\60\0\1\157\24\0\1\160\55\0"+
    "\1\161\40\0\2\135\2\0\50\135\32\0\1\162\71\0"+
    "\1\163\34\0\1\164\66\0\1\165\46\0\1\166\63\0"+
    "\1\167\56\0\1\170\56\0\1\171\53\0\1\172\10\0"+
    "\2\147\2\0\50\147\43\0\1\173\60\0\1\174\44\0"+
    "\1\175\63\0\1\176\55\0\1\177\37\0\1\200\67\0"+
    "\1\201\10\0\2\160\2\0\50\160\1\0\2\202\2\0"+
    "\10\202\1\203\37\202\41\0\1\204\56\0\1\205\46\0"+
    "\1\206\64\0\1\207\37\0\1\210\26\0\3\171\45\0"+
    "\1\211\37\0\1\212\54\0\1\213\62\0\1\214\43\0"+
    "\1\215\57\0\1\216\62\0\1\217\27\0\1\220\73\0"+
    "\1\221\31\0\2\222\1\0\1\223\2\0\3\222\4\0"+
    "\23\222\4\0\2\202\2\0\5\202\2\224\1\202\1\203"+
    "\2\202\3\224\4\202\23\224\3\202\31\0\1\225\25\0"+
    "\3\205\12\0\1\226\76\0\1\227\50\0\1\230\51\0"+
    "\1\231\23\0\53\232\1\0\1\232\41\0\1\233\51\0"+
    "\1\234\33\0\1\235\76\0\1\236\54\0\1\234\32\0"+
    "\1\237\100\0\1\240\24\0\2\222\1\0\1\222\2\0"+
    "\3\222\4\0\23\222\4\0\2\241\2\0\5\241\2\222"+
    "\1\241\1\223\2\241\3\222\4\241\23\222\3\241\32\0"+
    "\1\242\24\0\3\226\115\0\1\243\55\0\1\244\30\0"+
    "\1\245\33\0\53\232\1\246\1\232\40\0\1\247\45\0"+
    "\1\250\54\0\1\251\63\0\1\252\54\0\1\253\15\0"+
    "\3\243\45\0\1\254\44\0\1\255\44\0\1\256\24\0"+
    "\3\247\12\0\1\257\55\0\1\260\1\261\74\0\1\262"+
    "\31\0\1\263\66\0\1\264\25\0\53\265\1\0\1\265"+
    "\43\0\1\266\56\0\1\267\10\0\3\257\107\0\1\270"+
    "\56\0\1\271\61\0\1\272\43\0\1\273\20\0\53\265"+
    "\1\274\1\265\34\0\1\275\64\0\1\276\36\0\1\277"+
    "\64\0\1\300\44\0\1\301\57\0\1\302\54\0\1\303"+
    "\67\0\1\304\60\0\1\150\51\0\1\305\53\0\1\306"+
    "\54\0\1\307\54\0\1\310\44\0\1\311\21\0\3\307"+
    "\45\0\1\312\45\0\1\313\53\0\1\314\13\0\53\315"+
    "\1\0\1\315\26\0\1\316\66\0\1\150\14\0\53\315"+
    "\1\317\1\315\45\0\1\320\42\0\1\150\21\0";

  private static int [] zzUnpacktrans() {
    int [] result = new int[7380];
    int offset = 0;
    offset = zzUnpacktrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpacktrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\13\0\1\11\1\1\1\11\1\1\1\11\11\1\3\11"+
    "\1\1\4\11\2\1\1\11\1\1\1\11\5\0\2\11"+
    "\6\1\3\11\2\1\2\11\1\1\4\11\1\1\1\0"+
    "\1\11\13\0\1\11\1\1\6\0\1\11\6\0\1\1"+
    "\11\0\1\1\1\11\7\0\2\1\2\0\2\11\15\0"+
    "\1\1\14\0\1\11\1\0\3\1\1\0\1\1\6\0"+
    "\1\11\1\0\1\11\6\0\1\11\1\0\1\1\6\0"+
    "\1\1\3\0\1\11\10\0\1\11\1\0\1\11\7\0"+
    "\1\11\10\0\1\11\1\0";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[208];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** Number of newlines encountered up to the start of the matched text. */
  @SuppressWarnings("unused")
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  @SuppressWarnings("unused")
  protected int yycolumn;

  /** Number of characters up to the start of the matched text. */
  @SuppressWarnings("unused")
  private long yychar;

  /** Whether the scanner is currently at the beginning of a line. */
  @SuppressWarnings("unused")
  private boolean zzAtBOL = true;

  /** Whether the user-EOF-code has already been executed. */
  private boolean zzEOFDone;

  /* user code: */
  public BuildGradleLexer() {
    this((java.io.Reader)null);
  }
  private int dependenciesBracketsCounter = 0;
  private boolean DependenciesStarted = false;
  private boolean secondEnclosingSignForMap = false;

  private void checkIfReturnToMapDependency() {
      if(secondEnclosingSignForMap) {
          secondEnclosingSignForMap = false;
          yybegin(MAP_DEPENDENCY);
        }
      else {
           secondEnclosingSignForMap = true;
        }
  }



  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public BuildGradleLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** Returns the maximum size of the scanner buffer, which limits the size of tokens. */
  private int zzMaxBufferLen() {
    return Integer.MAX_VALUE;
  }

  /**  Whether the scanner buffer can grow to accommodate a larger token. */
  private boolean zzCanGrow() {
    return true;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      {@code false}, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position {@code pos} from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void zzDoEOF() {
    if (!zzEOFDone) {
      zzEOFDone = true;
    
  return;
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException
  {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMap(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
            zzDoEOF();
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
            { yybegin(YYINITIAL); return BAD_CHARACTER;
            }
          // fall through
          case 51: break;
          case 2:
            { return SPACE_CHARACTER;
            }
          // fall through
          case 52: break;
          case 3:
            { return CRLF;
            }
          // fall through
          case 53: break;
          case 4:
            { return BACKSLASH;
            }
          // fall through
          case 54: break;
          case 5:
            { return RSBRACE;
            }
          // fall through
          case 55: break;
          case 6:
            { return LCURBRACE;
            }
          // fall through
          case 56: break;
          case 7:
            { return RCURBRACE;
            }
          // fall through
          case 57: break;
          case 8:
            { return BAD_CHARACTER;
            }
          // fall through
          case 58: break;
          case 9:
            { yybegin(HANDLE_DEPENDENCIES); return CRLF;
            }
          // fall through
          case 59: break;
          case 10:
            { yybegin(EXTRACT_GROUP_STRING);
            }
          // fall through
          case 60: break;
          case 11:
            { return LPARENTHESIS;
            }
          // fall through
          case 61: break;
          case 12:
            { return RPARENTHESIS;
            }
          // fall through
          case 62: break;
          case 13:
            { return LSBRACE;
            }
          // fall through
          case 63: break;
          case 14:
            { yybegin(STRING_DEPENDENCY); yypushback(yylength());
            }
          // fall through
          case 64: break;
          case 15:
            { dependenciesBracketsCounter++ ; return LCURBRACE;
            }
          // fall through
          case 65: break;
          case 16:
            { if(--dependenciesBracketsCounter == 0) {
                                    yybegin(YYINITIAL);}  return RCURBRACE;
            }
          // fall through
          case 66: break;
          case 17:
            { return QUATATION_MARK;
            }
          // fall through
          case 67: break;
          case 18:
            { return APOSTROPHE;
            }
          // fall through
          case 68: break;
          case 19:
            { yybegin(MAP_DEPENDENCY); return COMMA;
            }
          // fall through
          case 69: break;
          case 20:
            { return GROUP_ID;
            }
          // fall through
          case 70: break;
          case 21:
            { return ARTIFACT_ID;
            }
          // fall through
          case 71: break;
          case 22:
            { this.checkIfReturnToMapDependency(); return QUATATION_MARK;
            }
          // fall through
          case 72: break;
          case 23:
            { this.checkIfReturnToMapDependency(); return APOSTROPHE;
            }
          // fall through
          case 73: break;
          case 24:
            { yybegin(EXTRACT_ARTIFACT_STRING); return COLON;
            }
          // fall through
          case 74: break;
          case 25:
            { yybegin(EXTRACT_VERSION_STRING); return COLON;
            }
          // fall through
          case 75: break;
          case 26:
            { yybegin(STRING_DEPENDENCY); return QUATATION_MARK;
            }
          // fall through
          case 76: break;
          case 27:
            { yybegin(STRING_DEPENDENCY);  return APOSTROPHE;
            }
          // fall through
          case 77: break;
          case 28:
            { yybegin(YYINITIAL); return ROOT_GROUP_VERSION_VALUE;
            }
          // fall through
          case 78: break;
          case 29:
            { 
            }
          // fall through
          case 79: break;
          case 30:
            { yybegin(STRING_DEPENDENCY);
            }
          // fall through
          case 80: break;
          case 31:
            { yybegin(HANDLE_DEPENDENCIES); return COMMENT;
            }
          // fall through
          case 81: break;
          case 32:
            { yybegin(MAP_DEPENDENCY);
            }
          // fall through
          case 82: break;
          case 33:
            { return LINE_COMMENT;
            }
          // fall through
          case 83: break;
          case 34:
            { yybegin(HANDLE_DEPENDENCIES); return LINE_COMMENT;
            }
          // fall through
          case 84: break;
          case 35:
            { return CONFIG_NAME;
            }
          // fall through
          case 85: break;
          case 36:
            { yybegin(YYINITIAL); return LINE_COMMENT;
            }
          // fall through
          case 86: break;
          case 37:
            { return VERSION;
            }
          // fall through
          case 87: break;
          case 38:
            { return JAVA;
            }
          // fall through
          case 88: break;
          case 39:
            { return MAIN;
            }
          // fall through
          case 89: break;
          case 40:
            { yybegin(HANDLE_ARTIFACT); return NAME_KEY;
            }
          // fall through
          case 90: break;
          case 41:
            { yybegin(GET_ROOT_VALUES); return ROOT_GROUP_KEY;
            }
          // fall through
          case 91: break;
          case 42:
            { yybegin(MAP_DEPENDENCY); yypushback(yylength());
            }
          // fall through
          case 92: break;
          case 43:
            { yybegin(HANDLE_GROUP);  return GROUP_KEY;
            }
          // fall through
          case 93: break;
          case 44:
            { return TEST;
            }
          // fall through
          case 94: break;
          case 45:
            { yybegin(GET_ROOT_VALUES); return ROOT_VERSION_KEY;
            }
          // fall through
          case 95: break;
          case 46:
            { yybegin(HANDLE_VERSION); return VERSION_KEY;
            }
          // fall through
          case 96: break;
          case 47:
            { return PLUGINS;
            }
          // fall through
          case 97: break;
          case 48:
            { return SOURCE_SETS;
            }
          // fall through
          case 98: break;
          case 49:
            { yybegin(HANDLE_DEPENDENCIES); return DEPENDENCIES;
            }
          // fall through
          case 99: break;
          case 50:
            { return REPOSITORIES;
            }
          // fall through
          case 100: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
