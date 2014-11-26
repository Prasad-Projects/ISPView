package cop;

import java.io.IOException;
import java.sql.SQLException;

import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphThread implements Runnable
{
	Thread t;
	SingleGraph graph;
	int version;
	String region, ISPName, status;
	//computeGraph( pathGraph, currentversion, region,  ISPName,  "Sent");	
	GraphThread(SingleGraph graph, int version,String region, String ISPName, String status)
	{
		this.graph = graph;
		this.version = version;
		this.region = region;
		this.ISPName = ISPName;
		this.status = status;
		
		
		t = new Thread(this,"Graph Thread "+region+" "+ISPName+""+status);
	    t.start();
	}
	public void run()
	{
		System.out.println("computing graph for region "+region);
		try {
			GraphClass.computeGraph( graph, version, region,  ISPName, status);
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
		synchronized(this) {
			GraphClass.finishedThreads++;
          }
		
		/*try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}
}
