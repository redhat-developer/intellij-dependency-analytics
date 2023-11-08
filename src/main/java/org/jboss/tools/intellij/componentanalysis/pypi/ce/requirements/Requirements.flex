package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi.RequirementsTypes.*;

%%

%{
  public RequirementsLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class RequirementsLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%eof{
  return;
%eof}

CRLF=[\n|\r|\r\n]
WHITE_SPACE=[^\S\r\n]+
BACKSLASH="\\"
COMMA=","
LSBRACE="["
RSBRACE="]"
LPARENTHESIS = "("
RPARENTHESIS = ")"
AT="@"

COMMENT=#.*

IDENTIFIER=[a-zA-Z0-9](([a-zA-Z0-9_.-])*[a-zA-Z0-9])*
VERSION_CMP=("<=" | "<" | "!=" | "===" | "==" | ">=" | ">" | "~=")
VERSION=([a-zA-Z0-9_.*+!-])+
REQ_COMMENT={WHITE_SPACE}{COMMENT}
REQ_OPTION=(";" | "--")
URI_OPTION=("/" | "@" | "+" | ":")
IDENTIFIER_URI={IDENTIFIER}{URI_OPTION}
OTHER_OPTION=("-" | "." | "/")

OTHER=[^\s\r\n\t\f\\]+

%state REQ
%state REQ_OTHER
%state URI_SPEC
%state OTHER_SPEC
%state HANDLE_VERSION

%%
<YYINITIAL> {
    {COMMENT}                { return COMMENT; }
    {IDENTIFIER}             { yybegin(REQ); yypushback(yylength()); }
    {OTHER_OPTION}           { yybegin(OTHER_SPEC); yypushback(yylength()); }
}

<REQ> {
    {IDENTIFIER_URI}         { yybegin(URI_SPEC); yypushback(yylength()); }

    {IDENTIFIER}             { return IDENTIFIER; }
    {VERSION_CMP}            { yybegin(HANDLE_VERSION); return VERSION_CMP; }

    {COMMA}                  { return COMMA; }
    {LPARENTHESIS}           { return LPARENTHESIS; }
    {RPARENTHESIS}           { return RPARENTHESIS; }
    {LSBRACE}                { return LSBRACE; }
    {RSBRACE}                { return RSBRACE; }
    {AT}                     { yybegin(REQ_OTHER); return AT; }
    {REQ_COMMENT}            { yybegin(YYINITIAL); return REQ_COMMENT; }

    {URI_OPTION}             { yybegin(URI_SPEC); yypushback(yylength()); }
    {REQ_OPTION}             { yybegin(REQ_OTHER); yypushback(yylength()); }
}

<HANDLE_VERSION> {
    {VERSION}                { yybegin(REQ); return VERSION; }
    {REQ_COMMENT}            { yybegin(YYINITIAL); return REQ_COMMENT; }
    {REQ_OPTION}             { yybegin(REQ_OTHER); yypushback(yylength()); }
}

<REQ_OTHER> {
    {REQ_COMMENT}            { yybegin(YYINITIAL); return REQ_COMMENT; }
    {OTHER}                  { return REQ_PART; }
}

<URI_SPEC> {
    {OTHER}                  { return URI_PART; }
}

<OTHER_SPEC> {
    {OTHER}                  { return OTHER_PART; }
}

{WHITE_SPACE}{COMMENT}       { yybegin(YYINITIAL); return COMMENT; }

{WHITE_SPACE}                { return WHITE_SPACE; }

{BACKSLASH}{CRLF}            { }

{BACKSLASH}                  { return BACKSLASH; }

{CRLF}                       { yybegin(YYINITIAL); return CRLF; }

[^]                          { yybegin(YYINITIAL); return BAD_CHARACTER; }
