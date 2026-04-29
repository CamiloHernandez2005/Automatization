package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Centralized, immutable, thread-safe test configuration loader.
 *
 * <p>Loads {@code config-{env}.properties} from the classpath where {@code env}
 * is taken from system property {@code -Denv=...} (default: {@code dev}).
 * Any value present as a system property takes precedence over the file value
 * — this allows ad-hoc overrides like {@code -Dlogin.password=secret}.
 *
 * <p>Property values may reference external variables with the
 * {@code ${KEY}} or {@code ${KEY:default}} syntax (same convention as
 * {@code application.properties}). These placeholders resolve, in order of
 * precedence: JVM system property &rarr; OS environment variable &rarr;
 * {@code .env} file at the working directory &rarr; the inline default.
 * Unresolved placeholders without a default fail loudly.
 *
 * <p>The {@code .env} loader is independent of {@code spring-dotenv} (which
 * only activates inside a Spring Boot context) — this lets the standalone
 * {@code TestRunner} pick up the same {@code .env} that Spring Boot uses.
 *
 * <p>After loading and merging, the resolved key/value map is exposed through
 * type-safe accessors and also promoted back to system properties so that
 * {@code main}-classpath consumers (e.g. {@code PlaywrightUtils} timeouts)
 * can read them via {@link System#getProperty(String)} without depending on
 * this test class.
 *
 * <p>Loading happens once per JVM in a {@code static} initializer. The
 * resulting map is created with {@link Map#copyOf(Map)} so no mutation is
 * possible after publication, making concurrent reads safe.
 *
 * <p>Required keys that are missing throw {@link IllegalStateException} with
 * a diagnostic message naming the key and the active environment.
 */
public final class TestConfig {

    private static final Logger log = LoggerFactory.getLogger(TestConfig.class);

    private static final String ENV_PROP = "env";
    private static final String DEFAULT_ENV = "dev";

    /** ${KEY} or ${KEY:default}. Default may be empty (e.g. ${KEY:}). */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?\\}");

    /** Override the .env path with -Ddotenv.path=/some/where/.env. */
    private static final String DOTENV_PATH_PROP = "dotenv.path";
    private static final String DEFAULT_DOTENV_FILE = ".env";

    private static final String ACTIVE_ENV;
    private static final String SOURCE_FILE;
    private static final Map<String, String> VALUES;

    static {
        ACTIVE_ENV = System.getProperty(ENV_PROP, DEFAULT_ENV).trim().toLowerCase();
        SOURCE_FILE = "config-" + ACTIVE_ENV + ".properties";

        Map<String, String> dotenv = loadDotEnv();

        Properties fromFile = new Properties();
        try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream(SOURCE_FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "No se encontró el archivo de configuración '" + SOURCE_FILE +
                                "' en el classpath (env='" + ACTIVE_ENV +
                                "'). Verifica src/test/resources/ o pasa -Denv=<dev|staging|prod>.");
            }
            fromFile.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error leyendo '" + SOURCE_FILE + "': " + e.getMessage(), e);
        }

        Map<String, String> merged = new HashMap<>();
        for (String key : fromFile.stringPropertyNames()) {
            String sysOverride = System.getProperty(key);
            String value = sysOverride != null
                    ? sysOverride
                    : resolvePlaceholders(fromFile.getProperty(key), key, dotenv);
            merged.put(key, value);
        }
        VALUES = Map.copyOf(merged);

        for (Map.Entry<String, String> e : VALUES.entrySet()) {
            if (System.getProperty(e.getKey()) == null) {
                System.setProperty(e.getKey(), e.getValue());
            }
        }

        log.info("TestConfig cargado: env='{}', archivo='{}', .env={}, claves={}",
                ACTIVE_ENV, SOURCE_FILE, dotenv.isEmpty() ? "ausente" : dotenv.size() + " entradas",
                new TreeSet<>(VALUES.keySet()));
    }

    /**
     * Reads simple {@code KEY=VALUE} pairs from {@code .env} at the working
     * directory (or the path in {@code -Ddotenv.path=...}). Lines that are
     * blank or start with {@code #} are skipped. Surrounding single/double
     * quotes are stripped. Returns an empty map if the file is absent — the
     * loader is intentionally silent so the test runner works without a
     * {@code .env} when all values come from {@code -D} flags or OS env.
     */
    private static Map<String, String> loadDotEnv() {
        String override = System.getProperty(DOTENV_PATH_PROP);
        Path path = override != null && !override.isBlank()
                ? Paths.get(override)
                : Paths.get(DEFAULT_DOTENV_FILE);

        if (!Files.isRegularFile(path)) {
            return Map.of();
        }

        Map<String, String> map = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(path);
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(key, value);
            }
        } catch (IOException e) {
            log.warn("No se pudo leer .env en '{}': {}", path.toAbsolutePath(), e.getMessage());
            return Map.of();
        }
        return Map.copyOf(map);
    }

    /**
     * Resolves {@code ${KEY}} / {@code ${KEY:default}} placeholders inside
     * {@code value}. Lookup precedence: system property &rarr; OS env var
     * &rarr; .env entry &rarr; inline default. A placeholder that resolves to
     * {@code null} (no source has it AND no default was given) throws
     * {@link IllegalStateException} naming the unresolved key and the
     * property that referenced it.
     */
    private static String resolvePlaceholders(String value, String forKey, Map<String, String> dotenv) {
        if (value == null || value.indexOf("${") < 0) return value;

        Matcher m = PLACEHOLDER.matcher(value);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String var = m.group(1).trim();
            String def = m.group(2); // may be null (no ":") or "" (":")

            String resolved = System.getProperty(var);
            if (resolved == null) resolved = System.getenv(var);
            if (resolved == null) resolved = dotenv.get(var);
            if (resolved == null) resolved = def;

            if (resolved == null) {
                throw new IllegalStateException(
                        "Variable '" + var + "' referenciada por '" + forKey +
                                "' en " + SOURCE_FILE + " no tiene valor. Defínela en .env, " +
                                "como variable de entorno, o pásala con -D" + var + "=<valor>.");
            }
            m.appendReplacement(out, Matcher.quoteReplacement(resolved));
        }
        m.appendTail(out);
        return out.toString();
    }

    private TestConfig() {
        // utility class
    }

    /**
     * Forces eager evaluation of the static initializer. Call this from
     * {@code TestRunner#main} (or a {@code @BeforeAll} in tests) to make sure
     * configuration is loaded — and system properties are promoted — before
     * any other class in the main classpath observes default values.
     */
    public static void init() {
        // touching the class triggers <clinit> if not already run
    }

    public static String activeEnv() {
        return ACTIVE_ENV;
    }

    public static String sourceFile() {
        return SOURCE_FILE;
    }

    /**
     * @throws IllegalStateException if the key is missing.
     */
    public static String getString(String key) {
        String v = VALUES.get(Objects.requireNonNull(key, "key"));
        if (v == null) {
            throw new IllegalStateException(
                    "Clave de configuración requerida no encontrada: '" + key +
                            "' (env='" + ACTIVE_ENV + "', archivo='" + SOURCE_FILE +
                            "'). Defínela o pásala con -D" + key + "=<valor>.");
        }
        return v;
    }

    /**
     * @throws IllegalStateException if the key is missing or not a valid integer.
     */
    public static int getInt(String key) {
        String v = getString(key);
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "La clave '" + key + "' no es un entero válido: '" + v + "'", e);
        }
    }

    /**
     * Parses standard boolean-ish strings: {@code true}/{@code false} (case-insensitive).
     * @throws IllegalStateException if the key is missing.
     */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key).trim());
    }

    public static String getOrDefault(String key, String fallback) {
        Objects.requireNonNull(fallback, "fallback");
        String v = VALUES.get(Objects.requireNonNull(key, "key"));
        return v != null ? v : fallback;
    }

    public static int getOrDefault(String key, int fallback) {
        String v = VALUES.get(Objects.requireNonNull(key, "key"));
        if (v == null) return fallback;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            log.warn("Clave '{}' no es entero ('{}'), usando fallback {}", key, v, fallback);
            return fallback;
        }
    }

    public static boolean getOrDefault(String key, boolean fallback) {
        String v = VALUES.get(Objects.requireNonNull(key, "key"));
        return v != null ? Boolean.parseBoolean(v.trim()) : fallback;
    }
}
