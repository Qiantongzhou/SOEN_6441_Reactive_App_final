package controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import static org.junit.jupiter.api.Assertions.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/**
 * @author Tomas Pereira
 * Integration testing for the search controller.
 * Uses the Play framework's testing branch to make fake HTTP requests.
 */
public class SearchControllerIntegrationTest extends WithApplication {

    /**
     * @author Tomas Pereira
     *
     * Starts the application before each test.
     */
    @BeforeEach
    public void init() {
        app = new GuiceApplicationBuilder().build();
    }

    /**
     * @author Tomas Pereira
     * Test for the landing page which includes only the Welcome message and search bar.
     * By checking for "Welcome to YT Lytics", we know that we are one of the pages.
     * By then checking that "Search Terms:" is not present, we know that it is the landing page, since no search is made yet.
     */
    @Test
    public void testIndex(){
        Http.RequestBuilder request = Helpers.fakeRequest().method(GET).uri("/");

        Result result = route(app, request);

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Welcome to YT Lytics"));
        assertFalse(contentAsString(result).contains("Search Terms:"));
    }

    /**
     * @author Tomas Pereira
     * Test for the result of a search query, which must show the search terms used.
     * By then checking that "Search Terms:" is present, we know that it is the result of a search.
     */
    @Test
    public void testSearchVideos() {
        Http.RequestBuilder request = Helpers.fakeRequest().method(GET).uri("/search/searchVideos?query=test");

        Result result = route(app, request);

        assertEquals(OK, result.status());
        // "Search Terms:" will not be present unless a search has been made
        assertTrue(contentAsString(result).contains("Search Terms:"));
    }

    /**
     * @author Tomas Pereira
     * Test for the More Stats page.
     * By then checking that "Word Statistics for" is present, we know that we are on the correct page.
     */
    @Test
    public void testMoreStats(){
        Http.RequestBuilder request = Helpers.fakeRequest().method(GET).uri("/search/MoreStats?query=test");

        Result result = route(app, request);

        assertEquals(OK, result.status());
        // "Word Statistics for" will not be present unless we are looking at More Stats
        assertTrue(contentAsString(result).contains("Word Statistics for"));
    }

    /**
     * @author Sam Collin
     * Tests for displaying Channel Page.
     * This test is more like an integration test. It validates that the route,
     * the controller and the view works together by sending a fake http request
     * invoking the controller and finally checks if the HTML render contains generic
     * info that should be on.
     */
    @Test
    public void testShowChannelProfile() {

        String channelId = "UCW4l6dIY-aEew8xv3baZVGA";
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/channel/" + channelId);

        Result result = route(app, request);

        assertEquals(OK, result.status(), "The response status should be 200 for a valid channel.");
        String content = contentAsString(result);

        // Validate content contains expected details about the channel
        assertTrue(content.contains("Channel Profile"), "The response should contain the channel title.");
        assertTrue(content.contains("Latest Videos"), "The response should contain the video title.");
    }

    /**
     * @author Tomas Pereira
     * Stops the application after each test.
     */
    @AfterEach
    public void tearDown(){
        app.asScala().stop();
    }


}
