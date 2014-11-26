import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;

/**
 * 
 * @author Anshuli
 * This class provides a method to build a PATRICIA trie to retrieve location of IP address and point to AS that 
 * it originated from.
 */
public class PatriciaTest 
{
	static PatriciaTrie<String,String> pt ;
	public static void main(String args[])//this was just for testing the API.
	{
		//testing.
		PatriciaTrie<String, Integer> trial2 = new PatriciaTrie<String, Integer>(StringKeyAnalyzer.CHAR);
		trial2.put("romane",1);
		trial2.put("romanus",2);
		trial2.put("romulus",3);
		trial2.put("rubens",4);
		trial2.put("ruber",5);
		trial2.put("rubicon",6);
		trial2.put("rubicundus",7);
		trial2.put("rubickube",8);
		System.out.println(trial2);
		
		PatriciaTrie<String, Integer> trial3 = new PatriciaTrie<String, Integer>(StringKeyAnalyzer.CHAR);
		trial3.put("he", 1);
		trial3.put("she", 2);
		trial3.put("hers", 3);
		trial3.put("his", 4);
		System.out.println(trial3);
		
		System.out.println(24&12);//11000 01100
		PatriciaTrie<String, String> trial = new PatriciaTrie<String, String>(StringKeyAnalyzer.CHAR);
		trial.put("10.2", "1");
		trial.put("10.2.3","2");
		trial.put("21.23.3.2", "3");
		trial.put("10","4");
		
		System.out.println(trial.prefixMap("10.2.3.4"));//problematic 
		
		//System.out.println(Integer.toBinaryString(17));
		//System.out.println(Integer.parseInt("101", 2));
		
		System.out.println(trial.selectValue("10.2.3.4"));//this works part-way; 
		//we still need to traverse down the patricia trie till we encounter a non- match. it diesn't give longest prefix match.
		//also, it matches with the third entry if its the only one in the trie; no - match isn't returned as null
		
		System.out.println(trial.select("10.2.3.4").getKey());//this is what we need!. Aha! but we still need to check because 
		//the last problem is still there (can't deal with in patricia, is an inherent thing); need to check for this explicitly.
		
	}
}

