package org.jboss.tools.intellij.analytics;

public interface ICookie {
  enum Name {
    LSPVersion,
    CLIVersion
  }

  void setValue(Name name, String value);
  String getValue(Name name);
}
