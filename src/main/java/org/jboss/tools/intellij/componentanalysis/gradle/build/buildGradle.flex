package org.jboss.tools.intellij.componentanalysis.gradle.build.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static org.jboss.tools.intellij.componentanalysis.gradle.build.psi.BuildGradleTypes.*;

%%

%{
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

%}

%public
%class BuildGradleLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%eof{
  return;
%eof}

PLUGINS=plugins\s*[{][^}]+[}]
DEPENDENCIES="dependencies"
ROOT_GROUP_KEY=group\s*[=]\s*
ROOT_GROUP_VERSION_VALUE=[a-zA-Z0-9.'-]+
ROOT_VERSION_KEY=version\s*[=]\s*
TEST=test\s*[{][^}]+[}]
REPOSITORIES=repositories\s*[{][^}]+[}]([\n|\r|\r\n][}])*
SOURCE_SETS=sourceSets
MAIN=main
JAVA=java
CRLF=[\n|\r|\r\n]
SPACE_CHARACTER=[^\S\r\n]+
BACKSLASH="\\"
COMMA=","
COMMA_CONTINUTATION={COMMA}({SPACE_CHARACTER}?){CRLF}
COLON=":"
QUATATION_MARK="\""
APOSTROPHE = "'"
LSBRACE="["
RSBRACE="]"
LCURBRACE="{"
RCURBRACE="}"
LPARENTHESIS = "("
RPARENTHESIS = ")"
//ENCLOSING_SIGNS=[{APOSTROPHE}{QUATATION_MARK}]?


COMMENT=(\/\/.*)
GROUP_KEY=group:
NAME_KEY=name:
VERSION_KEY=version:

CONFIG_NAME=implementation|api|compile|compileClasspath|compileOnly|runtime|runtimeClasspath|runtimeOnly

GROUP_ID=[a-zA-Z0-9._-]+
ARTIFACT_ID=[a-zA-Z0-9_.-]+
VERSION=[0-9]+[.][0-9]+(.[0-9]+)?(.[.a-zA-Z0-9-]+)?


LINE_COMMENT={SPACE_CHARACTER}{COMMENT}

%xstate STRING_DEPENDENCY
%xstate HANDLE_DEPENDENCIES
%state MAP_DEPENDENCY
%state HANDLE_GROUP
%state HANDLE_ARTIFACT
%state HANDLE_VERSION
%state EXTRACT_GROUP_STRING
%state EXTRACT_ARTIFACT_STRING
%state EXTRACT_VERSION_STRING
%xstate GET_ROOT_VALUES


%%
<YYINITIAL> {
    {PLUGINS}                { return PLUGINS; }
    {ROOT_GROUP_KEY}     {   yybegin(GET_ROOT_VALUES); return ROOT_GROUP_KEY; }
    {ROOT_VERSION_KEY}    {   yybegin(GET_ROOT_VALUES); return ROOT_VERSION_KEY; }
    {REPOSITORIES}           { return REPOSITORIES; }
    {TEST}                   { return TEST; }
    {SOURCE_SETS}             { return SOURCE_SETS; }
    {MAIN}                    { return MAIN; }
    {JAVA}                    { return JAVA; }
    {LCURBRACE}               { return LCURBRACE; }
    {RCURBRACE}               { return RCURBRACE; }
    {RSBRACE}                 { return RSBRACE; }
    {LINE_COMMENT}            { return LINE_COMMENT; }
    {DEPENDENCIES}           { yybegin(HANDLE_DEPENDENCIES); return DEPENDENCIES;}
    {CRLF}                    { return CRLF; }
}

<GET_ROOT_VALUES>{
     {ROOT_GROUP_VERSION_VALUE} { yybegin(YYINITIAL); return ROOT_GROUP_VERSION_VALUE; }
}

<HANDLE_DEPENDENCIES> {
    {LCURBRACE}                 { dependenciesBracketsCounter++ ; return LCURBRACE; }
    {RCURBRACE}                 { if(--dependenciesBracketsCounter == 0) {
                                    yybegin(YYINITIAL);}  return RCURBRACE; }
    {CRLF}                      { return CRLF; }
    {SPACE_CHARACTER}           { return SPACE_CHARACTER; }
    {CONFIG_NAME}               { return CONFIG_NAME; }
    {APOSTROPHE}                { yybegin(STRING_DEPENDENCY); yypushback(yylength()); }
    {QUATATION_MARK}            { yybegin(STRING_DEPENDENCY); yypushback(yylength());  }
    {GROUP_KEY}                 { yybegin(MAP_DEPENDENCY); yypushback(yylength()); }

}

<STRING_DEPENDENCY> {
    {LPARENTHESIS}           { return LPARENTHESIS; }
    {RPARENTHESIS}           { return RPARENTHESIS; }
    {LSBRACE}                { return LSBRACE; }
    {RSBRACE}                { return RSBRACE; }
    {COMMENT}                { yybegin(HANDLE_DEPENDENCIES); return COMMENT; }
    {LINE_COMMENT}           { yybegin(HANDLE_DEPENDENCIES); return LINE_COMMENT; }
    {SPACE_CHARACTER}        { return SPACE_CHARACTER;  }
    {APOSTROPHE}             { yybegin(EXTRACT_GROUP_STRING); }
    {QUATATION_MARK}         { yybegin(EXTRACT_GROUP_STRING); }
    {COMMA_CONTINUTATION}    { yybegin(STRING_DEPENDENCY); }
    {CRLF}                   { yybegin(HANDLE_DEPENDENCIES); return CRLF; }
    [^]                      { return BAD_CHARACTER; }

}

<EXTRACT_GROUP_STRING> {
    {GROUP_ID}               { return GROUP_ID;}
    {COLON}                  { yybegin(EXTRACT_ARTIFACT_STRING); return COLON; }
}

<EXTRACT_ARTIFACT_STRING> {
    {ARTIFACT_ID}            { return ARTIFACT_ID;}
    {COLON}                  { yybegin(EXTRACT_VERSION_STRING); return COLON; }
}

<EXTRACT_VERSION_STRING> {
    {VERSION}                { return VERSION;}
    {QUATATION_MARK}         { yybegin(STRING_DEPENDENCY); return QUATATION_MARK; }
    {APOSTROPHE}             { yybegin(STRING_DEPENDENCY);  return APOSTROPHE; }
}



<MAP_DEPENDENCY> {
    {LPARENTHESIS}           { return LPARENTHESIS; }
    {RPARENTHESIS}           { return RPARENTHESIS; }
    {LSBRACE}                { return LSBRACE; }
    {RSBRACE}                { return RSBRACE; }
    {COMMENT}                { yybegin(HANDLE_DEPENDENCIES); return COMMENT; }
    {LINE_COMMENT}           {  return LINE_COMMENT; }
    {GROUP_KEY}              { yybegin(HANDLE_GROUP);  return GROUP_KEY;}
    {SPACE_CHARACTER}        { return SPACE_CHARACTER; }
    {NAME_KEY}               { yybegin(HANDLE_ARTIFACT); return NAME_KEY;  }
    {VERSION_KEY}            { yybegin(HANDLE_VERSION); return VERSION_KEY; }
    {COMMA_CONTINUTATION}    { yybegin(MAP_DEPENDENCY);  }
    {CRLF}                   { yybegin(HANDLE_DEPENDENCIES); return CRLF; }
    [^]                      { return BAD_CHARACTER; }

}

<HANDLE_GROUP> {
    {QUATATION_MARK}         { return QUATATION_MARK; }
    {APOSTROPHE}             { return APOSTROPHE; }
    {GROUP_ID}               { return GROUP_ID;}
    {COMMA}                  { yybegin(MAP_DEPENDENCY); return COMMA; }
}


<HANDLE_ARTIFACT> {
    {QUATATION_MARK}         { return QUATATION_MARK; }
    {APOSTROPHE}             { return APOSTROPHE; }
    {ARTIFACT_ID}            { return ARTIFACT_ID;}
    {COMMA}                  { yybegin(MAP_DEPENDENCY); return COMMA; }
}

<HANDLE_VERSION> {
    {QUATATION_MARK}         { this.checkIfReturnToMapDependency(); return QUATATION_MARK;  }
    {APOSTROPHE}             { this.checkIfReturnToMapDependency(); return APOSTROPHE; }
    {VERSION}                { return VERSION;}
}

{LINE_COMMENT}               { yybegin(YYINITIAL); return LINE_COMMENT; }

{BACKSLASH}{CRLF}            { }

{SPACE_CHARACTER}            { return SPACE_CHARACTER; }

{BACKSLASH}                  { return BACKSLASH; }

[^]                          { yybegin(YYINITIAL); return BAD_CHARACTER; }
