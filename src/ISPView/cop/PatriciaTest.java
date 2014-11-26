package cop;

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
	public static void main(String args[])//this was just for testing the API.
	{
		//testing.
		//System.out.println(24&12);//11000 01100
		
		PatriciaTrie<String, String> trial = new PatriciaTrie<String, String>(StringKeyAnalyzer.CHAR);
		trial.put("10.2", "1");
		trial.put("10.2.3","2");
		trial.put("21.23.3.2", "3");
		trial.put("10","4");
		System.out.println(trial);
		System.out.println("............................");
		//trial.subMap(arg0, arg1)
		
		System.out.println(trial.prefixMap("10.2.3.4"));//problematic 
		
		//System.out.println(Integer.toBinaryString(17));
		//System.out.println(Integer.parseInt("101", 2));
		
		System.out.println(trial.selectValue("10.2.3.4"));//this works part-way; 
		//we still need to traverse down the patricia trie till we encounter a non- match. it diesn't give longest prefix match.
		//also, it matches with the third entry if its the only one in the trie; no - match isn't returned as null
		
		System.out.println(trial.select("10.2.3.4").getKey());//this is what we need!. Aha! but we still need to check because 
		//the last problem is still there (can't deal with in patricia, is an inherent thing); need to check for this explicitly.
	}
	/**
	 * this method uses a PATRICIA trie of IP addresses of known Autonomous Systems to retrieve the 
	 * location of an IP address
	 * @param ipAddr an IP address that we shall pinpoint to an Autonomous System where it originated from.
	 * @return the autonomous system that the IP address belongs to 
	 */
	public static String getASFromIP(String ipAddr)
	{
		PatriciaTrie<String,String> pt = new  PatriciaTrie<String,String>(StringKeyAnalyzer.CHAR);
		String ans="";

		try{
		Class.forName("org.h2.Driver");
		String userHome = System.getProperty("user.dir");
		if(!MainFrame.setPath){
			System.setProperty("user.dir", userHome+"/COP");
			userHome = System.getProperty("user.dir");
			//System.out.println(userHome);
			MainFrame.setPath = true;
		}
        //Connection conn = DriverManager.getConnection("jdbc:h2:~/COP", "sa", "");
		Connection conn = DriverManager.getConnection("jdbc:h2:file:"+userHome, "sa", "");
		
        Statement stat = conn.createStatement();
        
        ResultSet rs;
        rs = stat.executeQuery("SELECT * FROM PATHDB");
        
        while (rs.next()) 
        {
        	String network = rs.getString("network");
        	
        	//the corresponding network  is the last hop ; i.e., the destination.
        	String hops = rs.getString("Hops");
        	String AsNum = rs.getString("Hop"+hops);
        	String ASN = null;
        	
        	Statement stat1 = conn.createStatement();
            ResultSet rs1;
            rs1 = stat1.executeQuery("select shortname from ispnames  where asnum ='"+AsNum+"'");
            while(rs1.next())
            {
            	ASN = rs1.getString("shortname");
            }
            
        	int i=0;
			int mask =0;
			boolean foundMask = false;
			String ipNet="";
			for(i =0;i<network.length();i++)
			{
				if(network.charAt(i)=='/')
				{
					foundMask = true;
					
					//System.out.println("ip address "+ipNet);
					break;
				}
			}
			
			if(foundMask){
				mask = Integer.parseInt(network.substring(i+1,network.length()));
				ipNet=network.substring(0,i);
			}
			else {
				mask = 32;//when no mask is specified, the entire network is exactly one IP address.
				ipNet = network;
			}
			
			String ipMask = getMask(ipNet,mask);
			//System.out.println("mask "+mask);
        	pt.put(ipMask,ASN+":"+ipNet);//TODO concatenate the path ip the ASName as well (make it part of the value stored in the PATRICIA tree.)
        	stat1.close();	
        }
         
		ans = pt.select(ipAddr).getValue();
		//System.out.println("the ASName ans - "+ans);
        
        stat.close();
        conn.close();
        
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		if(ans==null) return "Match not Found";
		else return ans;
	}
	
	/**
	 * 
	 * @param ip the IP address 
	 * @param mask the subnet mask length
	 * @return The return value is the part of the IP address that is common to all the addresses in the network, 
	 * basically obtained by pruning the end and appending zeroes in the binary notation
	 * eg.-if 10.2.1.34 is the IP and 24 is the mask length, the return value is 10.2.1
	 */
	
	private static String getMask(String ip, int mask) {
		String[] split = new String[4];
		split = ip.split("\\.",4);
		
		if(mask<=8)
		{
			String temp = "" ;
			for(int i=0;i<8;i++)
			{	
				if(i<mask)temp+="1";
				else temp+="0";
			}
			//System.out.println("temp1 -"+temp);
			return ""+(Integer.parseInt(split[0])& Integer.parseInt(temp,2));
		}
		else if (mask>8 && mask <=16)
		{
			String temp = "" ;
			for(int i=0;i<8;i++)
			{	
				if(i<mask-8)temp+="1";
				else temp+="0";
			}
			//System.out.println("temp2 -"+temp);
			return(split[0]+"."+(Integer.parseInt(split[1])&Integer.parseInt(temp,2)));
		}

		else if (mask>16 && mask <=24)
		{
			String temp = "";
			for(int i=0;i<8;i++)
			{	
				if(i<mask-16)temp+="1";
				else temp+="0";
			}
			//System.out.println("temp3 -"+temp);
			return(split[0]+"."+split[1]+"."+(Integer.parseInt(split[2])&Integer.parseInt(temp,2)));
		}
		
		else if (mask>24 && mask <=32)
		{
			String temp = "";
			for(int i=0;i<8;i++)
			{	
				if(i<mask-24)temp+="1";
				else temp+="0";
			}
			//System.out.println("temp4 -"+temp+" ip- "+ip+" split[0] "+split[0] );
			return(split[0]+"."+split[1]+"."+split[2]+"."+(Integer.parseInt(split[3])&(Integer.parseInt(temp,2))));
		}
		return null;
		
	}
}

