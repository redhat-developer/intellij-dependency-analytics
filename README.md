# Dependency Analytics

[plugin-repo]: https://plugins.jetbrains.com/plugin/12541-dependency-analytics
[plugin-version-svg]: https://img.shields.io/jetbrains/plugin/v/12541-dependency-analytics.svg
[plugin-downloads-svg]: https://img.shields.io/jetbrains/plugin/d/12541-dependency-analytics.svg

![Java CI with Gradle](https://github.com/redhat-developer/intellij-dependency-analytics/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![JetBrains plugins][plugin-version-svg]][plugin-repo]
[![JetBrains plugins][plugin-downloads-svg]][plugin-repo]

'Dependency Analytics Report' with Insights about your application dependencies:

- Flags a security vulnerability(CVE) and suggests a remedial version

## Supported Languages

'Dependency Analytics' plugin supports projects using Maven, projects build on npm (Node ecosystem) and projects using Python.
Extending support for Golang and other languages is currently under progress.

> **NOTE** Dependency Analytics is an online service hosted and maintained by Red Hat. This open source software will access only your manifests file(s) to learn about application dependencies before giving you the report.

## Quick Start

- Install the plugin.
- Opening or editing a manifest file (`pom.xml` / `package.json` / `requirements.txt`) scans your application for security vulnerabilities.

## Features

1. Opening or editing a manifest file (`pom.xml` / `package.json` / `requirements.txt`) scans your application for security vulnerabilities, flag them along with 'quick fixes'.

![ screencast ](src/main/resources/images/demo.gif)



# Know more about Dependency Analytics Platform

The mission of this project is to significantly enhance developer experience:
providing Insights(security) for applications and helping developers, Enterprises.

- [GitHub Organization](https://github.com/redhat-developer)

# Feedback & Questions

- File a bug in [GitHub Issues](https://github.com/redhat-developer/intellij-dependency-analytics/issues)

# License

EPL 2.0, See [LICENSE](LICENSE) for more information.
