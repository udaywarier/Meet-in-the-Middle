import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

public class MeetInTheMiddle 
{
	//Google search URL. After the = sign, put in search query separated by + signs.
	static final String searchQuery = "https://www.google.com/search?q=";
	
	//Restaurant and state of interest, can be changed to accommodate different needs.
	static final String restaurant = "Buffalo Wild Wings";
	static final String state = "NJ";
	
	//Since the formatted restaurant name is the same for all locations, make it a global variable so the computation doesn't have to happen every time.
	static final String restaurantFormatted = formatGoogleSearch(restaurant);
	
	//Map of addresses of people, Set of locations of restaurants.
	static Map<String, String> friendsAddresses = new TreeMap<>();
	static Set<String> bdubsLocations = new TreeSet<>();
	
	public static void main(String[] args) throws IOException 
	{
		//Hard code Map and Set for now, figure out how to automate this later.
		
		friendsAddresses.put("Uday", "3 Diamond Ct");
		friendsAddresses.put("Rhea", "44 Colemantown Dr");
		friendsAddresses.put("Shlok", "30 Aspen Dr");
		
		bdubsLocations.add("Princeton");
		bdubsLocations.add("Morganville");
		bdubsLocations.add("North Brunswick");
		bdubsLocations.add("Harmon Meadow");
		bdubsLocations.add("Woodbridge");
		bdubsLocations.add("Linden");
		bdubsLocations.add("Mt. Laurel");
		bdubsLocations.add("Brick");
		bdubsLocations.add("Parsippany");
		bdubsLocations.add("Watchung");
		bdubsLocations.add("Bridgewater");
		bdubsLocations.add("Flemington");
		bdubsLocations.add("Monmouth");
		bdubsLocations.add("Sicklerville");
		bdubsLocations.add("Rockaway");
		bdubsLocations.add("Toms River");
		bdubsLocations.add("Mays Landing");
		bdubsLocations.add("Millville");
		
		//Cuz I'm a baller.
		run();
	}
	
	private static void run()
	{
		//Times table is the string that displays the full table of people, locations, and times.
		String timesTable = "\t\t";
		
		//Add all the people's names to the String.
		for(String name : friendsAddresses.keySet())
		{
			timesTable += name + "\t";
		}
		
		timesTable += "Mean\tStd. Dev.";
		
		//Create nested maps. The outer map is the restaurant location, the inner map is the restaurants distance from all the people.
		Map<String, TreeMap<String, Double>> totalTimes = new TreeMap<>();
		for(String bdubsName : bdubsLocations)
		{
			//Print a message so Uday knows something is happening when he presses the run button.
			System.out.println("Computing times for " + bdubsName + "...");
			
			//Add the nested map to the outer map. Inside this loop is where the time computation happens. The timeBetween() method takes a person's name and a restaurant's location, then 
			//computes the distance between the two.
			TreeMap<String, Double> currentTimes = new TreeMap<>();
			for(String friendsName : friendsAddresses.keySet())
			{
				currentTimes.put(friendsName, timeBetween(friendsAddresses.get(friendsName), bdubsName));
			}
			
			totalTimes.put(bdubsName, currentTimes);
		}
		
		//This loop basically just goes through the nested map and formats its contents into the String. It adds two additional things: the mean travel time for all people per location and 
		//standard deviation of travel time for each location, to help decide which location offers the most balanced travel time.
		for(Map.Entry<String, TreeMap<String, Double>> entry : totalTimes.entrySet())
		{
			timesTable += "\n" + entry.getKey() + "\t";
			
			//Ignore this, there's probably a better way to fix this formatting issue but I'm too lazy to figure it out now.
			if(entry.getKey().equals("Brick") || entry.getKey().equals("Linden"))
			{
				timesTable += "\t";
			}
			
			for(Map.Entry<String, Double> entry2 : entry.getValue().entrySet())
			{
				timesTable += entry2.getValue() + "\t";
			}
			
			timesTable += String.format("%.2f", getMean(entry.getValue())) + "\t" + String.format("%.2f", getStdDeviation(entry.getValue()));
		}
		
		System.out.println(timesTable);
	}
	
	//This method takes a String as input and formats it so that it can be put into a Google Search URL.
	private static String formatGoogleSearch(String search)
	{
		String formatted = "";
		
		String[] address =  search.split(" ");
		for(int x = 0; x < address.length; x++)
		{
			formatted += address[x] + (x == address.length - 1 ? "" : "+");
		}
		
		return formatted;
	}
	
	//Uses Java Internet capabilities to open a Web Page, then Jsoup to parse the Web Page to obtain the time that Google Maps says the travel will take.
	private static double timeBetween(String friendAddress, String bdubsAddress)
	{
		//Uses formatGoogleSearch() to format the URL that is used to search for the travel distance. Ignore the hard coded thing, I'll figure it out later.
		String search = searchQuery + formatGoogleSearch(friendAddress) + "+to+" + restaurantFormatted + "+" + formatGoogleSearch(bdubsAddress) 
		+ (bdubsAddress.equals("Monmouth") ? "" : "+" + state);
		
		String time = "";
		
		try
		{	
			//Jsoup stuff that parses the plain text from the web page.
			Document doc = Jsoup.connect(search).get();
			String[] text = doc.text().split(" ");
			
			//Loops through all the web page text to find where the travel time is.
			for(int x = 0; x < text.length; x++)
			{
				//Finds where on the web page the travel time is listed, then puts the travel time into a string and breaks from the loop.
				if(text[x].equalsIgnoreCase("Image"))
				{
					time += text[x + 1] + " " + text[x + 2];
					
					if(text[x + 2].equalsIgnoreCase("h") && text[x + 4].equalsIgnoreCase("min"))
					{
						time += " " + text[x + 3] + " " + text[x + 4];
					}
					break;
				}
			}
		}
		catch(Exception e) {e.printStackTrace();}
		
		return timeInMinutes(time);
	}
	
	//Takes a String that represents time and parses the time in minutes from the String. Ex:
	//39 min -> 39
	//2 h -> 120
	//2 h 39 min -> 159
	//Those are the three cases that this method could encounter, basically all it does is handle these cases to get the time. 
	private static double timeInMinutes(String time)
	{
		int minutes = 0;
		
		String[] timeParsed = time.split(" ");
		int length = timeParsed.length;
		
		if(length == 2)
		{
			if(timeParsed[1].equalsIgnoreCase("h"))
			{
				minutes = Integer.parseInt(timeParsed[0]) * 60;
			}
			
			else
			{
				minutes = Integer.parseInt(timeParsed[0]);
			}
		}
		
		if(length == 4)
		{
			minutes = (Integer.parseInt(timeParsed[0]) * 60) + Integer.parseInt(timeParsed[2]);
		}
		
		return (double)minutes;
	}
	
	private static double getMean(TreeMap<String, Double> times)
	{
		double totalTime = 0;
		
		for(Map.Entry<String, Double> entry : times.entrySet())
		{
			totalTime += entry.getValue();
		}
		
		return totalTime/ times.size();
	}
	
	private static double getStdDeviation(TreeMap<String, Double> times)
	{
		double mean = getMean(times);
		
		double currentTotal = 0;
		for(Map.Entry<String, Double> entry : times.entrySet())
		{
			currentTotal += Math.pow(entry.getValue() - mean, 2);
		}
		
		return Math.sqrt(currentTotal) / times.size();
	}
}