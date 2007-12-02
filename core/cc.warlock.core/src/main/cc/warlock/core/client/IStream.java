/*
 * Created on Jan 15, 2005
 */
package cc.warlock.core.client;


/**
 * @author Marshall
 *
 * A stream is StormFront's idea of a text buffer for a window. This is the Interface representing that buffer.
 */
public interface IStream {
	
	public void clear();
	
	public IProperty<String> getName();
	
	public IProperty<String> getTitle();
	
	public void send (String text);
	public void send (WarlockString text);
	
	public void prompt(String prompt);
	public void sendCommand(String text);
	public boolean isPrompting();
	public void flush();
	public boolean hasView();
	public void setView(boolean view);
	
	public void echo(String text);
	
	public void addStreamListener(IStreamListener listener);
	public void removeStreamListener(IStreamListener listener);
	
	public IWarlockClient getClient();
}
