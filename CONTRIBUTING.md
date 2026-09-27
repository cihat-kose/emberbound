# Contributing to Emberbound

Use JDK 21 or newer and the included Maven Wrapper. Import `pom.xml` into your IDE; the source root is `src/main/java` and the entry point is `dev.emberbound.Main`.

## Local workflow

```sh
sh ./mvnw spotless:apply
sh ./mvnw verify
java -jar target/emberbound.jar --demo
```

On Windows replace `sh ./mvnw` with `.\mvnw.cmd`. If the shell's `java` points to an older installation, use the Java executable under `JAVA_HOME` or `play.cmd --demo`.

## Making a change

- Keep terminal output and file access out of `domain` and `engine`.
- Add a behavior-focused regression test when changing combat, progression, economy or persistence. Use a fixed seed or explicit region populations.
- Do not expose or depend on mutable internal progress maps.
- Preserve the single-reader input model and graceful EOF behavior.
- Treat save schema changes as compatibility changes: update the version and document migration or rejection behavior.
- Keep gameplay documentation synchronized with balance and control changes.
- Do not commit `target/`, IDE files or personal saves.

The format is enforced by Spotless using Google Java Format's AOSP style. `verify` also enforces an 80% line-coverage floor and fails on compiler warnings. Test reports are under `target/surefire-reports`, with coverage at `target/site/jacoco/index.html`.

Describe the observable behavior and the relevant validation in pull requests. For gameplay bugs, include your character, equipment, seed, actions and whether you loaded a save.
