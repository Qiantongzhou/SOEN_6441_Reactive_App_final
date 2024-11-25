package models;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChannelTest {

    /**
     * @author Sam Collin
     * Tests the constructor and all the getters of the Channel class.
     */
    @Test
    public void testChannelClass(){

        // Let's create a mock Channel object
        Channel testChannel = new Channel(
                "999",
                "Sam Collin",
                "This is the description of my test channel",
                "08-11-2024",
                "France",
                "https://www.youtube.com/@ByFallenKing",
                "https://yt3.googleusercontent.com/1HWwbVicCzBqd4ZywZvAEpqu5bzCo9fL7qp8iZclcM-vfX_Y8LUM3xX7P2or2cRgwSg0a7AOa7U=s160-c-k-c0x00ffffff-no-rj",
                18900,
                false,
                100000,
                25

        );
        // Verify each assigned value with the getters
        assertEquals("999", testChannel.getId(), "Getter does not retrieve the initial value");
        assertEquals("Sam Collin", testChannel.getTitle(), "Getter does not retrieve the initial value");
        assertEquals("This is the description of my test channel", testChannel.getDescription(), "Getter does not retrieve the initial value");
        assertEquals("08-11-2024", testChannel.getPublishedAt(), "Getter does not retrieve the initial value");
        assertEquals("France", testChannel.getCountry(), "Getter does not retrieve the initial value");
        assertEquals("https://www.youtube.com/@ByFallenKing", testChannel.getCustomUrl(), "Getter does not retrieve the initial value");
        assertEquals("https://yt3.googleusercontent.com/1HWwbVicCzBqd4ZywZvAEpqu5bzCo9fL7qp8iZclcM-vfX_Y8LUM3xX7P2or2cRgwSg0a7AOa7U=s160-c-k-c0x00ffffff-no-rj", testChannel.getThumbnailUrl(), "Getter does not retrieve the initial value");
        assertEquals(18900, testChannel.getSubscriberCount(), "Getter does not retrieve the initial value");
        assertFalse(testChannel.getHiddenSubscriberCount(), "The subscriber visibility status is not the one set initially");
        assertEquals(100000, testChannel.getViewCount(), "Getter does not retrieve the initial value");
        assertEquals(25, testChannel.getVideoCount(), "Getter does not retrieve the initial value");

    }
}
