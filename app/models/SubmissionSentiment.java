package models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class SubmissionSentiment {

    /**
     * @author Tomas Pereira
     *
     * Constructor for the SubmissionSentiment Object.
     */
    public SubmissionSentiment(){}

    /**
     * @author Tomas Pereira
     *
     * Given a path to a file containing a list of words, reads the file and stores each of the words in a HashSet
     * to be used during sentiment analysis.
     *
     * @param path String path to the file.
     * @return Hashset containing each of the words in the input file
     */
    public Set<String> loadSentimentFile(String path){
        List<String> readLines = null;
        try{
            readLines = Files.readAllLines(Paths.get(path));
        }
        catch (IOException e){
            System.err.println("Unable to Read Sentiment File: " + e);
        }
        if (readLines != null)
            return new HashSet<>(readLines);
        else return null;
    }

    /**
     * @author Tomas Pereira
     *
     * Determines the sentiment of the description for a single video.
     * Using a stream, counts the occurence of each word in the description in both of the sets.
     * Using this count, returns a happy or sad sentiment if 70% of the words occur in that given set.
     * Othewise returns a neutral sentiment.
     *
     * @param description The video description being processed.
     * @param happySet The set of words indicating happy sentiment.
     * @param sadSet The set of words indicating sad sentiment.
     * @return Either :-) :-| or :-( based on the sentiment for the given description.
     */
    public static String singleSentiment(String description, Set<String> happySet, Set<String> sadSet){
        List <String> descWords = Arrays.asList(description.split(" "));
        int totalWords = descWords.size();

        long countHappy = descWords.stream().filter(happySet::contains).count();
        long countSad = descWords.stream().filter(sadSet::contains).count();

        if ((double) countHappy/totalWords >= 0.7)
            return ":-)";
        else if ((double) countSad/totalWords >= 0.7)
            return ":-(";
        else
            return ":-|";
    }

    /**
     * @author Tomas Pereira
     *
     * Streams a list of videos to get the most common sentiment among them.
     * Uses a stream to map each video to its description and collect the count for each sentiment.
     * Then uses another stream to process these sentiment counts and return the one with the highest occurence.
     *
     * @param videos List of videos to be processed
     * @return Either :-) :-| or :-( based on the sentiment for the whole set of videos.
     */
    public String determineSentiment(List<Video> videos){

        Set<String> happySet = loadSentimentFile("public/SentimentWords/HappyStrings.txt");
        Set<String> sadSet = loadSentimentFile("public/SentimentWords/SadStrings.txt");

        Map<String, Long> sentimentCount = videos.stream()
                .map(Video::getDescription)
                .map(desc -> singleSentiment(desc, happySet, sadSet))
                .collect(Collectors.groupingBy(sentiment -> sentiment, Collectors.counting()));

        // Gets the sentiment which appears the most from the set
        return sentimentCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(":-|");
    }

}
