package cop;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.implementations.SingleGraph;

public class RegionGraphsThread extends Thread
{
	Thread t;
	String region;
	boolean finishedThread ;
	//computeGraph( pathGraph, currentversion, region,  ISPName,  "Sent");	
	RegionGraphsThread(String region)
	{
		this.region = region;
		
		t = new Thread(this,"Graph Thread "+region);
	    t.start();
	}
	public void run()
	{	
		synchronized(this){
		finishedThread = false;
		System.out.println("computing graph for region "+region);
		}
		
		try {
			ArrayList<String> ISPs = new ArrayList<String>();
			
			ISPs = MainFrame.getISPNames(region, null);
			int currentversion = MainFrame.getVersions();//gets the latest version of the database which we shall use for in this method.
			if(region.equals("Mumbai"))
			{
			 	GraphClass.pathGraphMumbai = new SingleGraph(region);
				
				for(String ISPName : ISPs)
				{
					GraphClass.computeGraph( GraphClass.pathGraphMumbai, currentversion, region,  ISPName,  "Sent");	
					GraphClass.computeGraph( GraphClass.pathGraphMumbai, currentversion, region,  ISPName,  "Received");
				}
			}
			else if(region.equals("Delhi+(Noida)"))
			{
			 	GraphClass.pathGraphDelhi = new SingleGraph(region);
				
				for(String ISPName : ISPs)
				{
					GraphClass.computeGraph( GraphClass.pathGraphDelhi, currentversion, region,  ISPName,  "Sent");	
					GraphClass.computeGraph( GraphClass.pathGraphDelhi, currentversion, region,  ISPName,  "Received");
				}
			}
			else if(region.equals("Kolkata"))
			{
			 	GraphClass.pathGraphKolkata = new SingleGraph(region);
				
				for(String ISPName : ISPs)
				{
					GraphClass.computeGraph( GraphClass.pathGraphKolkata, currentversion, region,  ISPName,  "Sent");	
					GraphClass.computeGraph( GraphClass.pathGraphKolkata, currentversion, region,  ISPName,  "Received");
				}
			}
			else if(region.equals("Hyderabad"))
			{
			 	GraphClass.pathGraphHyd = new SingleGraph(region);
				
				for(String ISPName : ISPs)
				{
					GraphClass.computeGraph( GraphClass.pathGraphHyd, currentversion, region,  ISPName,  "Sent");	
					GraphClass.computeGraph( GraphClass.pathGraphHyd, currentversion, region,  ISPName,  "Received");
				}
			}
			File f = new File("input.txt");
			f.delete();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (EdgeRejectedException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NoResultException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		synchronized(this){
		System.out.println("FINISHED computing graph for region "+region);
		finishedThread = true;
		}
	}
	public boolean isRunning()
	{
		return !finishedThread;
	}
	
}
