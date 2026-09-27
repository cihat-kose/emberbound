# Presenting Emberbound

Position this as a finished Java application with a playable domain and visible engineering decisions. Lead with what someone can run, then show how the code makes the behavior dependable.

## Repository description

> A turn-based survival adventure in Java 21. UI-independent game rules, deterministic combat, validated saves, JUnit tests and cross-platform CI. Playable in the terminal.

Suggested GitHub topics: `java`, `java21`, `game`, `cli`, `oop`, `junit5`, `maven`, `software-design`.

The game's name and artifact are **Emberbound**, and the package root is `dev.emberbound`. The repository currently retains its original `java-adventure-game` URL. If renamed on GitHub later, update repository links and the CI badge in README and `pom.xml` together.

## Project summary for a CV or portfolio

**English**

> Built Emberbound, a complete turn-based survival game in Java 21. Separated combat and progression rules from the terminal interface, implemented versioned saves with defensive validation and safe file replacement, and added automated regression tests, coverage checks and Windows/Linux CI configuration.

**Türkçe**

> Java 21 ile oynanabilir bir sıra tabanlı hayatta kalma oyunu geliştirdim. Savaş ve ilerleme kurallarını terminal arayüzünden ayırdım; sürümlü kayıt, veri doğrulama ve güvenli dosya yazma ekledim. Hata senaryolarını ve oyun akışını otomatik testlerle doğrulayıp kapsam kontrolü ve Windows/Linux CI yapılandırması hazırladım.

Use only the parts you can explain and defend in a technical conversation. Add the current test count or coverage percentage only after running `verify` and checking its report; those figures can change with the code.

## A short reviewer walkthrough

1. Run `play.cmd --demo` on Windows or `sh play.sh --demo` elsewhere. The real console/engine completes the island and reaches the ending without needing manual input.
2. Open `engine/Encounter.java`: show attack ordering, typed results and the rule that a defeated enemy cannot counterattack.
3. Open `domain/Player.java`: show validation before a purchase mutates either balance or equipment.
4. Open `persistence/FileSaveStore.java` and its tests: show how invalid files are rejected and a failed replacement leaves the existing target intact.
5. Run `verify`, open the coverage report, and show the full campaign test across all three characters and 100 seeds per character.

## Useful design discussion

- Why character variations are immutable enum data instead of subclasses with identical behavior.
- Why console input belongs outside domain objects.
- Why a kill persists after retreat, but partial damage to the current enemy does not.
- What an atomic file replacement guarantees, what the fallback does not guarantee, and why this is not a concurrent database.
- How typed commands, immutable response DTOs and a session service would support a future frontend.
- Why no framework or database is needed for the current offline game.

Do not present the project as a deployed backend service, a multiplayer game, or a finished browser game. Those are possible extensions. The current evidence is the executable game, source, tests, documentation and any published CI runs.

## Publishing checklist

- Run `verify` and the packaged `--demo` locally.
- Review the changes, then commit and push to GitHub.
- Check that the first Windows and Linux workflow runs pass and artifacts are downloadable.
- Update the repository description/topics and pin the repository on your profile if desired.
- For a future release, attach the verified `emberbound.jar` and state the Java 21 requirement.

Documentation and CI configuration in this working tree do not by themselves publish a release or change repository metadata.
