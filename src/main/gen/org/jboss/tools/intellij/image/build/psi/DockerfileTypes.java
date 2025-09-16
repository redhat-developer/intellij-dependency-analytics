// This is a generated file. Not intended for manual editing.
package org.jboss.tools.intellij.image.build.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.jboss.tools.intellij.image.build.psi.impl.*;

public interface DockerfileTypes {

  IElementType ARG_DECLARATION = new DockerfileElementType("ARG_DECLARATION");
  IElementType ARG_INSTRUCTION = new DockerfileElementType("ARG_INSTRUCTION");
  IElementType ARG_VALUE = new DockerfileElementType("ARG_VALUE");
  IElementType AS_CLAUSE = new DockerfileElementType("AS_CLAUSE");
  IElementType COMPLEX_VAR = new DockerfileElementType("COMPLEX_VAR");
  IElementType FROM_INSTRUCTION = new DockerfileElementType("FROM_INSTRUCTION");
  IElementType IMAGE_NAME = new DockerfileElementType("IMAGE_NAME");
  IElementType IMAGE_NAME_LITERAL = new DockerfileElementType("IMAGE_NAME_LITERAL");
  IElementType IMAGE_NAME_PART = new DockerfileElementType("IMAGE_NAME_PART");
  IElementType INSTRUCTION = new DockerfileElementType("INSTRUCTION");
  IElementType INSTRUCTION_ARGS = new DockerfileElementType("INSTRUCTION_ARGS");
  IElementType ITEM = new DockerfileElementType("ITEM");
  IElementType OTHER_INSTRUCTION = new DockerfileElementType("OTHER_INSTRUCTION");
  IElementType PLATFORM_OPTION = new DockerfileElementType("PLATFORM_OPTION");
  IElementType PLATFORM_VALUE = new DockerfileElementType("PLATFORM_VALUE");
  IElementType SIMPLE_VAR = new DockerfileElementType("SIMPLE_VAR");
  IElementType VARIABLE_REF = new DockerfileElementType("VARIABLE_REF");

  IElementType ADD = new DockerfileTokenType("add");
  IElementType ANY_CHAR = new DockerfileTokenType("ANY_CHAR");
  IElementType ARG = new DockerfileTokenType("arg");
  IElementType AS = new DockerfileTokenType("as");
  IElementType CMD = new DockerfileTokenType("cmd");
  IElementType COLON = new DockerfileTokenType(":");
  IElementType COMMENT = new DockerfileTokenType("COMMENT");
  IElementType COPY = new DockerfileTokenType("copy");
  IElementType DOLLAR = new DockerfileTokenType("$");
  IElementType ENTRYPOINT = new DockerfileTokenType("entrypoint");
  IElementType ENV = new DockerfileTokenType("env");
  IElementType EQUALS = new DockerfileTokenType("=");
  IElementType EXPOSE = new DockerfileTokenType("expose");
  IElementType FROM = new DockerfileTokenType("from");
  IElementType IDENTIFIER = new DockerfileTokenType("IDENTIFIER");
  IElementType IMAGE_NAME_TOKEN = new DockerfileTokenType("IMAGE_NAME_TOKEN");
  IElementType LABEL = new DockerfileTokenType("label");
  IElementType LBRACE = new DockerfileTokenType("{");
  IElementType NEWLINE = new DockerfileTokenType("NEWLINE");
  IElementType OTHER_TOKEN = new DockerfileTokenType("OTHER_TOKEN");
  IElementType PLATFORM = new DockerfileTokenType("PLATFORM");
  IElementType PLATFORM_FLAG = new DockerfileTokenType("--platform");
  IElementType RBRACE = new DockerfileTokenType("}");
  IElementType RUN = new DockerfileTokenType("run");
  IElementType STRING = new DockerfileTokenType("STRING");
  IElementType USER = new DockerfileTokenType("user");
  IElementType VERSION = new DockerfileTokenType("VERSION");
  IElementType VOLUME = new DockerfileTokenType("volume");
  IElementType WORKDIR = new DockerfileTokenType("workdir");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ARG_DECLARATION) {
        return new DockerfileArgDeclarationImpl(node);
      }
      else if (type == ARG_INSTRUCTION) {
        return new DockerfileArgInstructionImpl(node);
      }
      else if (type == ARG_VALUE) {
        return new DockerfileArgValueImpl(node);
      }
      else if (type == AS_CLAUSE) {
        return new DockerfileAsClauseImpl(node);
      }
      else if (type == COMPLEX_VAR) {
        return new DockerfileComplexVarImpl(node);
      }
      else if (type == FROM_INSTRUCTION) {
        return new DockerfileFromInstructionImpl(node);
      }
      else if (type == IMAGE_NAME) {
        return new DockerfileImageNameImpl(node);
      }
      else if (type == IMAGE_NAME_LITERAL) {
        return new DockerfileImageNameLiteralImpl(node);
      }
      else if (type == IMAGE_NAME_PART) {
        return new DockerfileImageNamePartImpl(node);
      }
      else if (type == INSTRUCTION) {
        return new DockerfileInstructionImpl(node);
      }
      else if (type == INSTRUCTION_ARGS) {
        return new DockerfileInstructionArgsImpl(node);
      }
      else if (type == ITEM) {
        return new DockerfileItemImpl(node);
      }
      else if (type == OTHER_INSTRUCTION) {
        return new DockerfileOtherInstructionImpl(node);
      }
      else if (type == PLATFORM_OPTION) {
        return new DockerfilePlatformOptionImpl(node);
      }
      else if (type == PLATFORM_VALUE) {
        return new DockerfilePlatformValueImpl(node);
      }
      else if (type == SIMPLE_VAR) {
        return new DockerfileSimpleVarImpl(node);
      }
      else if (type == VARIABLE_REF) {
        return new DockerfileVariableRefImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
