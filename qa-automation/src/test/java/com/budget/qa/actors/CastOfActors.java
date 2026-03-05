package com.budget.qa.actors;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

/**
 * Factory for creating pre-configured Actors in the Screenplay pattern.
 *
 * <p>Each Actor represents a user persona with specific abilities:</p>
 * <ul>
 *     <li>{@code apiUser} — Can call REST APIs (Transaction, Report, Auth)</li>
 *     <li>{@code webUser} — Can browse the web frontend</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 *   Actor user = CastOfActors.apiUser("Alice");
 *   user.attemptsTo(
 *       Authenticate.withCredentials("alice@test.com", "password")
 *   );
 * }</pre>
 */
public final class CastOfActors {

    private CastOfActors() {
        // Utility class — prevent instantiation
    }

    /**
     * Creates an Actor with the ability to call REST APIs.
     *
     * @param name the actor's display name (used in Serenity reports)
     * @param baseUrl the base URL for the target microservice
     * @return a configured Actor
     */
    public static Actor apiUser(String name, String baseUrl) {
        return Actor.named(name)
                .whoCan(CallAnApi.at(baseUrl));
    }

    /**
     * Creates an Actor with the ability to browse the web frontend.
     *
     * @param name the actor's display name
     * @return a configured Actor (WebDriver injected by Serenity lifecycle)
     */
    public static Actor webUser(String name) {
        return Actor.named(name);
        // Note: BrowseTheWeb ability is injected automatically by Serenity
        // when using @Managed WebDriver in test classes, or explicitly:
        // .whoCan(BrowseTheWeb.with(driver))
    }
}
