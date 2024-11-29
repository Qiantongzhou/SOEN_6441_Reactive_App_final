package models;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
public class HttpUrlConnectionClientTest {

    private MockWebServer mockWebServer;
    private HttpUrlConnectionClient httpClient;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        httpClient = new HttpUrlConnectionClient();
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetSuccess() throws Exception {
        // Arrange
        String mockResponseBody = "{\"message\":\"Hello World\"}";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockResponseBody));

        String url = mockWebServer.url("/test").toString();

        // Act
        String response = httpClient.get(url);

        // Assert
        assertNotNull(response);
        assertEquals(mockResponseBody, response);
    }

    @Test
    public void testGetErrorResponse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        String url = mockWebServer.url("/test").toString();

        // Act & Assert
        Exception exception = null;
        try {
            httpClient.get(url);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Failed with HTTP code: 404"));
    }

    @Test
    public void testGetMalformedUrl() {
        // Arrange
        String malformedUrl = "http://";

        // Act & Assert
        Exception exception = null;
        try {
            httpClient.get(malformedUrl);
        } catch (Exception e) {
            exception = e;
        }

        assertNotNull(exception);
        //assertTrue(exception instanceof java.net.MalformedURLException);
    }
}
