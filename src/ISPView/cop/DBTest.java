package cop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.graphstream.graph.implementations.SingleGraph;

public class DBTest 
{
	/**
	 * select asname,network from ispnames where asnum in (select hopx from pathdb) x = num_of_hops (a.k.a. destination hop)
	 * source -> destination  = source -> IXP (rev) + IXP -> dest.
	 * @param source the source network in the path
	 * @param dest the destination network in the displayed path
	 * @return
	 */
	public static SingleGraph printNetworkPath(String source, String dest)//network to AS mapping.
	{
		//String source = "202.46.192.0/22"; //CYQ
		//String dest = "1.22.28.0/23";// "VODAFONE"
		//source -> destination  = source -> IXP (rev) + IXP -> dest.

		SingleGraph networkGraph = null;
				
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
	        networkGraph = new SingleGraph(source+"-"+dest);
	        
	        ResultSet rs;
	        rs = stat.executeQuery("SELECT * FROM PATHDB where network='"+source+"'");
	        
	        while (rs.next())
	        {
	        	String region = rs.getString("region");
	        	//addNode(networkGraph,region);

	        	int numHops = Integer.parseInt(rs.getString("hops"));
	        	String id0 = rs.getString("hop1");
	        	addEdge(networkGraph, source,id0);
	        	String id1 = "";
	        	for(int i =1; i< numHops; i++)
	        	{
	        		id1 = rs.getString("hop"+(i+1));
	        		addEdge(networkGraph, id1, id0); //the reverse path 
	        		id0 = id1;
	        	}      
	        	addEdge(networkGraph, "NIXI_"+region.toUpperCase(),id1);//the dest in the source path is connected to the IXP
	        	
	        }

	        ResultSet rs1;
	        rs1 = stat.executeQuery("SELECT * FROM PATHDB where network='"+dest+"'");
	        
	        while (rs1.next())
	        {
	        	String region = rs1.getString("region");
	        	int numHops = Integer.parseInt(rs1.getString("hops"));
	        	String id0 = rs1.getString("hop1");
	        	addEdge(networkGraph, "NIXI_"+region.toUpperCase(),id0);//the first hop is connected to the IXP
	        	
	        	String id1 = "";
	        	for(int i =1; i< numHops; i++)
	        	{
	        		id1 = rs1.getString("hop"+(i+1));
	        		addEdge(networkGraph, id1, id0); //the reverse path 
	        		id0 = id1;
	        	}
	        	addEdge(networkGraph, id1,dest);//the destination is connected to the last hop.
	        	
	        }

		}
		catch(SQLException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return networkGraph;
	 	
	}
	/**
	 * method that adds the node to the graph with basic ui style if it doesn't already exist in the graph.
	 * @param networkGraph the graph that's appended with the extra node.
	 * @param nodeID the String that identifies the node.
	 */
	static void addNode(SingleGraph networkGraph, String nodeID)
	{
		if(networkGraph.getNode(nodeID)==null)
		{ 
			networkGraph.addNode(nodeID);
			networkGraph.getNode(nodeID).setAttribute("ui.label",nodeID );
			networkGraph.getNode(nodeID).setAttribute("ui.style","fill-color: rgb(0,240,0);");
		}
		
	}
	/**
	 * Adds an edge to the specified graph, also adds the nodes if they're not present with basic UI attributes.
	 * @param networkGraph the graph that's modified by adding an edge to it
	 * @param node1 the ID of the source node in the directed graph
	 * @param node2 the ID of the destination node in the directed graph.
	 */
	static void addEdge(SingleGraph networkGraph, String node1, String node2)
	{
		addNode(networkGraph, node1);//adds the node if it doesn't exist in the graph already
		addNode(networkGraph, node2); 	
		if(networkGraph.getEdge(node1+"-"+node2)==null)
		{
			networkGraph.addEdge(node1+"-"+node2,networkGraph.getNode(node1),networkGraph.getNode(node2));
			networkGraph.getEdge(node1+"-"+node2).addAttribute("ui.style", "size: 1;");  
 
			networkGraph.getEdge(node1+"-"+node2).addAttribute("count",1);
		}
	}
	/**
	 * extracts an IP address from the network specified in CIDR notation
	 * @param network the network specified in CIDR notation
	 * @return the constant part of the IP address; i.e., the first m bits of the network addr (where m is the subnet mask length)
	 */
	static String getIP(String network)
	{
    	int i=0;
		String ipNet="";
		for(i =0;i<network.length();i++)
		{
			if(network.charAt(i)=='/')
			{
				break;
			}
		}
		ipNet = network;
		
		return ipNet;
	}
	static int getMask(String network)
	{
    	int i=0;
		int mask =0;
		boolean foundMask = false;
		for(i =0;i<network.length();i++)
		{
			if(network.charAt(i)=='/')
			{
				foundMask = true;
				break;
			}
		}
		if(foundMask){
			mask = Integer.parseInt(network.substring(i+1,network.length()));
		}
		else {
			mask = 32;//when no mask is specified, the entire network is exactly one IP address.
		}
		return mask;
	}
}
