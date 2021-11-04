/*
 * Class to support search of Twitter's tweets using twitter4j API.
 * Concatenates tweets to a file.
 * author: Nasif Hossain
 */

import twitter4j.*;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Scanner;
/**
 * Constructing a SearchTweets object will perform a single search
 * and store them in a List.
 */
public class SearchTweets {
	private static final int MAXTWEETS = 179; // max tweets at once
	private List<Status> tweets; // tweets from query
	private Query query; // query that generated the tweets
	private static File myTweet;
	private static Scanner myReader;

	/**
	 * Default contstructor contains no tweets, but you can
	 * add tweets to it.
	 */
	public SearchTweets() {
		tweets = new ArrayList<Status>();

	}

	/**
	 * Generate tweets based on typical Twitter search string.
	 *
	 * @param querystring (See https://dev.twitter.com/docs/using-search )
	 */
	public SearchTweets(String querystring) {
		this(new Query(querystring), 20, null, 0.0, Query.ResultType.recent);
	}

	/**
	 * Retrieve tweets based on typical Twitter search string.
	 *
	 * @param query  valid Query object
	 * @param count  number of tweets to get (max 100)
	 * @param loc    Central point of tweets (null to get all tweets)
	 * @param radius Radius (in km) of locations searched
	 * @param type   type of tweets to get
	 */
	public SearchTweets(Query query, int count, GeoLocation loc,
						double radius, Query.ResultType type) {
		this.query = query;
		if (count > 0 && count < MAXTWEETS) // limit tweets we get
			query.setCount(count);
		query.setResultType(type);
		if (loc != null) query.setGeoCode(loc, radius, Query.KILOMETERS);
		Twitter twitter = new TwitterFactory().getInstance();
		QueryResult result = null;
		try {
			result = twitter.search(query);
			tweets = result.getTweets(); // store retrieved tweets
		} catch (TwitterException te) {
			tweets = null;
			te.printStackTrace();
			System.out.println("Failed to search tweets: " + te.getMessage());
		}
	}


	/**
	 * @return number of tweets in this object
	 */
	public int numTweets() {
		if (tweets == null) return 0;
		return tweets.size();
	}

	/**
	 * Add a method to return an arraylist consisting of every userid and the
	 * number of tweets by that user.
	 */
	public ArrayList<UserData> getCounts() {
		if (tweets == null) return null;
		ArrayList<String> results = new ArrayList<String>();
		// get info about all tweets
		for (Status tweet : tweets) {
			String name = tweet.getUser().getScreenName();
			results.add(name);
		}
		Collections.sort(results);
		ArrayList<UserData> userdata = new ArrayList<UserData>();

		// Count tweets by username, assume userdata is sorted by name.
		for (int i = 0; i < results.size(); i++) {
			String name = results.get(i);
			int count = 0;
			// loop over all of same name, update count
			while (i < results.size() && name.equals(results.get(i))) {
				i++;
				count++;
			}
			userdata.add(new UserData(name, count));
			i--;
		}
		return userdata;
	}

	/**
	 * Print all tweets to standard output.
	 */
	public void print() {
		if (tweets == null) return;
		// Print out info about all tweets
		for (Status tweet : tweets) {
			System.out.println("\n\n@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
			System.out.println("date: " + tweet.getCreatedAt());
			//System.out.println("User: " + tweet.getUser());
			System.out.println("Reply to: " + tweet.getInReplyToScreenName());
		}
	}

	/**
	 * Save tweets to file named fname.
	 */
	public void save(String fname)
			throws IOException {
		save(fname, this.tweets);
	}

	/**
	 * Save all tweets to a file.  Saves all Status data
	 * as serialized objects.
	 *
	 * @param filename Name of output file.  Will append
	 *                 all unique tweets to any file of same name.
	 */
	public void save(String filename, List<Status> tweetslist)
			throws IOException {
		load(filename); //load all the status objects from the file into the arrayList
		deleteDuplicate();//delete duplicates from arraylist after loading it in the arraylist
//		String reference = "d*123**23122*";
		FileOutputStream fileobject = new FileOutputStream(filename);
		ObjectOutputStream output = new ObjectOutputStream(fileobject);
		for (Status temp : tweets) output.writeObject(temp);

		output.close();
		fileobject.close();
	}

		// TODO


	/**
	 * Save tweets from file named fname.
	 */
	public void load(String fname) {
		try {
			load(fname, this.tweets);
		} catch (IOException | ClassNotFoundException ioe) {
			System.err.println("Error in load.  Load failed.");
			ioe.printStackTrace();

		}
 	}


	/**
	 * Load tweets from a saved file of Status objects.
	 * Appends to current this.tweets all tweets not already stored.
	 *
	 * @param filename Name of input file.
	 */
	public void load(String filename, List<Status> tweetslist) throws IOException, ClassNotFoundException, EOFException {
		try {
			FileInputStream fos = new FileInputStream(filename);
			BufferedInputStream bos = new BufferedInputStream(fos);
			ObjectInputStream input = new ObjectInputStream(bos);
			Status temp = null;
			while((temp = (Status) input.readObject()) != null){
				tweetslist.add(temp);
			}
			fos.close();
			bos.close();
			input.close();
		} catch (EOFException exc){
			exc.printStackTrace();
		}
	}


	/**
	 * Delete duplicate tweets from tweets ArrayList
	 */

	public void deleteDuplicate() {
		List<Status> newTweets = new ArrayList<>();
		for (Status tweet : tweets) {
			if (!newTweets.contains(tweet))
				newTweets.add(tweet);
		}
			tweets = newTweets;

		}

	// TODO

	/**
	 * Load location data from a file.
	 *
	 * @param fname File from which to load data
	 * @return Arraylist of all cities in the file.
	 */
	public static ArrayList<Location> loadLocations(String fname) {
		ArrayList<Location> locations = new ArrayList<Location>();
		File infile = new File(fname);
		Scanner scanner = null;
		try {
			scanner = new Scanner(infile);
		} catch (FileNotFoundException ex) {
			System.err.println("Error: locations file not found.");
			return null;
		}
		// Loop over lines and split into fields we keep.
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			StringTokenizer tokenizer = new StringTokenizer(line);
			String[] fields = line.split(",");
			long pop = Long.parseLong(fields[4]);
			if (pop > 100000) { // Ignore small towns.
				// See Location.java to figure out these fields.
				Location loc = new Location(fields[0], fields[1],
						Double.parseDouble(fields[2]), Double.parseDouble(fields[3]), pop);
				locations.add(loc);
				//System.out.println("Added " + loc.toString());
			}

		}
		scanner.close();
		return locations;
	}


	/**
	 * Collect tweets and add all resulting tweets to a file.
	 *
	 * @param numtweets   Max number of tweets to gather
	 * @param searchwords String of all words that will form twitter searches
	 * @param loc         Location to search for tweets
	 * @param radius      Radius (kilometers) about loc to search
	 * @param type        Type of results to find
	 */
	public static void collectTweets(int numtweets, String searchwords, GeoLocation loc,
									 double radius, Query.ResultType type, String outfile) throws IOException {

		StringTokenizer searchtok = new StringTokenizer(searchwords);
		while (searchtok.hasMoreTokens()) {
			String word = searchtok.nextToken();
			SearchTweets tweets = new SearchTweets(new Query(word), numtweets, loc, radius, type);
			System.out.println("Num Tweets: " + tweets.numTweets());
			tweets.print();
			tweets.save(outfile);

		}
	}

	/**
	 * Usage: java twitter4j.examples.search.SearchTweets [query]
	 *
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		final String OUTFILE = "alltweetsNorth.ser";

		if (args.length == 1) {
			System.out.println("Just reading tweets.");
			SearchTweets searcher = new SearchTweets();
			ArrayList<Status> tweetlist = new ArrayList<Status>();
			try {
				searcher.load(OUTFILE, tweetlist);
			} catch (Exception e) {
				System.out.println("ERROR: " + e);
				System.exit(-1);
			} finally {
				System.exit(-1);
			}
		} else if (args.length != 0) {
			System.out.println("java SearchTweets");
			System.exit(-1);
		}


		/*
		 * TODO: Right now this just searches for these words in big cities.
		 */
		String searchWords = "#vaccinated #vaccine vaccine vaccinated fda-approved";
		int maxnum = 20; // max num tweets to get at one time
		final double radius = 20; // radius around location to search

//		GeoLocation gl = new GeoLocation(42.01955, -73.9080); // Bard
//		// 179 tweets within 50 Kilometers
//		SearchTweets tweets = new SearchTweets(new Query("vaccine"),
//				10, gl, 20, Query.ResultType.recent);
//		System.out.println("Num Tweets: " + tweets.numTweets());
//
//		tweets.print();

		//OMIT FOR NOW

		// Get citites data.
		ArrayList<Location> locations = loadLocations("us-cities.txt");
		long minpop = 6000000L; // min city size to consider

		// WARNING: If you get too much data, you will be     //
		// locked out for 15 minutes!                         //
		for (int i = 250; i < locations.size(); i++) {
			if(locations.get(i).getLatitude() > 40) {
				//if (locations.get(i).getPopulation() < minpop) { //we do not check for population
					System.out.println(locations.get(i) + "===================");
					GeoLocation loc = locations.get(i).getLocation();
					collectTweets(maxnum, searchWords, loc, radius,
							Query.ResultType.recent, "alltweetsNorth.ser");

			}
		}


	}
}


