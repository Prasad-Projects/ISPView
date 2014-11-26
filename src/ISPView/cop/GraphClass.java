package cop;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.implementations.*;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;

/**
 * Class which defines all the methods concerning the creation and manipulation of the graph.
 *
 */
public class GraphClass 
{

	static SingleGraph graph;
	static SingleGraph graphNwk;
	static SingleGraph displayPath;
	static SingleGraph pathGraphMumbai, pathGraphDelhi, pathGraphKolkata, pathGraphHyd;//Kolkata,Hyderabad,	Mumbai,	Delhi+(Noida)
	static View view,viewPath,nwkView;
	static String viewID;
    static int finishedThreads;
	/**
	* Gets the required paths from the database
	* @param version Version chosen by user
	* @param Region Region chosen by user
	* @param ISPName Name of ISP chosen by user
	* @param Status Status of data chosen by user
	 * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	 * @throws NoResultException Thrown if no data is found.
	* 
	*/
	private static ResultSet GetAllPaths(int version, String Region, String ISPName, String Status) throws ClassNotFoundException, SQLException, NoResultException
    {
        Class.forName("org.h2.Driver");
        String userHome = System.getProperty("user.dir");
		if(!MainFrame.setPath){
			System.setProperty("user.dir", userHome+"/COP");
			userHome = System.getProperty("user.dir");
			//System.out.println(userHome);
			MainFrame.setPath = true;
		}
		Connection conn = DriverManager.getConnection("jdbc:h2:file:"+userHome, "sa", "");
        //Connection conn = DriverManager.getConnection("jdbc:h2:~/COP", "sa", "");
		
        Statement stat = conn.createStatement();

        StringBuffer Query;
        //System.out.println("version "+version);
        if(ISPName.indexOf(':')>=0 && ISPName.indexOf(':')<ISPName.length())
        	//the ISPName has a ASNum attached in order to differentiate between different AS that have the same shortname
        {
        	String asNum, asName;
        	asName = ISPName.substring(0,ISPName.indexOf(':'));
        	asNum = ISPName.substring(ISPName.indexOf(':')+1, ISPName.length());
        	
        	Query = new StringBuffer("select * from pathdb2 where Version="+version+" and ASName='"+asName+"and ASNum = '"+asNum+"' and Region='"+Region+"' and issent=");
        }
        else
        {
        	Query = new StringBuffer("select * from pathdb2 where Version="+version+" and ASName='"+ISPName+"' and Region='"+Region+"' and issent=");
        }
        //System.out.println("Query "+Query);
        if(Status.equalsIgnoreCase("Sent"))
        	Query.append("true");
        else
        	Query.append("false");
        
        ResultSet rs;
        //System.out.println(Query);
        rs = stat.executeQuery(Query.toString());
        
        if(rs==null)
        {
        	throw new NoResultException();
        }
        
        return rs;
    }
    
	/**
	* Gets the required paths from the database
	* @param results ResultSet from the query
	 * @throws NumberFormatException Thrown if a parsing error occurs.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	* 
	*/
    private static String[] GetPath(ResultSet results) throws NumberFormatException, SQLException
	{
    	int HopCount = Integer.parseInt(results.getString("Hops"));
    	
		String[] S = new String[HopCount];
		for(int i=1; i<=HopCount; i++)
		{
			S[i-1]=results.getString("Hop"+Integer.toString(i));
		}
		return S;
	}
    
    /**
	* Gets the ISP Name corresponding to an AS Number.
	* @param ASNum The required AS Number.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
     * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	* 
	*/
    private static String GetASName(String ASNum) throws SQLException, ClassNotFoundException
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
        
        ResultSet rs;
        rs = stat.executeQuery("select * from ISPNames where ASNum='"+ASNum+"'");
        
        String R=new String();
        while(rs.next())
        {
        	R=rs.getString("ShortName");
        }
        return R;
   
    }
    
    /**
     * Takes a snapshot of the graph and stores as a .png file in the current directory.
     */
    static void SnapShot(SingleGraph graph1)
    {
  
    	FileSinkImages pic = new FileSinkImages(OutputType.PNG, Resolutions.VGA);
    	pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
    	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    	try
    	{
    		pic.writeAll(graph1, "screenshot"+timeStamp+".png");
    	}
    	catch(IOException e)
    	{
    		new NotifyUser("Cannot take screenshot now. File Error.");
    	}
    	
    	
    }
    
    //TODO add a button to use one of the two different layouts - choose between these two methods.
    /**
	* Creates the graph
	* @param version Version chosen by user
	* @param Region Region chosen by user
	* @param ISPName Name of ISP chosen by user
	* @param Status Status of data chosen by user
    * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	 * @throws NoResultException Thrown if no data is found.
	 * @throws IOException Thrown if there ips an error with the intermediate file.
	* 
	*/
    /*
     * 
    static void createTree(int version, String Region, String ISPName, String Status) throws ClassNotFoundException, SQLException, NoResultException, IOException
    {
    	if(MainFrame.panel2.isAncestorOf(viewPath))
        {
        	MainFrame.panel2.remove(viewPath);
        	//System.out.println("removing");
        }
        if(MainFrame.panel2.isAncestorOf(view))
    	{
    		MainFrame.panel2.remove(view);
    		//System.out.println("removing");    		
    	}
        if(MainFrame.panel2.isAncestorOf(nwkView))
        {
        	MainFrame.panel2.remove(nwkView);
        	//System.out.println("removing");
        }
        //System.out.println("In Create Tree method, "+MainFrame.panel2.getComponentCount());
        
    	graph = new SingleGraph(Region+" "+ISPName+" "+Status);
    	
    	computeGraph(graph, version, Region,  ISPName,  Status);
    	
		Viewer viewer = graph.display(false);
		viewer.enableAutoLayout();
		view = viewer.addDefaultView(false);  
		view.setSize(500, 400);
		
		//to arrange the nodes in the new UI Layout, calculate the distance of the nodes from Source node- ISPName
    	Dijkstra dijkstra = new Dijkstra(Element.EDGE,"ui.label",null);
    	dijkstra.init(graph);

    	dijkstra.setSource(graph.getNode(ISPName));
    	if(graph.getNode(ISPName)!=null){ 
    		dijkstra.compute();
    		//int maxDist = dijkstra.maxPathLength() ??
    		//ArrayList<Integer> yPos = new ArrayList<Integer>();
    		
    		HashMap<Integer, Integer> yPos = new HashMap<Integer, Integer>();
    		Iterator<Node> nodeIter = graph.getNodeIterator();
    		while(nodeIter.hasNext())
    		{
    			Node n = nodeIter.next();
    			double dist =  dijkstra.getPathLength(n);
    			n.addAttribute("layout.frozen");
    			//System.out.println(n.getId());
    			n.addAttribute("ui.label", n.getId());
    		
    			int yPosition = 0;
    			if(yPos.containsKey((int)dist))
				{
    				yPosition = 1 + yPos.get((int)dist);
    				yPos.remove((int)(dist));
    				yPos.put((int)dist, yPosition);
				}
    			else
    			{
    				yPos.put((int)dist, 0);
    			}
    			n.addAttribute("xy",(dist/20) * view.getWidth(),((double)yPosition/20) * view.getHeight() );
    		
    		}
    		
    	}
		view.addComponentListener(new ComponentListener()
				{
					public void componentResized(ComponentEvent arg0) {
						view.setSize(500, 400);
						view.setLocation(0, 0);
						//TODO ??
						
					}

					public void componentHidden(ComponentEvent arg0) {
						
					}

					public void componentMoved(ComponentEvent arg0) {
					}

					public void componentShown(ComponentEvent arg0) {
					}

					
				});
	
		
		MainFrame.panel2.add(view);
		
		MainFrame.panel3.setVisible(true);
		MainFrame.zoomSlider.setValue(0);
		
		File f = new File("input.txt");
		f.delete();
				
		
	}
    */

    public static void createTree(int version, String Region, String ISPName, String Status) throws ClassNotFoundException, SQLException, NoResultException, IOException
    {
    	if(MainFrame.panel2.isAncestorOf(viewPath))
        {
        	MainFrame.panel2.remove(viewPath);
        }
        if(MainFrame.panel2.isAncestorOf(view))
    	{
    		MainFrame.panel2.remove(view);
    	}
        if(MainFrame.panel2.isAncestorOf(GraphClass.nwkView))
        {
        	MainFrame.panel2.remove(GraphClass.nwkView);
        }
    	graph = new SingleGraph(Region+" "+ISPName+" "+Status);
    	
    	computeGraph(graph, version, Region,  ISPName,  Status);
    	
		Viewer viewer = graph.display(false);
		viewer.enableAutoLayout();
		view = viewer.addDefaultView(false);  
		view.setSize(500, 400);

		view.addComponentListener(new ComponentListener()
				{
					public void componentResized(ComponentEvent arg0) {
						view.setSize(500, 400);
						view.setLocation(0, 0);
					}

					public void componentHidden(ComponentEvent arg0) {
						
					}

					public void componentMoved(ComponentEvent arg0) {
					}

					public void componentShown(ComponentEvent arg0) {
					}

					
				});

		if(viewPath!=null)
			viewPath.setVisible(false);
		MainFrame.panel2.add(view);
		
		MainFrame.panel3.setVisible(true);
		MainFrame.zoomSlider.setValue(0);
		
		File f = new File("input.txt");
		f.delete();
		
		
	}
    
    
    /**
	* Creates the intermediate file
	* @param version Version chosen by user
	* @param Region Region chosen by user
	* @param ISPName Name of ISP chosen by user
	* @param Status Status of data chosen by user
     * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	 * @throws NoResultException Thrown if no data is found.
	 * @throws IOException Thrown if there is an error with the intermediate file.
	 * 
	* 
	*/
	private static void createFile(int version, String Region, String ISPName, String Status) throws ClassNotFoundException, SQLException, NoResultException, IOException
    {

			Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("input.txt"), "utf-8"));
	        StringBuffer PathString=new StringBuffer();
	   	    ResultSet rs=GetAllPaths(version, Region, ISPName, Status);
	   	    
			if(rs.next())
			{		
				do
				{
					String[] parts = GetPath(rs);			

				    for(int i=0;i<parts.length;i++)
				    {
				    	if(i!=0)
				    	PathString.append(" ");
				    	PathString.append(parts[i]);
				    	//System.out.print(parts[i]+" ");
				    }
				    
				    PathString.append('\n');
				    fileWriter.write(PathString.toString());
				    PathString = new StringBuffer();
				    
				}
				while(rs.next());
				
			}
			//else	throw new NoResultException();
			
			fileWriter.close();
		
    }
    
    /**
     * 
     * @param source AS in the shortest path
     * @param dest - destination AS in the shortest path
     * uses Dijkstra's algorithm to compute the shortest path.
     * @throws IOException 
     * @throws NoResultException 
     * @throws SQLException 
     * @throws ClassNotFoundException 
     */
	
	public static void getShortestPath(String region,String source,String dest) throws ClassNotFoundException, SQLException, NoResultException, IOException
   // public static void main(String args[]) throws ClassNotFoundException, SQLException, NoResultException, IOException
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
            
            int hops =0;
            String[] hopsAS = null ;
            ResultSet rs;
            rs = stat.executeQuery("select * from shortestpathDB where region='"+region+"' and source='"+source+"'and dest ='"+dest+"'");
            boolean pathFound = false;
            while(rs.next())
            {
            	//System.out.println("queried DB. "+"select * from shortestpathDB where region='"+region+"' and source='"+source+"'and dest ='"+dest+"'");
            	hops = Integer.parseInt(rs.getString("hops"));
            	hopsAS = new String[hops+2];
            	for(int i=0;i<hops+1;i++)
            	{
            		hopsAS[i+1] = rs.getString("hop"+(i+1));
            		//System.out.println(hopsAS[i+1]);
            	}
            	pathFound = true;
            }
            if(!pathFound)
            	new NotifyUser("No path exists!");
            else{
            String id0 = source;
    		displayPath = new SingleGraph(source+"-"+dest);

    		for(int i=0;i<hops;i++)//get the data for the path by querying the database.(shortestpathDB table)
    		{
    		String id1 = hopsAS[i+2];
    		//computeGraph(displayPath,1, region,  id1,  "Sent");
    		//computeGraph(displayPath,1, region,  id1,  "Received");

    		DBTest.addNode(displayPath,id0);
    		DBTest.addNode(displayPath,id1);
    		
    		DBTest.addEdge(displayPath , id0 , id1);
    		id0 = id1;
    	    }

	        //Connection conn = DriverManager.getConnection("jdbc:h2:~/COP", "sa", "");
			Connection conn1 = DriverManager.getConnection("jdbc:h2:file:"+userHome, "sa", "");
			
    		
            Statement stat1 = conn1.createStatement();
    		int version= 1;
    		ResultSet rs1 = stat1.executeQuery("select max(version) from versiondb");
        	while(rs1.next())
        	{
        		version=Integer.parseInt(rs1.getString("max(version)"));
        		//System.out.println("ver "+version);
        	}
        	//System.out.println("computed version- max "+version);
        	conn1.close();
    		//System.out.println("version "+version);
    		//System.out.println("computing the rest of the graph");
        	computeGraph( displayPath,version, region, source,  "Sent");
    		computeGraph( displayPath,version, region, source,  "Received");
    		//System.out.println("first graph : "+displayPath.getEdge("ASN-BSNL"));
    		
    		displayPath.getNode(source).setAttribute("ui.style","fill-color: rgb(0,240,0);");
    		
		if(MainFrame.panel2.isAncestorOf(GraphClass.viewPath))
        {
        	MainFrame.panel2.remove(GraphClass.viewPath);
        	
        }
    	if(MainFrame.panel2.isAncestorOf(GraphClass.view))
        {
    		//System.out.println("Removing view JPanel");
    		MainFrame.panel2.remove(GraphClass.view);
        }
    	if(MainFrame.panel2.isAncestorOf(GraphClass.nwkView))
        {
        	MainFrame.panel2.remove(GraphClass.nwkView);
        }
    	Viewer viewer = displayPath.display(false);
    	//Viewer viewer = DBTest.printNetworkPath().display(false);
		viewer.enableAutoLayout();
		
		viewPath = viewer.addDefaultView(false);
		viewPath.setSize(500, 400);
		
	
		viewPath.addComponentListener(new ComponentListener()
		{
			public void componentResized(ComponentEvent arg0) {
				viewPath.setSize(500, 400);
				viewPath.setLocation(0, 0);
				//System.out.println("viewPath view resized : "+arg0);
			}

			public void componentHidden(ComponentEvent arg0) {
			}

			public void componentMoved(ComponentEvent arg0) {
			}

			public void componentShown(ComponentEvent arg0) {
			}

			
		});
	
		
		MainFrame.panel2.add(viewPath);
    	}
        conn.close();
    	}
    	
/*TODO 
 * this is to print the following
 * source -> destination  = source -> IXP (rev) + IXP -> dest.
 * and compare with the path computed by Dijkstra's algo.
 */
		
    	
    
/***
 * 
 * @param graph 
 * @param graph the computed graph. 
 * @param version the Database version number
 * @param Region - the NIXI region chosen
 * @param ISPName- the source in the graph.
 * @param Status whether we are analyzing sent/received data.
 * @throws IOException 
 * @throws NoResultException 
 * @throws SQLException 
 * @throws ClassNotFoundException 
 */
	static void computeGraph(SingleGraph graph, int version, String Region, String ISPName, String Status) 
			throws ClassNotFoundException, SQLException, NoResultException, IOException,EdgeRejectedException 
	{
    	createFile(version, Region,  ISPName,  Status);
    	String strLine;
		
    	FileInputStream fstream = new FileInputStream("input.txt");	    	
		DataInputStream in = new DataInputStream(fstream);
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		graph.setAutoCreate(true);
		final double EdgeMultiple = 0.06; 
		
		if(graph.getNode(ISPName)==null)
		{	
			graph.addNode(ISPName);
			//System.out.println("added node "+ISPName);
		}
		
		graph.getNode(ISPName).addAttribute("ui.label", ISPName);
		graph.getNode(ISPName).addAttribute("ui.style", "fill-color: rgb(240,0,0);");
		int rank;
		
		while ((strLine = br.readLine()) != null) 
		{
			String[] parts = strLine.split(" ");
			String First = GetASName(parts[0]);
			
			if(graph.getNode(First)==null)
    		{
    			graph.addNode(First);
    			if(Status.equalsIgnoreCase("Sent"))
    			{
    				try {
						graph.addEdge(ISPName+First, ISPName, First, true);
						//System.out.print("edge -"+(ISPName)+(First));
					} catch (EdgeRejectedException e) {
						//e.printStackTrace();
						//System.out.print("graph already has edge -"+graph.getEdge(ISPName+First)!=null);
						//System.out.println(e.getMessage());
					}
    			}
    			else
    			{
    				try {
						graph.addEdge(ISPName+First, First, ISPName, true);
						//System.out.print("edge -"+First+ISPName);
					} catch (EdgeRejectedException e) {
						//e.printStackTrace();
						//System.out.print("graph already has edge -"+graph.getEdge(ISPName+First)!=null);
						//System.out.println(e.getMessage());
					}
    			}
    			graph.getNode(First).addAttribute("ui.style", "fill-color: rgb(240,0,0);");
    			graph.getNode(First).addAttribute("ui.label", First);
    		}
			
			rank=0;

		    for(int i=1;i<parts.length;i++)
		    {
		    	String current = GetASName(parts[i]);
		    	String previous = GetASName(parts[i-1]);
		    	if(current!=previous)
		    	{
		    		if(graph.getNode(current)==null)
		    		{
		    			
		    			graph.addNode(current);
		    			//System.out.println("added node "+current);
		    			rank++;
		    			graph.getNode(current).addAttribute("ui.label", current);
		    			graph.getNode(current).addAttribute("ui.style", "fill-color: rgb("+Integer.toString(240-(rank*60))+","+Integer.toString((rank*60))+","+Integer.toString((rank*60))+");");
		    		}
		    		
				    if(graph.getNode(previous)==null)
				    {
				    	graph.addNode(previous);
				    	//System.out.println("added node "+previous);
				    	rank++;
				    	graph.getNode(previous).addAttribute("ui.label", previous);
		    			graph.getNode(previous).addAttribute("ui.style", "fill-color: rgb("+Integer.toString(240-(rank*60))+","+Integer.toString((rank*60))+","+Integer.toString((rank*60))+");");
				    }
		    		
				    if(graph.getEdge(previous+"-"+current)==null)
				    {
				    	String EdgeName = previous+"-"+current;
				    	if(Status.equalsIgnoreCase("Sent"))
				    	{
				    		try {
								graph.addEdge(EdgeName, previous, current, true);
								
								//System.out.print("edge -"+(previous)+current);
								if(graph.getEdge(EdgeName)!=null)
									{
									graph.getEdge(EdgeName).addAttribute("ui.style", "size: "+ Double.toString(EdgeMultiple) +";");
									graph.getEdge(EdgeName).addAttribute("fill-color",  "rgb(0,0,0);");  
									graph.getEdge(EdgeName).addAttribute("count", 1);
									}
						        
							} catch (EdgeRejectedException e) {
								//e.printStackTrace();
								//System.out.print("graph already has edge -"+graph.getEdge(EdgeName)!=null);
								//System.out.println(e.getMessage());
							}
				    	}
				    	else
				    	{
				    		try {
								graph.addEdge(EdgeName, current, previous, true);
								//System.out.print("edge -"+(current)+previous);
								
								if(graph.getEdge(EdgeName)!=null){
								graph.getEdge(EdgeName).addAttribute("ui.style", "size: "+ Double.toString(EdgeMultiple) +";");
								graph.getEdge(EdgeName).addAttribute("fill-color",  "rgb(0,0,0);");  
								graph.getEdge(EdgeName).addAttribute("count", 1);
								}
								
							} catch (EdgeRejectedException e) {
								//e.printStackTrace();
								//System.out.print("graph already has edge -"+graph.getEdge(EdgeName)!=null);
								//System.out.println(e.getMessage());
							}
				    	}
				    	
				    }
				    else
				    {
				    	String EdgeName = previous+"-"+current;
				    	int EdgeCount = Integer.parseInt(graph.getEdge(EdgeName).getAttribute("count").toString());
				    	EdgeCount++;
				    	if(graph.getEdge(EdgeName)==null){
					    	graph.getEdge(EdgeName).setAttribute("count", EdgeCount);
					    	graph.getEdge(EdgeName).addAttribute("fill-color",  "rgb(0,0,0);");  
					    	graph.getEdge(EdgeName).setAttribute("ui.style", "size: "+ Double.toString(EdgeMultiple*EdgeCount) +";");
					    }
				    	
				    }
		    	}
		    }
		}
		//System.out.println("edges "+graph.getEdgeCount());
	}

public static void addNwkView(String source, String dest) 
{
	graphNwk  = new SingleGraph("nwk"+source+" "+dest);
	graphNwk = DBTest.printNetworkPath(source, dest);
	
	if(MainFrame.panel2.isAncestorOf(GraphClass.viewPath))
    {
    	MainFrame.panel2.remove(GraphClass.viewPath);
    }
	if(MainFrame.panel2.isAncestorOf(GraphClass.view))
    {
    	MainFrame.panel2.remove(GraphClass.view);
    }
	if(MainFrame.panel2.isAncestorOf(nwkView))
    {
    	MainFrame.panel2.remove(GraphClass.nwkView);
    }
	
	
	Viewer viewer = graphNwk.display(false);
	viewer.enableAutoLayout();
	nwkView = viewer.addDefaultView(false);  
	nwkView.setSize(500, 400);
	
	
	nwkView.addComponentListener(new ComponentListener()
	{
		public void componentResized(ComponentEvent arg0) {
			nwkView.setSize(500, 400);
			nwkView.setLocation(0, 0);
		}

		public void componentHidden(ComponentEvent arg0) {
		}

		public void componentMoved(ComponentEvent arg0) {
		}

		public void componentShown(ComponentEvent arg0) {
		}
		
	});
	if(graphNwk != null && GraphTools.connectedComponents(graphNwk) == 1)
		MainFrame.panel2.add(GraphClass.nwkView);
		    		
	
}

}
