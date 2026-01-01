# stopgap

- Helidon SE Template
- check out :app src and test

## Overview

- uses Helidon SE (the minimal, code-first API surface).
- no runtime/reflection magic
- keeping dependencies to a minimum.
- ir: instance registry with codegen.
- helidon-extensions: codegen
- app: template and starter project

## Tests

- e2e
  - build docker image via gradle
  - run docker image via junit extension mechanism
  - WebClientExtension to inject webclient into tests
- intTest
  - WebServerTestExtension to help test endpoints by bringing up a loom webserver
  - WebClientExtension to inject webclient into tests
- unit

## Overview of Helidon

- No async mess — handlers are plain, blocking Java methods executed on virtual threads.
- Helidon Nima webserver which is rewritten to take advantage of Project Loom virtual threads.

- Web server: Helidon WebServer (Nima) 4.x
- Config: YAML via helidon-config-yaml
- Minimal DI: a small InstanceRegistry (custom) instead of a full container
- Tests: JUnit Jupiter
