package com.budget.qa.runners;

import org.junit.jupiter.api.Disabled;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * JUnit 5 Suite Runner for Serenity BDD + Cucumber.
 *
 * <p>This runner discovers and executes all {@code .feature} files under
 * {@code src/test/resources/features/} and maps them to step definitions
 * in {@code com.budget.qa.stepdefinitions}.</p>
 *
 * <p><strong>Execution:</strong></p>
 * <pre>
 *   ./gradlew test                         # Run all tests
 *   ./gradlew test --tests "*Transaction*" # Run only transaction features
 *   ./gradlew test aggregate               # Run all + generate Serenity report
 * </pre>
 *
 * <p><strong>Serenity Report:</strong> After execution, open
 * {@code target/site/serenity/index.html} for the full living documentation.</p>
 *
 * <p><strong>NOTE:</strong> Disabled until Cucumber step definitions are fully
 * implemented. Running this suite without glue code causes
 * {@code UndefinedStepException}. Re-enable by removing {@code @Disabled}.</p>
 */
@Disabled("Cucumber step definitions not yet implemented — remove when glue code is ready")
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.budget.qa.stepdefinitions")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "io.cucumber.core.plugin.SerenityReporterParallel")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
public class CucumberTestSuite {
    // Marker class — configuration is entirely annotation-driven
}
