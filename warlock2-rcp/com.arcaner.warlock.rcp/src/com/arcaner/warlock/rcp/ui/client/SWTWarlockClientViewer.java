/*
 * Created on Mar 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.arcaner.warlock.rcp.ui.client;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;

import com.arcaner.warlock.client.IWarlockClient;
import com.arcaner.warlock.client.IWarlockClientViewer;
import com.arcaner.warlock.rcp.views.CompassView;
import com.arcaner.warlock.rcp.views.GameView;

/**
 * @author Marshall
 *
 * A convenience super class for viewers who need SWT thread access
 */
public class SWTWarlockClientViewer implements IWarlockClientViewer {

	private IWarlockClientViewer viewer;
	private ListenerWrapper wrapper;
	private boolean asynch;
	
	private static enum EventType
	{
		Append, Append2, Echo, SetViewerTitle
	};
	
	public SWTWarlockClientViewer (IWarlockClientViewer viewer)
	{
		this(viewer, false);
	}
	
	public SWTWarlockClientViewer (IWarlockClientViewer viewer, boolean asynch)
	{
		this.viewer = viewer;
		this.wrapper = new ListenerWrapper();
		this.asynch = asynch;
	}
	
	protected class ListenerWrapper implements Runnable {
		public String text, viewName;
		public IWarlockClient client;
		public EventType eventType;
		
		public void run () {
			switch (eventType)
			{
				case Append: viewer.append(text); break;
				case Append2: viewer.append(viewName, text); break;
				case Echo: viewer.echo(text); break;
				case SetViewerTitle: viewer.setViewerTitle(text); break;
			}
			viewName = null;
			text = null;
			client = null;
		}
	}
	
	private void run(Runnable runnable)
	{
		if (asynch)
		{
			Display.getDefault().asyncExec(runnable);
		} else {
			Display.getDefault().syncExec(runnable);
		}
	}
	
	public void append(String text) {
		wrapper.text = text;
		wrapper.eventType = EventType.Append;
		run(wrapper);
	}
	
	public void echo(String text) {
		wrapper.text = text;
		wrapper.eventType = EventType.Echo;
		run(wrapper);
	}
	
	public void setViewerTitle(String title) {
		wrapper.text = title;
		wrapper.eventType = EventType.SetViewerTitle;
		run(wrapper);
	}
	
	public String getCurrentCommand() {
		return viewer.getCurrentCommand();
	}
	
	public IWarlockClient getWarlockClient() {
		return viewer.getWarlockClient();
	}
	
	public void append(String viewName, String text) {
		wrapper.viewName = viewName;
		wrapper.text = text;
		wrapper.eventType = EventType.Append2;
		run(wrapper);
	}
}
