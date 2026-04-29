package com.example.demo;

import com.example.demo.config.TestConfig;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.List;

/**
 * Programmatic JUnit 5 test runner.
 *
 * <p>Discovers and executes tests via JUnit Platform Launcher. Designed to
 * coexist with {@code mvn test} (Surefire) — this runner is an additional
 * entry point, not a replacement.
 *
 * <h2>Usage</h2>
 * <pre>
 *   # All tests in the default package (com.example.demo.e2e)
 *   java -jar target/&lt;artifact&gt;-tests-jar-with-dependencies.jar
 *
 *   # A single test class (FQN or simple name resolved against the default package)
 *   java -jar ...-tests-jar-with-dependencies.jar LoginTest
 *   java -jar ...-tests-jar-with-dependencies.jar com.example.demo.e2e.LoginTest
 *
 *   # A single test method
 *   java -jar ...-tests-jar-with-dependencies.jar LoginTest#loginConCredencialesValidas
 *
 *   # An arbitrary package
 *   java -jar ...-tests-jar-with-dependencies.jar pkg:com.example.demo.e2e.flows
 *
 *   # Switch environment + override a value
 *   java -Denv=staging -Dlogin.password=secret -jar ...-tests-jar-with-dependencies.jar LoginTest
 * </pre>
 *
 * <p>Exit codes: {@code 0} when every test passes (or no test was found),
 * {@code 1} otherwise — suitable for CI step status.
 */
public final class TestRunner {

    private static final Logger log = LoggerFactory.getLogger(TestRunner.class);

    static final String DEFAULT_PACKAGE = "com.example.demo.e2e";
    private static final String PACKAGE_PREFIX = "pkg:";

    private TestRunner() {
        // entry point only
    }

    public static void main(String[] args) {
        // Eagerly load configuration so any subsequent class load (PlaywrightUtils, etc.)
        // observes the resolved system properties.
        TestConfig.init();
        log.info("TestRunner iniciado: env='{}', archivo='{}'",
                TestConfig.activeEnv(), TestConfig.sourceFile());

        LauncherDiscoveryRequest request = buildRequest(args);
        Launcher launcher = LauncherFactory.create();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        printSummary(summary);

        System.exit(summary.getTotalFailureCount() == 0 ? 0 : 1);
    }

    static LauncherDiscoveryRequest buildRequest(String[] args) {
        LauncherDiscoveryRequestBuilder builder = LauncherDiscoveryRequestBuilder.request();

        if (args == null || args.length == 0 || allBlank(args)) {
            builder.selectors(DiscoverySelectors.selectPackage(DEFAULT_PACKAGE));
            log.info("Selector por defecto: paquete '{}'", DEFAULT_PACKAGE);
            return builder.build();
        }

        for (String raw : args) {
            String arg = raw == null ? "" : raw.trim();
            if (arg.isEmpty()) continue;

            if (arg.startsWith(PACKAGE_PREFIX)) {
                String pkg = arg.substring(PACKAGE_PREFIX.length()).trim();
                builder.selectors(DiscoverySelectors.selectPackage(pkg));
                log.info("Selector: paquete '{}'", pkg);
            } else if (arg.contains("#")) {
                String[] parts = arg.split("#", 2);
                String fqn = qualify(parts[0].trim());
                String method = parts[1].trim();
                builder.selectors(DiscoverySelectors.selectMethod(fqn, method));
                log.info("Selector: método '{}#{}'", fqn, method);
            } else {
                String fqn = qualify(arg);
                builder.selectors(DiscoverySelectors.selectClass(fqn));
                log.info("Selector: clase '{}'", fqn);
            }
        }

        return builder.build();
    }

    private static boolean allBlank(String[] args) {
        for (String a : args) {
            if (a != null && !a.trim().isEmpty()) return false;
        }
        return true;
    }

    private static String qualify(String name) {
        return name.contains(".") ? name : DEFAULT_PACKAGE + "." + name;
    }

    private static void printSummary(TestExecutionSummary summary) {
        long total   = summary.getTestsFoundCount();
        long started = summary.getTestsStartedCount();
        long success = summary.getTestsSucceededCount();
        long skipped = summary.getTestsSkippedCount();
        long failed  = summary.getTotalFailureCount();
        long durMs   = summary.getTimeFinished() - summary.getTimeStarted();

        // Final summary on stdout is intentional: this is the runner's
        // user-facing report (and CI log artifact). The rest of the codebase
        // uses SLF4J/Logback as required.
        PrintWriter out = new PrintWriter(System.out, true);
        out.println();
        out.println("==================== TEST RUNNER SUMMARY ====================");
        out.printf ("  Found      : %d%n", total);
        out.printf ("  Started    : %d%n", started);
        out.printf ("  Succeeded  : %d%n", success);
        out.printf ("  Skipped    : %d%n", skipped);
        out.printf ("  Failed     : %d%n", failed);
        out.printf ("  Duration   : %d ms%n", durMs);

        List<TestExecutionSummary.Failure> failures = summary.getFailures();
        if (!failures.isEmpty()) {
            out.println("------------------------- FAILURES --------------------------");
            for (TestExecutionSummary.Failure f : failures) {
                out.printf("  X %s%n", f.getTestIdentifier().getDisplayName());
                out.printf("    %s%n", f.getException().toString());
            }
            // Full stack traces to stderr for CI logs.
            summary.printFailuresTo(new PrintWriter(System.err, true));
        }
        out.println("=============================================================");
    }
}
