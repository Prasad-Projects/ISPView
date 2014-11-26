package cop;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.httpclient.HttpException;


/**
 * Defines a parallel thread which updates the database in the background.
 * @author indu
 *
 */
class UpdateThread implements Runnable 
{
	   Thread t;
	   UpdateThread() 
	   {
	      t = new Thread(this, "Update Thread");
	      t.start(); 
	   }
	   
	   public void run() 
	   {
	      try 
	      {
	    	  new NotifyUser("This process may take a long time to complete.");
	    	  ToolsClass.NIXIUpdate();
	    	  new NotifyUser("Update Completed");
	      } 
	      catch (HttpException h) 
	      {
	         new NotifyUser("Connection Error");
	      }
	      catch (IOException i) 
	      {
	         new NotifyUser("Connection Error");
	      }
	      catch (SQLException s) 
	      {
	         new NotifyUser("DataBase Error");
	      } 
	      catch (ClassNotFoundException e) 
	      {
	    	  new NotifyUser("Error loading database drivers.");
	      }
	     
	   }
}

