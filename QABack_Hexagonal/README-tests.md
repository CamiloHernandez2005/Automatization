# Tests guide

This project ships **two** equivalent ways to run the e2e (Playwright) test
suite:

1. `mvn test` — Surefire, the conventional path.
2. `TestRunner` — a programmatic JUnit 5 launcher with a `main` method,
   packaged as a self-contained executable JAR. Useful for CI steps that
   pick a specific class/method, or for distributing the runner to a
   machine without Maven.

Both share the same test classes, the same `TestConfig`, and the same
configuration files. Picking one over the other is a runtime decision.

---

## Project layout

```
src/main/java/com/example/demo/Playwright/   # services consumed by tests (and the controller)
src/main/resources/application.properties     # Spring config; secrets via env vars
src/test/java/com/example/demo/
    TestRunner.java                           # programmatic entry point
    config/TestConfig.java                    # immutable, thread-safe config loader
    e2e/                                      # Playwright e2e tests (default scope of TestRunner)
src/test/resources/
    config-dev.properties                     # versioned, placeholders only
    config-{env}.properties.example           # template per environment
src/assembly/tests-jar.xml                    # assembly descriptor for the runner JAR
```

---

## Running tests with `mvn test` (Surefire)

This runs every `*Tests` / `*Test` class Surefire discovers, including
`DemoApplicationTests` (which loads the full Spring context and therefore
needs the database reachable).

```bash
# Active env defaults to 'dev'.
mvn test

# Pick a different env (loads config-staging.properties from test classpath).
mvn test -Denv=staging

# Override an individual property.
mvn test -Dlogin.password=actualPassword

# Run only Playwright e2e tests (skip the Spring context test).
mvn test -Dtest='com.example.demo.e2e.*'
```

`mvn test` also requires the database credentials Spring expects. Either:

```bash
# Linux / macOS
DB_USERNAME=jdtt DB_PASSWORD=*** JWT_SECRET=*** mvn test

# Windows PowerShell
$env:DB_USERNAME="jdtt"; $env:DB_PASSWORD="***"; $env:JWT_SECRET="***"; .\mvnw.cmd test
```

---

## Running tests with the `TestRunner` JAR

### Build

```bash
mvn clean package
# Produces:
#   target/demo-0.0.1-SNAPSHOT.jar                                  # Spring Boot fat-jar (DemoApplication)
#   target/demo-0.0.1-SNAPSHOT-tests-jar-with-dependencies.jar      # TestRunner self-contained JAR
```

> Skip the Spring context test if your DB is not up:
> `mvn -DskipTests package`. Tests will still ship inside the runner JAR
> and execute when invoked via `java -jar`.

### Run

```bash
# 1) All tests under com.example.demo.e2e (default).
java -jar target/demo-0.0.1-SNAPSHOT-tests-jar-with-dependencies.jar

# 2) A specific class — simple name (resolved against com.example.demo.e2e)
#    or fully-qualified.
java -jar target/demo-0.0.1-SNAPSHOT-tests-jar-with-dependencies.jar LoginTest
java -jar target/demo-0.0.1-SNAPSHOT-tests-jar-with-dependencies.jar com.example.demo.e2e.LoginTest

# 3) A specific method.
java -jar target/demo-0.0.1-SNAPSHOT-tests-jar-with-dependencies.jar LoginTest#loginConCredencialesValidas

# 4) An arbitrary package (use the pkg: prefix).
java -jar target/demo-0.0.1-SNAPSHOT-tests-jar-with-dependencies.jar pkg:com.example.demo.e2e.flows

# 5) Switch environment + override credentials at runtime.
java -Denv=staging -Dlogin.password=secret \
     -jar target/demo-0.0.1-SNAPSHOT-tests-jar-with-dependencies.jar LoginTest

# 6) Run with a visible browser (instead of headless).
java -Dplaywright.headed=true \
     -jar target/demo-0.0.1-SNAPSHOT-tests-jar-with-dependencies.jar
```

The runner exits with status `0` if every test passes (or none was
discovered), `1` otherwise. Failure traces go to `stderr`; the human-
readable summary goes to `stdout`.

---

## Running `TestRunner` without packaging (development)

Faster feedback during development: skips the assembly build, compiles
test sources, executes via `exec:java` against the test classpath.

```bash
mvn test-compile
mvn exec:java -Dexec.mainClass=com.example.demo.TestRunner -Dexec.classpathScope=test

# With arguments:
mvn exec:java -Dexec.mainClass=com.example.demo.TestRunner \
              -Dexec.classpathScope=test \
              -Dexec.args="LoginTest#loginConCredencialesValidas"

# With env switching:
mvn exec:java -Dexec.mainClass=com.example.demo.TestRunner \
              -Dexec.classpathScope=test \
              -Denv=staging \
              -Dexec.args="LoginTest"
```

---

## Configuration: `config-{env}.properties`

`TestConfig` (in `src/test/java/com/example/demo/config/TestConfig.java`)
is the single entry point for environment-specific values.

### Selection
- Active environment comes from `-Denv=<dev|staging|prod>` (default: `dev`).
- The file `config-<env>.properties` must exist on the test classpath.

### Override precedence (highest first)
1. `-Dkey=value` system properties on the JVM.
2. The value in `config-<env>.properties`.

### Failure modes
- File missing on classpath → `IllegalStateException` with a clear message.
- Required key missing → `IllegalStateException` naming the key and the
  active env (use `getOrDefault` if a key is optional).

### Promoting values to `System` properties
Loaded values are pushed back to system properties at class-init time.
This is how `PlaywrightUtils` (in `src/main/java`) reads its timeouts
without depending on the test classpath.

### Adding a new key

1. Add the key to **all** `config-{env}.properties.example` files.
2. Add a sensible value (or `CHANGEME` placeholder) to
   `src/test/resources/config-dev.properties`.
3. Read it from a test or service via `TestConfig.getString("my.key")`,
   `TestConfig.getInt("my.key")`, `TestConfig.getBoolean("my.key")`, or
   `TestConfig.getOrDefault("my.key", fallback)`.
4. Document it in this README if it affects how tests are run.

### Current keys

| Key | Type | Used by | Notes |
|---|---|---|---|
| `app.baseUrl` | string | `BasePlaywrightTest`, e2e flows | Login URL of the system under test |
| `app.smokeUrl` | string | `PlaywrightUrlTest` | Root URL for smoke navigation |
| `login.username` | string | `LoginTest` | Test user |
| `login.password` | string | `LoginTest` | Test password — never commit real values |
| `playwright.headed` | boolean | `BasePlaywrightTest`, `PlaywrightUtils#createBrowser` | `true` opens a visible browser |
| `playwright.timeout.short.ms` | long | `PlaywrightUtils#shortTimeout` | ms |
| `playwright.timeout.default.ms` | long | `PlaywrightUtils#defaultTimeout` | ms |
| `playwright.timeout.long.ms` | long | `PlaywrightUtils#longTimeout` | ms |

---

## Secrets handling

- `application.properties` no longer carries real DB / JWT credentials.
  Values come from environment variables: `DB_USERNAME`, `DB_PASSWORD`,
  `JWT_SECRET`, optionally `JWT_EXPIRATION`. See
  `src/main/resources/application.properties.example`.
- `config-dev.properties` is versioned with **placeholders** only.
  Override `login.password` with `-Dlogin.password=...` at runtime.
- `config-{env}.properties.example` files are templates. Real per-env
  files (e.g. `config-staging-local.properties`) are gitignored — see
  `.gitignore`.

---

## Coexistence with the HTTP controller

`POST /test/sp` (defined in `TestController`) is unchanged and still
works when the Spring app is running. The runner does **not** start a
Spring context: it instantiates services directly when needed, and reads
the URL from `TestConfig` instead of resolving it from the database.

---

## Risks / assumptions

1. **`SalesPortalService` was given a second constructor** that allows
   running without `ComponentRegionRepositoryPort`. Calling
   `runSalesPortalTest(TestDTO)` on an instance built that way will
   throw — only `runSalesPortalTest(TestDTO, String url)` is valid in
   the runner path. The HTTP path (Spring-built bean) keeps the original
   behavior.
2. **`PlaywrightUtils` timeouts** were converted from `public static
   final Duration` constants to instance methods reading
   `System` properties. Callers updated. Defaults match previous
   hardcoded values.
3. **`PlaywrightUrlTest` was moved** from `com.example.demo` to
   `com.example.demo.e2e` so the runner discovers it by default.
4. **`DemoApplicationTests.contextLoads()`** still requires the database
   and JWT secret. It will fail under `mvn test` without the env vars.
   This was the case before the refactor; it has not been changed.
5. **`PCTerminalService`** was kept as-is (fully commented out).
   `TestController` was kept too — the `/test/pct` endpoint stays
   commented, `/test/sp` still works.
6. **Assembly JAR is large (~230 MB)** because it bundles Spring Boot,
   Hibernate, Playwright, JUnit Platform and the SQL Server driver.
   This is the price of a single self-contained artifact. If size
   becomes a problem, switch to a slimmer assembly that excludes
   transitive Spring dependencies, or ship a thin runner with
   `Class-Path` references.
7. **`config-dev.properties` is committed** with placeholder credentials
   (`CHANGEME`). Tests that need real credentials must override via
   `-Dlogin.password=...` or by creating a gitignored
   `config-dev-local.properties` (current `TestConfig` does not chain
   files; `-D` flags are the supported override path).
