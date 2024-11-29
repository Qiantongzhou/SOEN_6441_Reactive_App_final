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


    @AfterEach
    public void tearDown(){
        app.asScala().stop();
    }


}
