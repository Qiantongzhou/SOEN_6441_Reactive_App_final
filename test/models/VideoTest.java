package models;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Tomas Pereira
 *
 * Testing for the video class
 */
public class VideoTest {

    private Video testVideo;

    @Before
    public void init(){
        testVideo = new Video("V123124", "TestVideoTitle", "C123412",
                "TestVideoChannel", "TestVideoDesc", "https://fakeurl.com/thumbnail.jpg");
    }

    /**
     * @author Tomas Pereira
     * Test Getter for Video ID
     */
    @Test
    public void testGetVidID(){
        assertEquals(testVideo.getVideoId(), "V123124");
    }

    /**
     * @author Tomas Pereira
     * Test Getter for Video Title
     */
    @Test
    public void testGetTitle(){
        assertEquals(testVideo.getTitle(), "TestVideoTitle");
    }

    /**
     * @author Tomas Pereira
     * Test Getter for Channel ID
     */
    @Test
    public void testGetChannelID(){
        assertEquals(testVideo.getChannelId(), "C123412");
    }

    /**
     * @author Tomas Pereira
     * Test Getter for Channel Title
     */
    @Test
    public void testGetChannelTitle(){
        assertEquals(testVideo.getChannelTitle(), "TestVideoChannel");
    }

    /**
     * @author Tomas Pereira
     * Test Getter for Video Description
     */
    @Test
    public void testGetDescription(){
        assertEquals(testVideo.getDescription(), "TestVideoDesc");
    }

    /**
     * @author Tomas Pereira
     * Test Getter for Thumbnail
     */
    @Test
    public void testGetThumbnail(){
        assertEquals(testVideo.getThumbnail(), "https://fakeurl.com/thumbnail.jpg");
    }

}
