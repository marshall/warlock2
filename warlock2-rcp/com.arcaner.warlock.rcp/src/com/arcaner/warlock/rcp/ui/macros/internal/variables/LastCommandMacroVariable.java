/*
 * Created on Mar 27, 2005
 */
package com.arcaner.warlock.rcp.ui.macros.internal.variables;

import com.arcaner.warlock.client.ICommand;
import com.arcaner.warlock.client.IWarlockClient;
import com.arcaner.warlock.client.IWarlockClientViewer;
import com.arcaner.warlock.rcp.ui.macros.IMacroVariable;

/**
 * @author Marshall
 */
public class LastCommandMacroVariable implements IMacroVariable {

	public String getIdentifier() {
		return "$lastCommand";
	}

	public String getValue(IWarlockClientViewer context) {
		
		IWarlockClient client = context.getWarlockClient();
		String currentCommand = context.getCurrentCommand();
		if (currentCommand != null && currentCommand.length() > 0)
		{
			client.getCommandHistory().addCommand(currentCommand);
			ICommand command = client.getCommandHistory().prev();
			
			if(command != null) {
				return command.getCommand();
			}
		}
		else {
			return client.getCommandHistory().current().getCommand();
		}
		
		return null;
	}

}
