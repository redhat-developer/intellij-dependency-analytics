package org.jboss.tools.intellij.componentanalysis.golang.build.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.jboss.tools.intellij.componentanalysis.golang.build.psi.GoModTypes;
import com.intellij.psi.TokenType;

%%

%class GoModLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%{
  public GoModLexer() {
    this((java.io.Reader)null);
  }
%}

// Whitespace and line endings
WHITESPACE = [ \t\f]+
NEWLINE = \r?\n

// Comments
COMMENT = "//"[^\r\n]*

// Keywords
MODULE = "module"
GO = "go"
REQUIRE = "require"
REPLACE = "replace"
EXCLUDE = "exclude"
INDIRECT = "indirect"

// Operators and delimiters
LPAREN = "("
RPAREN = ")"
ARROW = "=>"

// Identifiers and versions - VERSION must come before IDENTIFIER to avoid conflicts
VERSION = v?[0-9]+(\.[0-9]+)*(-[0-9a-zA-Z\-_.]+)?(\+[0-9a-zA-Z\-_.]+)?
IDENTIFIER = (\.\.\/[a-zA-Z0-9_\.\/\-]*|\/[a-zA-Z0-9_\.\/\-]+|[a-zA-Z_][a-zA-Z0-9_\.\/\-]*)

%%

<YYINITIAL> {
  {WHITESPACE}        { return TokenType.WHITE_SPACE; }
  {NEWLINE}           { return GoModTypes.NEWLINE; }
  {COMMENT}           { return GoModTypes.COMMENT; }
  
  {MODULE}            { return GoModTypes.MODULE; }
  {GO}                { return GoModTypes.GO; }
  {REQUIRE}           { return GoModTypes.REQUIRE; }
  {REPLACE}           { return GoModTypes.REPLACE; }
  {EXCLUDE}           { return GoModTypes.EXCLUDE; }
  {INDIRECT}          { return GoModTypes.INDIRECT; }

  {LPAREN}            { return GoModTypes.LPAREN; }
  {RPAREN}            { return GoModTypes.RPAREN; }
  {ARROW}             { return GoModTypes.ARROW; }
  
  {VERSION}           { return GoModTypes.VERSION; }
  {IDENTIFIER}        { return GoModTypes.IDENTIFIER; }
  
  [^]                 { return TokenType.BAD_CHARACTER; }
}