package cop;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.*;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.View;

/**
 * The main frame for the applications which consists 3 panels.
 *
 */
public class MainFrame
{

	static JComboBox<String> RegionBox, ASNameBox, StatusBox, VersionBox, SourceBox, pathFromBox, pathToBox,pathRegionBox;
	static JButton OKButton, UpdateButton, PruneButton, CaptureButton, HelpButton, DocButton,OKButton2,OKButton3;
	static JSlider zoomSlider;
	static JLabel L1, L2, L3, L4, L5, L6, L7,L8,L9,L10;
	static JPanel runPanel, panel2, panel3, panel4, updatePanel,pathPanel;
	static JFrame frame;
	static JTabbedPane tabbedPane;
	static ImageIcon icon = new ImageIcon("helpicon.gif");

	static boolean DBAccess = true;
	static boolean setPath = false;
	private static JLabel L11;
	private static JTextField AScb;
	private static JTextField answerAS;
	static RegionGraphsThread thread1;
	static RegionGraphsThread thread2;
	static RegionGraphsThread thread3;
	static RegionGraphsThread thread4;
	private static JLabel Lbl1;
	private static JComboBox<String> networkFromBox;
	private static JComboBox<String> networkToBox;
	private static JLabel Lbl10;
	private static JButton OKButtonNwk;
	
	
	/**
	* Fetches all the available versions of the data set from the database.
	*  @return the max version number -i.e., the latest version of the database.
	 * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	*/
	static int getVersions() throws ClassNotFoundException, SQLException
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

        ArrayList<Integer> vers = new ArrayList<Integer>();
        ResultSet rs;
        rs = stat.executeQuery("select version from versiondb");
        while (rs.next()) 
        {
        	String ver = rs.getString("version");
        	//System.out.println(ver);
        	if(VersionBox!=null)VersionBox.addItem(ver);
        	vers.add(Integer.parseInt(ver));
        	
        }
        stat.close();
        conn.close();
        
        Collections.sort(vers);
        return vers.get(vers.size()-1);//returns the latest version
	}
	
	/**
	* Loads all the available regions.
	*/
	static void getRegions(JComboBox<String> cb)  
	{
		cb.addItem("Mumbai");
		cb.addItem("Delhi+(Noida)");
		cb.addItem("Kolkata");
		cb.addItem("Hyderabad");
	}
	
	
	/**
	* Fetches all the ISP names corresponding to the selected region and fills in the combobox with the retrieved values.
	* 
	*  @param Region The selected region
	 * @throws ClassNotFoundException Thrown if the H2 drivers aren't loaded.
	 * @throws SQLException Thrown if it the program is unable to connect to the database or execute the query.
	 * 
	*
	*/
	
	static ArrayList<String> getISPNames(String Region,JComboBox<String> cb) throws ClassNotFoundException, SQLException
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
        //rs = stat.executeQuery("select distinct shortname from ispnames, iptoasnumdb where ispnames.asnum = iptoasnumdb.asnum order by shortname");
        rs = stat.executeQuery("select distinct asname,asnum from pathdb2 where region='"+Region+"' order by asname");
        ArrayList<String> ret = new ArrayList<String>();
        Hashtable<String,Integer> table = new Hashtable<String,Integer>();
        
        while (rs.next()) 
        {
        	String asName = rs.getString("asname");
        	String asNum = rs.getString("asnum");
        	if(!table.containsKey(asName))
        	{
        		table.put(asName,Integer.parseInt(asNum));
        	}
        	else//if the ASNames are repeated, append the ASNumbers
        	{
        		Integer val = table.get(asName);
        		table.remove(asName);
        		table.put(asName+":"+val, val);
        		
        		table.put(asName+":"+asNum, Integer.parseInt(asNum));
        	}
          
		}
        //ret = (ArrayList<String>) table.keySet();
        Iterator<String> iter = table.keySet().iterator();
        while(iter.hasNext())
        {
        	String val = iter.next();
        	if(cb!= null) cb.addItem(val);
        	ret.add(val);
        }
        stat.close();
        conn.close();
        
        return ret;
	}

/**
 * 
 * @param cb the combobox that is filled up with a list of all the networks in the database.
 * @return the list of all the networks in the database
 * @throws ClassNotFoundException
 * @throws SQLException
 */
	static ArrayList<String> getAllNetworks(JComboBox<String> cb) 
	{ 
		ArrayList<String> ret = null;
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
        rs = stat.executeQuery("select distinct NETWORK from pathdb2");
        ret = new ArrayList<String>();
        while (rs.next()) 
        {
        	String val = rs.getString("NETWORK");
            if(cb!=null) cb.addItem(val);
            ret.add(val);
        }
        stat.close();
        conn.close();
        }
        catch(SQLException e)
        {
        	e.printStackTrace();
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        
        return ret;
	}
	
	/**
	* Loads the Query Panel by fetching the required the data from the database.
	*/
	static void getQueryPanel()
	{	    
	    runPanel = new JPanel();
	    runPanel.setSize(300, 500);
	    BoxLayout layout = new BoxLayout(runPanel, BoxLayout.PAGE_AXIS);
	    runPanel.setLayout(layout);

	    
	    L1 = new JLabel("Version", icon, JLabel.LEADING);
	    L1.setHorizontalTextPosition(JLabel.LEADING);
	    L1.setLocation(10, 30);
	    L1.setAlignmentX(Component.CENTER_ALIGNMENT);
	    L1.setVisible(true);
	    runPanel.add(L1);
	    
	    VersionBox = new JComboBox<String>();
		VersionBox.setVisible(true);
		runPanel.add(VersionBox);
		try 
		{
			getVersions();
		} 
		catch (SQLException e) 
		{
			new NotifyUser("Unable to fetch data at this time. Try again Later.");
			frame.dispose();
		} 
		catch (ClassNotFoundException e) 
		{
			new NotifyUser("Error loading database engine drivers.");
			frame.dispose();
		}
		
		L2 = new JLabel("Source", icon, JLabel.LEADING);
		L2.setHorizontalTextPosition(JLabel.LEADING);
		L2.setLocation(10, 130);
		L2.setAlignmentX(Component.CENTER_ALIGNMENT);
	    L2.setVisible(true);
	    runPanel.add(L2);
	    
		SourceBox = new JComboBox<String>();
		SourceBox.addItem("NIXI");
		SourceBox.setLocation(10, 160);
		SourceBox.setVisible(true);
		runPanel.add(SourceBox);
	    	    
		L3 = new JLabel("Region", icon, JLabel.LEADING);
		L3.setHorizontalTextPosition(JLabel.LEADING);
		L3.setLocation(10, 230);
		L3.setAlignmentX(Component.CENTER_ALIGNMENT);
	    L3.setVisible(true);
	    runPanel.add(L3);
	    
		RegionBox = new JComboBox<String>();
	    getRegions(RegionBox);
	    RegionBox.setLocation(10, 260);
	    RegionBox.setVisible(true);
	    RegionBox.addItemListener(new ItemListener()
	    {
	        public void itemStateChanged(ItemEvent event) 
	        {
	           if (event.getStateChange() == ItemEvent.SELECTED) 
	           {
	        	   	String region = event.getItem().toString();
		            try 
		      		{
		      			getISPNames(region,ASNameBox);
		      		} 
		            catch (SQLException e) 
		    		{
		    			new NotifyUser("Unable to fetch data at this time. Try again Later.");
		    			frame.dispose();
		    		} 
		    		catch (ClassNotFoundException e) 
		    		{
		    			new NotifyUser("Error loading database engine drivers.");
		    			frame.dispose();
		    		}
	           }
	        }       
	    });
	    runPanel.add(RegionBox);
	    
	    L4 = new JLabel("ISP Name", icon, JLabel.LEADING);
		L4.setHorizontalTextPosition(JLabel.LEADING);
	    L4.setLocation(10, 330);
	    L4.setAlignmentX(Component.CENTER_ALIGNMENT);
	    L4.setVisible(true);
	    runPanel.add(L4);
	    
	    ASNameBox = new JComboBox<String>();
	    ASNameBox.setLocation(10, 360);
		ASNameBox.setVisible(true);
		runPanel.add(ASNameBox);
		
		try 
		{
			getISPNames(RegionBox.getSelectedItem().toString(),ASNameBox);
		} 
		catch (SQLException e) 
		{
			new NotifyUser("Unable to fetch data at this time. Try again Later.");
			frame.dispose();
		} 
		catch (ClassNotFoundException e) 
		{
			new NotifyUser("Error loading database engine drivers.");
			frame.dispose();
		}
	

		L5 = new JLabel("Status", icon, JLabel.LEADING);
		L5.setHorizontalTextPosition(JLabel.LEADING);
		L5.setLocation(10, 430);
		L5.setAlignmentX(Component.CENTER_ALIGNMENT);
	    L5.setVisible(true);
	    runPanel.add(L5);
	    
		StatusBox = new JComboBox<String>();
		StatusBox.addItem("Sent");
		StatusBox.addItem("Received");
		StatusBox.setLocation(10, 460);
		StatusBox.setVisible(true);
		runPanel.add(StatusBox);
		
		OKButton = new JButton("OK");
		OKButton.setLocation(100, 500);
		OKButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		OKButton.setVisible(true);
		OKButton.addActionListener(new ActionListener() 
	    {  
	        public void actionPerformed(ActionEvent e)
	        {  
	           try 
	           {
	        	   //GraphClass G = new GraphClass();
	        	   GraphClass.createTree(Integer.parseInt(VersionBox.getSelectedItem().toString()), RegionBox.getSelectedItem().toString(), ASNameBox.getSelectedItem().toString(), StatusBox.getSelectedItem().toString());
	        	   //System.out.println("GraphClass.createTree("+Integer.parseInt(VersionBox.getSelectedItem().toString())+" , "+ RegionBox.getSelectedItem().toString()+" , " +ASNameBox.getSelectedItem().toString()+" , "+ StatusBox.getSelectedItem().toString());
	        	   //System.out.println("height of viewer "+GraphClass.view.getHeight());
	           } 
	           catch(NoResultException n)
	           {
	        	   new NotifyUser("No Data Found.");
	           }
	           catch(SQLException s)
	           {
	        	   new NotifyUser("Database error. Try again later.");
	           } 
	           catch (ClassNotFoundException e1) 
	           {
	        	   new NotifyUser("Error Loading database driver.");
	           } 
	           catch (IOException e1) 
	           {
	        	   new NotifyUser("File error");
	           }
	               
	        }  
	    });
		runPanel.add(OKButton);

	    runPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	    runPanel.setVisible(true);
	}
	
	/**
	* Loads the Tools Panel.
	*/
	static void getUpdatePanel()
	{
	    updatePanel = new JPanel();
	    updatePanel.setSize(300, 500);
	    BoxLayout layout = new BoxLayout(updatePanel, BoxLayout.PAGE_AXIS);
	    updatePanel.setLayout(layout);
		
	    
	    updatePanel.add(Box.createRigidArea(new Dimension(25,25)));
	    
		UpdateButton = new JButton("Update Database");
		UpdateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		UpdateButton.setVisible(true);
		UpdateButton.addActionListener(new ActionListener() 
	    {  
	        public void actionPerformed(ActionEvent e)
	        {  
	        	if(DBAccess==true)
	        	{
	        		DBAccess=false;
					new UpdateThread();
					new NotifyUser("Updation complete.");
		        	DBAccess=true;
	        	}else
	        	{
	        		new NotifyUser("Database cannot be updated right now as it being accesed by another process. Please try again later.");
	        	}
	        }  
	    });
		updatePanel.add(UpdateButton);
		
		updatePanel.add(Box.createRigidArea(new Dimension(50,50)));
	    
	    PruneButton = new JButton("Prune Database");
		PruneButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		PruneButton.setVisible(true);
		PruneButton.addActionListener(new ActionListener() 
	    {  
	        public void actionPerformed(ActionEvent e)
	        {  
	        	if(DBAccess==true)
	        	{
	        		DBAccess=false;
					new PruneThread();
					new NotifyUser("Pruning is complete.");
		        	DBAccess=true;
	        	}
	        	else
	        	{
	        		new NotifyUser("Database cannot be pruned right now as it being accesed by another process. Please try again later.");
	        	}
	        }  
	    });
		updatePanel.add(PruneButton);
		
  
		updatePanel.add(Box.createRigidArea(new Dimension(50,50)));
	    
		
	    updatePanel.setBorder(BorderFactory.createLineBorder(Color.black));
	    updatePanel.setVisible(true);
	}
	/**
	 * this panel has that tells us the shortest paths using Djikstra's algo and also identifies the 
	 * AS given an IP address using shortest prefix matching.
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	static void getPathPanel()
	{
	    pathPanel = new JPanel();
	    pathPanel.setSize(300, 500);
	    BoxLayout layout = new BoxLayout(pathPanel, BoxLayout.PAGE_AXIS);
	    pathPanel.setLayout(layout);
	    pathPanel.add(Box.createRigidArea(new Dimension(25,25)));
	    
	    L8 = new JLabel("Region", null, JLabel.LEADING);
	    L8.setHorizontalTextPosition(JLabel.LEADING);
	    L8.setLocation(10, 30);
	    L8.setAlignmentX(Component.CENTER_ALIGNMENT);
	    L8.setVisible(true);
	    pathPanel.add(L8);
	    
	    pathRegionBox = new JComboBox<String>();
	    getRegions(pathRegionBox);
	    pathRegionBox.setLocation(10, 260);
	    pathRegionBox.setVisible(true);
	    pathPanel.add(pathRegionBox);

	    L9 = new JLabel("Source", null, JLabel.LEADING);
	    L9.setHorizontalTextPosition(JLabel.LEADING);
	    L9.setLocation(10, 30);
	    L9.setAlignmentX(Component.CENTER_ALIGNMENT);
	    L9.setVisible(true);
	    pathPanel.add(L9);
	    pathFromBox = new JComboBox<String>();
	    pathFromBox.setLocation(10, 360);
	    pathFromBox.setVisible(true);
		pathPanel.add(pathFromBox);
	    
	    String region = pathRegionBox.getSelectedItem().toString();
		try {
			getISPNames(region, pathFromBox);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("path regionBox error");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("path regionBox error");
		}


		L10 = new JLabel("Destination", null, JLabel.LEADING);
	    L10.setHorizontalTextPosition(JLabel.LEADING);
	    L10.setLocation(10, 30);
	    L10.setAlignmentX(Component.CENTER_ALIGNMENT);
	    L10.setVisible(true);
	    pathPanel.add(L10);
		pathToBox = new JComboBox<String>();
	    pathToBox.setLocation(10, 360);
	    pathToBox.setVisible(true);
		pathPanel.add(pathToBox);
		try {
			getISPNames(region, pathToBox);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("path regionBox error");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("path regionBox error");
		}
		
	    pathRegionBox.addItemListener(new ItemListener()
	    {
	        public void itemStateChanged(ItemEvent event) 
	        {
	           if (event.getStateChange() == ItemEvent.SELECTED) 
	           {
	        	    
	        	   	String region = event.getItem().toString();
		            try 
		      		{
		      			getISPNames(region,pathFromBox);
		      			getISPNames(region,pathToBox);
		      		} 
		            catch (SQLException e) 
		    		{
		    			new NotifyUser("Unable to fetch data at this time. Try again Later.");
		    			frame.dispose();
		    		} 
		    		catch (ClassNotFoundException e) 
		    		{
		    			new NotifyUser("Error loading database engine drivers.");
		    			frame.dispose();
		    		}
	           }
	        }       
	    });

		OKButton2 = new JButton("OK");
		OKButton2.setLocation(100, 500);
		OKButton2.setAlignmentX(Component.CENTER_ALIGNMENT);
		OKButton2.setVisible(true);
		OKButton2.addActionListener(new ActionListener() 
	    {  
	        public void actionPerformed(ActionEvent e)
	        {  
	        	
	       		String source = pathFromBox.getSelectedItem().toString();
	       		String dest = pathToBox.getSelectedItem().toString();
	       		String region = pathRegionBox.getSelectedItem().toString();
	       		
	       		if(source!=null&& dest!=null)
	       		{
	       			try {
	       			GraphClass.getShortestPath(region, source, dest);
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					} catch (SQLException e1) {
						e1.printStackTrace();
					} catch (NoResultException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
	       		}  
	        }  
	    }); 
		pathPanel.add(OKButton2);
	   
		///////////////////////////////////////TODO

	    Lbl1 = new JLabel("Source Network", null, JLabel.LEADING);
	    Lbl1.setHorizontalTextPosition(JLabel.LEADING);
	    Lbl1.setLocation(10, 30);
	    Lbl1.setAlignmentX(Component.CENTER_ALIGNMENT);
	    Lbl1.setVisible(true);
	    pathPanel.add(Lbl1);
	    networkFromBox = new JComboBox<String>();
	    networkFromBox.setLocation(10, 360);
	    networkFromBox.setVisible(true);
	    getAllNetworks(networkFromBox);
	    pathPanel.add(networkFromBox);
		

		Lbl10 = new JLabel("Destination Network", null, JLabel.LEADING);
	    Lbl10.setHorizontalTextPosition(JLabel.LEADING);
	    Lbl10.setLocation(10, 30);
	    Lbl10.setAlignmentX(Component.CENTER_ALIGNMENT);
	    Lbl10.setVisible(true);
	    pathPanel.add(Lbl10);
		networkToBox = new JComboBox<String>();
		networkToBox.setLocation(10, 360);
		networkToBox.setVisible(true);
		getAllNetworks(networkToBox);
		pathPanel.add(networkToBox);
		

		OKButtonNwk = new JButton("Find Path");
		OKButtonNwk.setLocation(100, 500);
		OKButtonNwk.setAlignmentX(Component.CENTER_ALIGNMENT);
		OKButtonNwk.setVisible(true);
		OKButtonNwk.addActionListener(new ActionListener() 
	    {  
			public void actionPerformed(ActionEvent e)
	        {  
				String source = networkFromBox.getSelectedItem().toString();
				String dest = networkToBox.getSelectedItem().toString();
	        	
	       		//Graph has to have only one connected component, otherwise - no path exists between the two autonomous systems
	       		
				GraphClass.addNwkView(source, dest);
		       	
	        }
	    });
		pathPanel.add(OKButtonNwk);
		
		
		L11 = new JLabel("Find AS:Enter IP", null, JLabel.LEADING);
	    L11.setHorizontalTextPosition(JLabel.LEADING);
	    L11.setLocation(10, 30);
	    L11.setAlignmentX(Component.CENTER_ALIGNMENT);
	    L11.setVisible(true);
	    pathPanel.add(L11);
	    
	    AScb = new JTextField();
	    AScb.setLocation(10,30);
	    AScb.setBounds(0, (int) (L11.getAlignmentY()+L11.getHeight()),200, 200);
	    AScb.setVisible(true);
	    pathPanel.add(AScb);
	    
	    answerAS = new JTextField();
	    answerAS.setLocation(10,30);
	    answerAS.setBounds(0, (int) (AScb.getAlignmentY()+AScb.getHeight()),200,200);
	    answerAS.setEditable(false);
	    answerAS.setVisible(true);
	    pathPanel.add(answerAS);

		OKButton3 = new JButton("OK");
		OKButton3.setLocation(100, 500);
		OKButton3.setAlignmentX(Component.CENTER_ALIGNMENT);
		OKButton3.setVisible(true);
		OKButton3.addActionListener(new ActionListener() 
	    {  
	        public void actionPerformed(ActionEvent e)
	        {  
	        	answerAS.setText("Ans:- "+PatriciaTest.getASFromIP(AScb.getText()));
	        }  
	    }); 
		pathPanel.add(OKButton3);
			
	}
	
	/**
	* Loads Panel1.
	*/
	static void getPanel1()
	{
		getQueryPanel();
	    getUpdatePanel();
	    getPathPanel();
	    
	    runPanel.setBackground(Color.decode("#7A71DE"));
	    updatePanel.setBackground(Color.decode("#7A71DE"));
	    pathPanel.setBackground(Color.decode("#7A71DE"));
		
		tabbedPane = new JTabbedPane();
	    tabbedPane.setBounds(0, 0, 300, 500);
		tabbedPane.addTab( "Run a Query", runPanel );
		tabbedPane.addTab( "Tools", updatePanel );
		tabbedPane.addTab("Paths",pathPanel);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
		        //int selectedIndex = tabbedPane.getSelectedIndex();
		        //System.out.println("selectedIndex = " + selectedIndex);
				
		        frame.setBounds(frame.getBounds());
			}
		});
	}
	
	/**
	* Loads Panel2.
	*/
	static void getPanel2()
	{
		panel2 = new JPanel();
	    panel2.setBounds(300, 0, 500, 400);
	    panel2.setVisible(true);
	    
	    
	}
	
	/**
	* Loads Panel3.
	*/
	static void getPanel3()
	{
		panel3 = new JPanel();
	    panel3.setBounds(300, 400, 500, 100);
	    BoxLayout layout = new BoxLayout(panel3, BoxLayout.X_AXIS);
	    panel3.setLayout(layout);
	    
	    zoomSlider = new JSlider(100,200,100);
	    zoomSlider.setMajorTickSpacing(10);
	    zoomSlider.setPaintTicks(true);
	    zoomSlider.setPaintLabels(true);
	    zoomSlider.addChangeListener(new ChangeListener() 
	    {
	        public void stateChanged(ChangeEvent ce) 
	        {
	        	JSlider source = (JSlider)ce.getSource();
	            if (!source.getValueIsAdjusting()) 
	            {
	            	int percent = source.getValue();
	            	double viewPercent= (200.0-percent)/(100);
	            	//GraphClass.Zoom(viewPercent);
	            	Component [] components = panel2.getComponents();
			        for(int i=0;i<components.length;)
			        {
			        	Component c = components[i];
			        	((View) c).getCamera().setViewPercent(viewPercent);//zoom!
			        	break;
			        }
	            }
	        }
	    });
	    zoomSlider.setVisible(true);
	    
		CaptureButton = new JButton("Capture");
		CaptureButton.setVisible(true);
		CaptureButton.addActionListener(new ActionListener() 
	    {  
	        public void actionPerformed(ActionEvent e)
	        {  
	        	SingleGraph g = null;
	        	if(MainFrame.panel2.isAncestorOf(GraphClass.viewPath))
	            {
	        		g = GraphClass.displayPath;
	            }
	            if(MainFrame.panel2.isAncestorOf(GraphClass.view))
	        	{
	            	g = GraphClass.graph;
	        	}
	            if(MainFrame.panel2.isAncestorOf(GraphClass.nwkView))
	            {
	            	g = GraphClass.graphNwk;
	            }
	            if(g != null)
	        		GraphClass.SnapShot(g);
	        	new NotifyUser("Screenshot has been saved.");
	        }  
	    });
		panel3.add(CaptureButton);
	    
		HelpButton = new JButton("Help");
		HelpButton.setVisible(true);
		HelpButton.addActionListener(new ActionListener() 
	    {  
	        public void actionPerformed(ActionEvent e)
	        {  
	        	new NotifyUser("The bright red node is the one you have chosen to see the information about. The duller the node, the further away it is from your chosen node.\n The thickness of an edges is proportional to the amount of traffic through it.\n Use the slider to zoom in and out of the graph.\n If you want to take snapshots of the graph to study later, use the capture button.");
	        }  
	    });
		panel3.add(HelpButton);
		panel3.add(zoomSlider);
	    
		//panel3.setVisible(false);
	    panel3.setBackground(Color.decode("#7A71DE"));
	    }
	
	/**
	* Loads Panel4.
	*/
	static void getPanel4()
	{
		panel4 = new JPanel();
	    panel4.setSize(0, 0);
	    panel4.setVisible(true);
	    panel4.setBackground(Color.decode("#AAC2E3"));

	}
	
	public static void main(String[] args) 
	{
	    frame = new JFrame("ISPView - India");
	    
	    frame.addComponentListener(new ComponentListener()
	    		{
					public void componentHidden(ComponentEvent arg0) {
						
					}
					public void componentMoved(ComponentEvent arg0) {
						
					}					
					@Override 
					public void componentResized(ComponentEvent arg0) {
						
						//System.out.println("frame resized!! ");
						float height = frame.getHeight();
						float width = frame.getWidth();
						
						try {
							
							//System.out.println("frame resized");
							int tabbedPaneWidth = (int)(0.3*width);
							if(tabbedPane != null)
							{
								tabbedPane.setBounds(0,0,tabbedPaneWidth,(int)(0.8*height));
								
								panel2.setBounds((int)(3*width/8),0,(int)((width-tabbedPaneWidth)*0.8),(int)(height*0.7));								
								panel3.setBounds((int)(3*width/8),(int)(panel2.getHeight()),(int)((width-tabbedPaneWidth)*0.8),(int)(panel2.getHeight()*0.3) );
							}
														
						} catch (Exception e) 
						{
							e.printStackTrace();
							//do nothing ; this exception shall be raised the first time when the panels aren't loaded.
						}
						
					}
					public void componentShown(ComponentEvent arg0) {
						
					}
	    		
	    		});
	    frame.setSize(900, 600);
	    frame.setLocation(200,70);
	    
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    

	    getPanel1();
	    getPanel2();
	    getPanel3();
	    getPanel4();
	    
	    Container content = frame.getContentPane();
	    
	    content.add(tabbedPane);
	    content.add(panel2);
	    content.add(panel3);
	    content.add(panel4);
	    
	    content.setVisible(true);
	    frame.setVisible(true);
	    //new ResizeThread():
	}


	
}
