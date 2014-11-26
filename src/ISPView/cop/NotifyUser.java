package cop;


import javax.swing.JOptionPane;

/**
 * Used to display messages to the user.
 *
 */
public class NotifyUser 
{

	static String message;
	
	public NotifyUser(String M)
	{
		JOptionPane.showMessageDialog(MainFrame.frame, M);
	}


}
