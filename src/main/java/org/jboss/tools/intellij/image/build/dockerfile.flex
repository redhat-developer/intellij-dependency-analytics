package org.jboss.tools.intellij.image.build.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import org.jboss.tools.intellij.image.build.psi.DockerfileTypes;
import com.intellij.psi.TokenType;

%%

%class DockerfileLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%ignorecase

%{
  public DockerfileLexer() {
    this((java.io.Reader)null);
  }
%}

// Whitespace and line endings
LINE_CONTINUATION = "\\"[ \t]*\r?\n
WHITESPACE = [ \t\f]+
NEWLINE = \r?\n

// Comments (include trailing newline so comments inside multi-line instructions don't break continuation)
COMMENT = "#"[^\r\n]*(\r?\n)?

// Keywords (case insensitive)
FROM = [Ff][Rr][Oo][Mm]
ARG = [Aa][Rr][Gg]
AS = [Aa][Ss]
RUN = [Rr][Uu][Nn]
COPY = [Cc][Oo][Pp][Yy]
ADD = [Aa][Dd][Dd]
WORKDIR = [Ww][Oo][Rr][Kk][Dd][Ii][Rr]
CMD = [Cc][Mm][Dd]
ENTRYPOINT = [Ee][Nn][Tt][Rr][Yy][Pp][Oo][Ii][Nn][Tt]
ENV = [Ee][Nn][Vv]
EXPOSE = [Ee][Xx][Pp][Oo][Ss][Ee]
VOLUME = [Vv][Oo][Ll][Uu][Mm][Ee]
USER = [Uu][Ss][Ee][Rr]
LABEL = [Ll][Aa][Bb][Ee][Ll]
MAINTAINER = [Mm][Aa][Ii][Nn][Tt][Aa][Ii][Nn][Ee][Rr]
HEALTHCHECK = [Hh][Ee][Aa][Ll][Tt][Hh][Cc][Hh][Ee][Cc][Kk]
SHELL = [Ss][Hh][Ee][Ll][Ll]
STOPSIGNAL = [Ss][Tt][Oo][Pp][Ss][Ii][Gg][Nn][Aa][Ll]
ONBUILD = [Oo][Nn][Bb][Uu][Ii][Ll][Dd]

// Platform option
PLATFORM_FLAG = "--platform"

// Operators and delimiters
EQUALS = "="
COLON = ":"
DOLLAR = "$"
LBRACE = "{"
RBRACE = "}"

// Any non-whitespace, non-newline character (catch-all for any instruction content)
ANY_CHAR = [^\s\r\n]

// Platform values (specific patterns first)
PLATFORM = "linux/"("amd64"|"arm64"|"386"|"arm/v7"|"arm/v6"|"ppc64le"|"s390x")

// Strings
STRING = "\""([^\"\\]|\\.)*"\""

// Version numbers (numbers starting patterns)
VERSION = [0-9]+(\.[0-9]+)*(\-[a-zA-Z0-9\-_.]+)?

// Identifiers (letter starting patterns)
IDENTIFIER = [a-zA-Z_][a-zA-Z0-9_.\-]*

// Image names (most general pattern, should be last)
IMAGE_NAME_TOKEN = [a-zA-Z0-9._\-/]+

%%

<YYINITIAL> {
  {LINE_CONTINUATION}  { return TokenType.WHITE_SPACE; }
  {WHITESPACE}        { return TokenType.WHITE_SPACE; }
  {NEWLINE}           { return DockerfileTypes.NEWLINE; }
  {COMMENT}           { return DockerfileTypes.COMMENT; }

  {FROM}              { return DockerfileTypes.FROM; }
  {ARG}               { return DockerfileTypes.ARG; }
  {AS}                { return DockerfileTypes.AS; }
  {RUN}               { return DockerfileTypes.RUN; }
  {COPY}              { return DockerfileTypes.COPY; }
  {ADD}               { return DockerfileTypes.ADD; }
  {WORKDIR}           { return DockerfileTypes.WORKDIR; }
  {CMD}               { return DockerfileTypes.CMD; }
  {ENTRYPOINT}        { return DockerfileTypes.ENTRYPOINT; }
  {ENV}               { return DockerfileTypes.ENV; }
  {EXPOSE}            { return DockerfileTypes.EXPOSE; }
  {VOLUME}            { return DockerfileTypes.VOLUME; }
  {USER}              { return DockerfileTypes.USER; }
  {LABEL}             { return DockerfileTypes.LABEL; }
  {MAINTAINER}        { return DockerfileTypes.MAINTAINER; }
  {HEALTHCHECK}       { return DockerfileTypes.HEALTHCHECK; }
  {SHELL}             { return DockerfileTypes.SHELL; }
  {STOPSIGNAL}        { return DockerfileTypes.STOPSIGNAL; }
  {ONBUILD}           { return DockerfileTypes.ONBUILD; }

  {PLATFORM_FLAG}     { return DockerfileTypes.PLATFORM_FLAG; }

  {EQUALS}            { return DockerfileTypes.EQUALS; }
  {COLON}             { return DockerfileTypes.COLON; }
  {DOLLAR}            { return DockerfileTypes.DOLLAR; }
  {LBRACE}            { return DockerfileTypes.LBRACE; }
  {RBRACE}            { return DockerfileTypes.RBRACE; }

  {STRING}            { return DockerfileTypes.STRING; }
  {PLATFORM}          { return DockerfileTypes.PLATFORM; }
  {VERSION}           { return DockerfileTypes.VERSION; }
  {IDENTIFIER}        { return DockerfileTypes.IDENTIFIER; }
  {IMAGE_NAME_TOKEN}  { return DockerfileTypes.IMAGE_NAME_TOKEN; }

  {ANY_CHAR}          { return DockerfileTypes.ANY_CHAR; }

  [^]                 { return TokenType.BAD_CHARACTER; }
}