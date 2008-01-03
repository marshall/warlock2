package cc.warlock.rcp.ui.macros.internal;

import java.util.List;

import org.eclipse.swt.SWT;

import cc.warlock.core.client.IWarlockClientViewer;
import cc.warlock.core.script.IScript;
import cc.warlock.core.script.ScriptEngineRegistry;
import cc.warlock.rcp.ui.macros.IMacro;
import cc.warlock.rcp.ui.macros.IMacroHandler;

public class SystemMacroHandler implements IMacroHandler {

	private IMacro sendMacro, stopScript;
	
	public SystemMacroHandler ()
	{
		sendMacro = new Macro(SWT.CR);
		sendMacro.addHandler(this);
		
		stopScript = new Macro(SWT.ESC);
		stopScript.addHandler(this);
	}
	
	public IMacro[] getMacros ()
	{
		return new IMacro[] { sendMacro, stopScript };
	}
	
	public boolean handleMacro (IMacro macro, IWarlockClientViewer viewer)
	{
		if (macro.equals(sendMacro))
		{
			handleSend(viewer);
			return true;
		}
		else if (macro.equals(stopScript))
		{
			handleStopScript(viewer);
			return true;
		}
		
		return false;
	}
	
	public void handleSend (IWarlockClientViewer viewer)
	{
		viewer.submit();
	}

	private void handleStopScript(IWarlockClientViewer viewer)
	{
		List<IScript> runningScripts = ScriptEngineRegistry.getRunningScripts();
		if (runningScripts.size() > 0)
		{
			IScript currentScript = runningScripts.get(runningScripts.size() - 1);
			
			if (currentScript != null)
			{
				currentScript.stop();
			}
		}
	}
	
}