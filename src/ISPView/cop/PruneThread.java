package cop;

import java.sql.SQLException;

/**
 * Defines a parallel thread which prunes the database as a background process.
 *
 */
class PruneThread implements Runnable 
{
	   Thread t;
	   PruneThread() 
	   {
	      t = new Thread(this, "Prune Thread");
	      t.start();
	   }
	   
	   public void run() 
	   {
	      try 
	      {
	    	  ToolsClass.NIXIPrune();
	    	  new NotifyUser("Pruning Completed");
	      }
	      catch (SQLException s) 
	      {
	         new NotifyUser("DataBase Error");
	      }
	      catch (Exception e)
	      {
	    	  new NotifyUser("Error ocurred during Updating. Try Later.");
	      }
	   }
}
