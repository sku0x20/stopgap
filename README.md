stopgap — Helidon SE spike on Loom (Nima) with JDK 25

Overview

- This is a tiny spike project to try out helidon.io.
- Uses Helidon SE (the minimal, code-first API surface). No CDI; a tiny custom registry wires things together.
- Runs on Helidon Nima webserver which is rewritten to take advantage of Project Loom virtual threads.
- No async mess — handlers are plain, blocking Java methods executed on virtual threads.
- Philosophy: no magic or as little as possible; keep dependencies to a minimum.
- Built and tested with the latest JDK 25 (released recently).

What’s inside

- Web server: Helidon WebServer (Nima) 4.x
- Config: YAML via helidon-config-yaml
- Minimal DI: a small InstanceRegistry (custom) instead of a full container
- Tests: JUnit Jupiter

Design notes

- Virtual threads (Project Loom) underpin Nima; you write straightforward blocking code while maintaining scalability.
- Services are registered explicitly in code. No component scanning. No reflection-heavy magic.
- Services and endpoints are wired through a tiny InstanceRegistry to keep the DI story minimal and explicit.

Notes & caveats

- Targeting JDK 25: ensure your tooling (IDE, Gradle, Docker base image) matches.
- Helidon 4.x API is evolving; this spike aims to stay minimal and explicit to make upgrades simple.

