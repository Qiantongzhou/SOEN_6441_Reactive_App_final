package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.SearchHistoryModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.http.websocket.Message;
import play.libs.F;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.test.Helpers;
import play.test.TestServer;
import play.test.WithApplication;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import akka.stream.javadsl.Flow;
import okhttp3.*;
import okhttp3.WebSocketListener;
import okhttp3.Response;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class SearchControllerTest extends WithApplication {

    private ActorSystem system;
    private Materializer materializer;
    private SearchHistoryModel mockSearchHistoryModel;

    @Before
    public void setup() {
        // Obtain the ActorSystem and Materializer from the application injector
        system = app.injector().instanceOf(ActorSystem.class);
        materializer = app.injector().instanceOf(Materializer.class);

        // Mock the SearchHistoryModel
        mockSearchHistoryModel = Mockito.mock(SearchHistoryModel.class);
    }

    @After
    public void teardown() {
        // If necessary, perform cleanup
    }

    @Test
    public void testIndex() {
        // Arrange
        SearchController controller = new SearchController(system, materializer, mockSearchHistoryModel);

        // Act
        Result result = controller.index();

        // Assert
        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Search"));
        // Additional assertions can be added based on the content of index.scala.html
    }

    @Test
    public void testSearchSocketCreation() throws Exception {
        // Arrange
        SearchController controller = new SearchController(system, materializer, mockSearchHistoryModel);

        // Create a fake HTTP request
        Http.Request request = new Http.RequestBuilder()
                .method("GET")
                .uri("/searchSocket")
                .build();

        // Act
        WebSocket webSocket = controller.searchSocket();

        // Assert
        assertNotNull(webSocket);

        // Simulate accepting the WebSocket
        CompletionStage<F.Either<Result, Flow<Message, Message, ?>>> flowFuture = webSocket.apply(request);
        assertNotNull(flowFuture);

        // Get the flow
        F.Either<Result, Flow<Message, Message, ?>> flow = flowFuture.toCompletableFuture().get(5, TimeUnit.SECONDS);
        assertNotNull(flow);
    }


}
