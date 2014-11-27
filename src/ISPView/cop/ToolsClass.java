package cop;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;

import java.io.*;
import java.sql.*;


/**
 * Class which defines all the methods which deal with tools i.e. Updating and Pruning the database.
 * @author indu
 *
 */
public class ToolsClass 
{	
	static int currentversion;
	static StringBuffer InsertQuery1 = new StringBuffer("insert into PathDB(version, Source, Region, ASName, IsSent");
	static StringBuffer InsertQuery3;
	static StringBuffer InsertQuery5 = new StringBuffer(")");
	static int MaxHops = 20;
	static String[] RegionList = {"Mumbai", "Delhi+(Noida)","Kolkata", "Hyderabad"};
	
	
	/**
	 * Parses paths for a given ISP, Region and status and then inserts into the database
	 * @param IP IP for the chosen ISP
	 * @param isSent Status of the traffic (true for sent, false for received)
	 * @param ISPName Name of the chosen ISP
	 * @param Region Region of the chosen ISP
	 * @throws HttpException Thrown if there is an error in internet connectivity
	 * @throws IOException Thrown if there is an error in sending the HTTP request
	 * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	 */
 	static void GetRoutes(String IP, Boolean isSent, String ISPName, String Region) throws HttpException, IOException, ClassNotFoundException, SQLException
	{
 		InsertQuery3 = new StringBuffer(",Hops, Network) values(" +Integer.toString(currentversion) +", 'NIXI','");
		StringBuffer InsertQuery2 = new StringBuffer();
		StringBuffer InsertQuery4 = new StringBuffer();
		StringBuffer InsertQuery = new StringBuffer();
		
		InsertQuery4.append(Region+"','");
		InsertQuery4.append(ISPName+"',");
		InsertQuery4.append(isSent.toString());

		HttpClient client = new HttpClient();
		String url = "http://203.190.131.164/lg/?query=bgp&protocol=IPv4&addr=neighbors+" + IP + "+";
		if(isSent)
		{
			url+="advertised-";
		}
		url += "routes&router=NIXI+"+Region;
		
		GetMethod method = new GetMethod(url);
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
			
		
	      // Execute the method.
	      client.executeMethod(method);

	      //Parse Response
	      InputStream Body = method.getResponseBodyAsStream();	      
	      Document doc = Jsoup.parse(Body, null, url);
	      Element codeElement = doc.getElementsByTag("CODE").first();
	      Elements links = codeElement.getElementsByTag("A");
	  
	      //Form query
	      int hops=0;
	      StringBuffer Network = new StringBuffer();
	      for (Element link : links) 
	      {
	    	  try
	    	  {
	    		  Integer.parseInt(link.text());
	    		  InsertQuery4.append("," + link.text());
	    		  hops++;
	    	  }
	    	  catch(NumberFormatException e)
	    	  {
	    	      InsertQuery4.append("," + Integer.toString(hops)+",'"+Network+"'");
	    	      
	    	      	for(int i=1; i<=hops; i++)
	    	      	{
	    	      		InsertQuery2.append(",Hop"+Integer.toString(i));
	    	      	}

	    	      	InsertQuery.append(InsertQuery1);
	    	      	InsertQuery.append(InsertQuery2);
	    	      	InsertQuery.append(InsertQuery3);
	    	      	InsertQuery.append(InsertQuery4);
	    	      	InsertQuery.append(InsertQuery5);
	    	    
	    	      	
	    	      	if(hops>MaxHops)
	    	      	{
	    	      		while(hops>MaxHops)
	    	      		{
	    	      			MaxHops++;
	    	      			stat.execute("alter Table PathDB add column Hop"+Integer.toString(MaxHops)+" varchar(255)");
	    	      		}
	    	      	}
	    	      	
	    	      	if(hops>0)
	    	      		stat.execute(InsertQuery.toString());
	    	      	
	    	      	InsertQuery = new StringBuffer();
	    	      	InsertQuery2 = new StringBuffer();
	    	      	InsertQuery4 = new StringBuffer();
	    			InsertQuery4.append(Region+"','");
	    	      	InsertQuery4.append(ISPName+"',");
	    			InsertQuery4.append(isSent.toString());
	    	      	hops=0;
	    	      	Network = new StringBuffer(link.text());
	    	      	
	    	  }
	      }
  
	}
	
 	
 	/**
 	 * Gets the ISP Name from the IP and the Region
 	 * @param IP IP address of the required ISP
 	 * @param Region Region of the required ISP
 	 * @return Name of the ISP
 	 * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
 	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
 	 * @throws IOException Thrown if there is an error in sending the HTTP request
 	 * @throws HttpException Thrown if there is an error in internet connectivity
 	 */
	static String GetISPName(String IP, String region) throws ClassNotFoundException, SQLException, HttpException, IOException
	{
		String ASNum = GetASNum(IP, region);
		String ISPName = null;
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
        rs = stat.executeQuery("select * from ispnames where ASnum = '"+ASNum+"'");
        while(rs.next())
        {
        	ISPName = rs.getString("shortname");
        }
        
        return ISPName;
	}
	
	
	
	/**
	 * Gets AS Number from IP and Region
	 * @param IP IP of the required AS
	 * @param Region Region of the required AS
	 * @return AS Number of the required ISP
	 * @throws IOException Thrown if there is an error in sending the HTTP request
	 * @throws HttpException Thrown if there is an error in internet connectivity
	 */
	static String GetASNum(String IP, String Region) throws HttpException, IOException
	{
		HttpClient client = new HttpClient();
	    String url = "http://203.190.131.164/lg/?query=bgp&protocol=IPv4&addr=neighbors+"+IP+"&router=NIXI+"+Region;
	    
	    // Create a method instance.
	    GetMethod method = new GetMethod(url);
	    

	      // Execute the method.
	      int statusCode = client.executeMethod(method);

	      if (statusCode != HttpStatus.SC_OK) 
	      {
	        System.err.println("Method failed: " + method.getStatusLine());
	      }

	      InputStream Body = method.getResponseBodyAsStream();	      
	      Document doc = Jsoup.parse(Body, null, url);
	      
	      Element link = doc.getElementsByTag("CODE").first().getElementsByTag("A").get(1);
	      method.releaseConnection();
	      return link.text().trim();
  
	}
	
	
	
	/**
	 * Gets all ISP paths for a particular Region
	 * @param Region Region for which the paths are to be picked up
	 * @throws HttpException Thrown if there is an error in internet connectivity
	 * @throws IOException Thrown if there is an error in sending the HTTP request
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	 * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 */
	@SuppressWarnings("deprecation")
	static void GetAllPaths(String Region) throws HttpException, IOException, ClassNotFoundException, SQLException
	{	
	    HttpClient client = new HttpClient();
	    String url = "http://203.190.131.164/lg/";
	    
	    // Create a method instance.
	   PostMethod method = new PostMethod(url);
	   method.setRequestBody("query=summary&protocol=IPv4&addr=&router=NIXI+"+Region);	    
	    

	      // Execute the method.
	      client.executeMethod(method);

	      //Parse the response
	      InputStream Body = method.getResponseBodyAsStream();	      
	      Document doc = Jsoup.parse(Body, null, url);      
	      Element codeElement = doc.getElementsByTag("CODE").first();
	      Elements links = codeElement.getElementsByTag("B");
	      
	      
	      //Process the Response
	      for (Element link : links) 
	      {
	    	  try
	    	  {
	    		  Integer.parseInt(link.text());
	    	  }
	    	  catch(NumberFormatException e)
	    	  {
	    		  String S=GetISPName(link.text().trim(), Region);
	    		  //stat.execute("insert into PathDB(ASName) values('"+S+"')");
	    		  //GetRoutes(link.text(), true, S);
	    		  GetRoutes(link.text(), false, S, Region);
	    		  GetRoutes(link.text(), true, S, Region);
	    		  //count++;
	    	  }
	      }

	    
	      // Release the connection.
	      method.releaseConnection();
	    
	    
	}
	
	
	
	/**
	 * Creates the table PathDB if its doesn't exist. Sets the version number for the latest update 
	 * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	 */
	static void SetUpDB() throws ClassNotFoundException, SQLException
	{
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

        try
        {
        	stat.execute("create table PathDB2(version int, id int primary key auto_increment, Source varchar(255), Region varchar(255),ASNum varchar(255), ASName varchar(255), IsSent Boolean, Network varchar(255), Hops int," +
		"Hop1 varchar(255), Hop2 varchar(255), Hop3 varchar(255), Hop4 varchar(255), Hop5 varchar(255), " +
		"Hop6 varchar(255), Hop7 varchar(255), Hop8 varchar(255), Hop9 varchar(255), Hop10 varchar(255), " +
		"Hop11 varchar(255), Hop12 varchar(255), Hop13 varchar(255), Hop14 varchar(255), Hop15 varchar(255), " +
		"Hop16 varchar(255), Hop17 varchar(255), Hop18 varchar(255), Hop19 varchar(255), Hop20 varchar(255), )");
        }
        catch(Exception e)
        {
        }
        
        currentversion=0;
        try
        {
        	stat.execute("create table versiondb(time timestamp default CURRENT_TIMESTAMP, version int)");
        	currentversion=1;
        }
        catch(Exception e)
        {
        	ResultSet rs = stat.executeQuery("select max(version) from versiondb");
        	while(rs.next())
        	{
        		currentversion=Integer.parseInt(rs.getString("max(version)"));
        	}
        	currentversion++;
        }
        stat.execute("insert into versiondb(version) values("+ Integer.toString(currentversion) + ")");
        conn.close();
        
	}

	
	/**
	 * Updates the table by adding a new set of data
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	 * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 * @throws IOException Thrown if there is an error in sending the HTTP request
	 * @throws HttpException Thrown if there is an error in internet connectivity
	 */
	static void NIXIUpdate() throws ClassNotFoundException, SQLException, HttpException, IOException  
	{
		SetUpDB();
		for(String Region: RegionList)
		{
			GetAllPaths(Region);
		}
	}

	
	
	/**
	 * Prunes the database to retain only the latest version
	 * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	 */
	static void NIXIPrune() throws ClassNotFoundException, SQLException
	{
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
        
        stat.execute("delete from versiondb where version != (select max(version) from versiondb)");
        stat.execute("delete from pathdb where version != (select version from versiondb)");
        
	}
	
	public static void main(String[] args) throws Exception 
	{
		SetUpDB();
		for(String Region: RegionList)
		{
			GetAllPaths(Region);
		}
		
	}

}
