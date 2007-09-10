package cc.warlock.rcp.util;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import cc.warlock.client.IWarlockStyle;
import cc.warlock.client.internal.WarlockStyle;
import cc.warlock.client.stormfront.IStormFrontClient;
import cc.warlock.network.SGEConnection;
import cc.warlock.rcp.application.WarlockApplication;
import cc.warlock.rcp.plugin.Warlock2Plugin;
import cc.warlock.rcp.ui.network.SWTConnectionListenerAdapter;
import cc.warlock.rcp.views.DebugView;
import cc.warlock.rcp.views.GameView;


/**
 * This utility class is for anything login (SGE) related
 * @author marshall
 */
public class LoginUtil {

	public static void connect (GameView gameView, Map<String,String> loginProperties)
	{
		String server = loginProperties.get("GAMEHOST");
		int port = Integer.parseInt (loginProperties.get("GAMEPORT"));
		String key = loginProperties.get("KEY");
		IStormFrontClient client = Warlock2Plugin.getDefault().createNextClient();
		gameView.setStormFrontClient(client);
		
		try {
			client.connect(server, port, key);
			
			if (WarlockApplication.instance().inDebugMode())
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(DebugView.VIEW_ID);
				
				client.getConnection().addConnectionListener(new SWTConnectionListenerAdapter(DebugView.instance()));
			}
			
		} catch (IOException e) {
			String errorConnectMessage =
			"******************************************************************\n" +
			"* The connection was refused, possibly meaning the server is currently down,\n" +
			"* or your internet connection is not active\n" +
			"******************************************************************\n";
			
			IWarlockStyle style = WarlockStyle.createCustomStyle("mono", 0, errorConnectMessage.length());
			style.addStyleType(IWarlockStyle.StyleType.MONOSPACE);
			
			client.getDefaultStream().send(errorConnectMessage, style);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
	
	public static void connectAndOpenGameView (Map<String,String> loginProperties)
	{
		connect (GameView.createNext(), loginProperties);
	}
	
	public static void showAuthenticationError (int status)
	{
		String message = "Warlock was unable to log your account in due to the following error: ";
		message += getAuthenticationError(status);
		
		String title = "Login Error: ";
		
		switch (status)
		{
			case SGEConnection.ACCOUNT_REJECTED:
			{
				title += "Account Rejected";
			} break;
			case SGEConnection.INVALID_ACCOUNT:
			{
				title += "Invalid Account";
			} break;
			case SGEConnection.INVALID_PASSWORD:
			{
				title += "Wrong Password";
			} break;
		}
		
		if (title != null && message != null)
			MessageDialog.openError(Display.getDefault().getActiveShell(), title, message);
	}
	
	public static String getAuthenticationError (int status)
	{
		switch (status)
		{
			case SGEConnection.ACCOUNT_REJECTED:
				return "The account was rejected by the server.";
			case SGEConnection.INVALID_ACCOUNT:
				return "The account was not recognized by the server.";
			case SGEConnection.INVALID_PASSWORD:
				return "The password you entered was incorrect.";
		}
		
		return null;
	}
}
