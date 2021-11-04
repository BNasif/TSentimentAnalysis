import java.io.*;
import java.util.*;
import java.util.Scanner;
import java.util.StringTokenizer;

import twitter4j.*;

public class AnalyzeTweets {
    private HashMap<String, Double> wordSentiHashMap; //Words to Sentiment HashMap
    private HashMap<Status, Double> tweetsHashMap; //Tweets to Sentiment HashMap
    private List<Status> tweetsList; //All tweets


    public AnalyzeTweets() {
        tweetsList = new ArrayList<Status>();
        tweetsHashMap = new HashMap<>();
        wordSentiHashMap = new HashMap<>();
    }

    public void loadTweets(String filename) throws IOException, ClassNotFoundException {
        loadTweets(filename, this.tweetsList);
    }

    /**
     * Loads the tweets from a file to the arraylist where it can be accessed later
     *
     * @param filename   file where all tweets are stored
     * @param tweetsList arraylist where we will load the tweets
     * @throws IOException
     * @throws ClassNotFoundException
     */


    private void loadTweets(String filename, List<Status> tweetsList) throws IOException, ClassNotFoundException {
        try {
            FileInputStream fos = new FileInputStream(filename);
            BufferedInputStream bos = new BufferedInputStream(fos);
            ObjectInputStream input = new ObjectInputStream(bos);
            Status temp = null;
            while ((temp = (Status) input.readObject()) != null) {
                tweetsList.add(temp);
            }
            fos.close();
            bos.close();
            input.close();
        } catch (EOFException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * @param filename Load the data of words to sentiment values from text file to a HashMap
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadHash(String filename) throws IOException, ClassNotFoundException {
        loadHash(filename, this.wordSentiHashMap);
    }

    private void loadHash(String filename, HashMap<String, Double> TweetMaps) throws IOException, ClassNotFoundException {
        Scanner scanner = new Scanner(new File(filename));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            StringTokenizer word = new StringTokenizer(line, "#\t"); //words (key)
            String keyString = word.nextToken();
            Double value = Double.parseDouble(word.nextToken("#arnv\t")); //sentiment value (value)
            TweetMaps.put(keyString, value);
        }
    }

    public void loadTweetsForSen() {
        loadTweetsForSen(this.tweetsList, this.wordSentiHashMap, this.tweetsHashMap);
    }

    /**
     * Loads the tweets to their total sentiment value into a HashMap
     *
     * @param tweetsList       Status tweets
     * @param wordSentiHashMap HashMap where the dictionary of words to their sentiment values are stored
     * @param tweetsHashMap    HashMap where we will put in the Status tweets to their total sentiment values
     */
    private void loadTweetsForSen(List<Status> tweetsList, HashMap<String, Double> wordSentiHashMap, HashMap<Status, Double> tweetsHashMap) {
        for (Status temp : tweetsList) { //Looping through the Tweets Arraylist
            String tweet = temp.getText();
            double key = sValueOfTweet(tweet, wordSentiHashMap); //the key of the Hashmap is average sentiment value of the tweet
            tweetsHashMap.put(temp, key);
        }
    }

    /**
     * Calculates the average sentiment value of a tweet, negation is also taken care of in this method
     * The average value is calculated by dividing the total sum of the sentiments to the number of words of sentiment used
     * @param tweet the text of the tweet that is to be analyzed
     * @param wordSentiHashMap HashMap where the dictionary of words to their sentiment values are stored
     * @return returns the average sentiment value of the whole text of the tweet
     */

    private double sValueOfTweet(String tweet, HashMap<String, Double> wordSentiHashMap) { //break down words of the string in tweets status and add their sentiment values together
        double avgSenValue = 0;
        double negation = 0;
        double fiNegation = 0;
        double senValue = 0;
        int numberOfSentiWords = 0;
        StringTokenizer words = new StringTokenizer(tweet, "# \n!.:,?;/><'@%^&*()"); //breaks down sentences into words
        while (words.hasMoreTokens()) { //looping through each word of a tweet
            String word = words.nextToken();
            if (wordSentiHashMap.containsKey(word)) { //when the HashMapList has the word, this is O(1) so it is very quick
                numberOfSentiWords++; //add the counter everytime sentiment value word is used
                if (word.equals("not") && words.hasMoreTokens()) { //for negation and when the sentence has next word
                    String firstNegation = words.nextToken();
                    if (wordSentiHashMap.get(firstNegation) != null) //negation
                        negation = wordSentiHashMap.get(firstNegation) * -1;
                    if (words.hasMoreTokens()) {
                        String nextNegation = words.nextToken();//negation
                        if (wordSentiHashMap.containsKey(nextNegation))
                            fiNegation = wordSentiHashMap.get(nextNegation) * -1;
                        negation += fiNegation;
                    }
                    senValue += negation;
                } else {
                    senValue += wordSentiHashMap.get(word);
                }
            }
        }
        if (numberOfSentiWords == 0) return 0;
        avgSenValue = senValue / numberOfSentiWords;
        return avgSenValue;
    }

    public void printWordSentiHashMap() {
        System.out.println(Arrays.asList(this.wordSentiHashMap));
    }

    public void printTweetsHashmap() {
        System.out.println(Arrays.asList(this.tweetsHashMap));
    }

    public void printTweets() {
        for (int i = 0; i < this.tweetsList.size(); i++) {
            System.out.println(tweetsList.get(i).getText());
        }
    }

    /**
     * Calculates the average of all the sentiment values
     * @param tweetsHashMap HashMap where the tweets are stored
     * @return returns the average of all the sentiment values
     */

    public double calcAvg(HashMap<Status, Double> tweetsHashMap) {
        double totalSenSum = 0;
        for (Map.Entry<Status, Double> temp : tweetsHashMap.entrySet()) {
            totalSenSum += temp.getValue();
        }
        double avgSenVal = totalSenSum / tweetsHashMap.size();
        return avgSenVal;
    }

    /**
     * Calulates the Standard Deviation of all the sentiment values
     * @param tweetsHashMap HashMap where tweets are stored
     * @return returns the Standard Deviation of the sentiment values
     */

    public double calcSD(HashMap<Status, Double> tweetsHashMap) {
        double sum = 0;
        double SquaredDifference = 0;
        for (Map.Entry<Status, Double> temp : tweetsHashMap.entrySet()) {
            sum += temp.getValue();
        }
        double mean = sum / tweetsHashMap.size();
        for (Map.Entry<Status, Double> temp : tweetsHashMap.entrySet()) {
            SquaredDifference += Math.pow(temp.getValue() - mean, 2);
        }
        return Math.sqrt(SquaredDifference / tweetsHashMap.size());
    }

    /**
     * Calculates the percentage of all the positive sentiment tweets
     * @param tweetsHashMap HashMap where tweets are stored
     * @return returns the percentage of positive sentiment tweets
     */

    public double calcPercPositive(HashMap<Status, Double> tweetsHashMap) {
        double percentage;
        double positiveCounter = 0.0;
        for (Map.Entry<Status, Double> temp : tweetsHashMap.entrySet()) {
            if (temp.getValue() > 0) positiveCounter++;
        }
        percentage = (positiveCounter / tweetsHashMap.size()) * 100.0;
        return percentage;
    }

    /**
     * Calculates the Percentages of all the negative sentiment tweets
     * @param tweetsHashMap HashMap where tweets are stored
     * @return returns the Percentage of negative sentiment tweets
     */

    public double calcPercNegative(HashMap<Status, Double> tweetsHashMap) {
        double percentage;
        double negativeCounter = 0;
        for (Map.Entry<Status, Double> temp : tweetsHashMap.entrySet()) {
            if (temp.getValue() < 0) negativeCounter++;
        }
        percentage = (negativeCounter / tweetsHashMap.size()) * 100;
        return percentage;
    }

    /**
     * Iterates through the tweets HashMap and writes the value of every element to a file
     * This can be used to graph the data that is obtained
     * @param tweetsHashMap
     * @throws IOException
     */
    public void writeDataToFile(HashMap<Status,Double> tweetsHashMap)throws IOException{
        FileWriter myWriter = new FileWriter("data.txt");
        for(Map.Entry<Status,Double> temp : tweetsHashMap.entrySet()){
            if(temp.getValue() != 0)
            myWriter.write(String.valueOf(temp.getValue()) + "\n");
        }
        myWriter.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        AnalyzeTweets aTweet = new AnalyzeTweets();
        final String sentiWords = "SentiWords_1.1.txt";
        final String tweets = "alltweetsSouthBig.ser";
        final HashMap<Status, Double> hash = aTweet.tweetsHashMap;
        aTweet.loadHash(sentiWords);
        aTweet.loadTweets(tweets);
        aTweet.loadTweetsForSen();
        System.out.println("Average Sentiment Value = " + aTweet.calcAvg(hash));
        System.out.println("Standard deviation of Sentiment value = " + aTweet.calcSD(hash));
        System.out.println("Percentage of positive tweets = " + aTweet.calcPercPositive(hash) + "%");
        System.out.println("Percentage of negative tweets = " + aTweet.calcPercNegative(hash) + "%");
        aTweet.writeDataToFile(hash);
        //aTweet.printSentiHashMap();
//      aTweet.printHashMap();
//        aTweet.printTweets();
    }

}
