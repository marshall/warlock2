/**
 * Warlock, the open-source cross-platform game client
 *  
 * Copyright 2008, Warlock LLC, and individual contributors as indicated
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
/*
 * Created on Mar 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cc.warlock.rcp.ui.client;

import java.io.InputStream;
import java.util.Collection;

import org.eclipse.swt.widgets.Display;

import cc.warlock.core.client.ICommand;
import cc.warlock.core.client.IMacroCommand;
import cc.warlock.core.client.IMacroVariable;
import cc.warlock.core.client.IWarlockClient;
import cc.warlock.core.client.IWarlockClientViewer;
import cc.warlock.core.client.WarlockString;
import cc.warlock.core.client.internal.WarlockMacro;

/**
 * @author Marshall
 *
 * A convenience super class for viewers who need SWT thread access
 */
public class SWTWarlockClientViewer implements IWarlockClientViewer  {

	private IWarlockClientViewer viewer;
	
	public SWTWarlockClientViewer (IWarlockClientViewer viewer)
	{
		this.viewer = viewer;
	}
	
	private class SetCommandWrapper implements Runnable {
		public String command;
		
		public SetCommandWrapper(String command) {
			this.command = command;
		}
		
		public void run () {
			viewer.setCurrentCommand(command);
		}
	}
	
	
	private class NextCommandWrapper implements Runnable {
		public void run () {
			viewer.nextCommand();
		}
	}
	
	private class PrevCommandWrapper implements Runnable {
		public void run () {
			viewer.prevCommand();
		}
	}
	
	private class SearchHistoryWrapper implements Runnable {
		public void run () {
			viewer.searchHistory();
		}
	}
	
	private class RepeatLastCommandWrapper implements Runnable {
		public void run () {
			viewer.repeatLastCommand();
		}
	}
	
	private class RepeatSecondToLastCommandWrapper implements Runnable {
		public void run () {
			viewer.repeatSecondToLastCommand();
		}
	}
	
	private class SubmitWrapper implements Runnable {
		public void run () {
			viewer.submit();
		}
	}
	
	private class AppendWrapper implements Runnable {
		public char c;
		
		public AppendWrapper(char ch) {
			this.c = ch;
		}
		
		public void run () {
			viewer.append(c);
		}
	}
	
	private class CopyWrapper implements Runnable {
		public void run () {
			viewer.copy();
		}
	}
	
	private class PlaySoundWrapper implements Runnable {
		public InputStream soundStream;
		
		public PlaySoundWrapper(InputStream soundStream) {
			this.soundStream = soundStream;
		}
		
		public void run () {
			viewer.playSound(soundStream);
		}
	}
	
	private class PasteWrapper implements Runnable {
		public void run () {
			viewer.paste();
		}
	}
	
	private class OpenCustomStreamWrapper implements Runnable {
		public String name;
		
		public OpenCustomStreamWrapper(String name) {
			this.name = name;
		}
		
		public void run () {
			viewer.openCustomStream(name);
		}
	}
	
	private class PrintToCustomStreamWrapper implements Runnable {
		private String name;
		private WarlockString text;
		
		public PrintToCustomStreamWrapper(String name, WarlockString text) {
			this.name = name;
			this.text = text;
		}
		
		public void run () {
			viewer.printToCustomStream(name, text);
		}
	}
	
	private class ClearCustomStreamWrapper implements Runnable {
		public String name;
		
		public ClearCustomStreamWrapper(String name) {
			this.name = name;
		}
		
		public void run () {
			viewer.clearCustomStream(name);
		}
	}
	
	protected void run(Runnable runnable)
	{
		Display.getDefault().asyncExec(new CatchingRunnable(runnable));
	}
	
	public String getCurrentCommand() {
		return viewer.getCurrentCommand();
	}
	
	public IWarlockClient getClient() {
		return viewer.getClient();
	}
	
	public void setCurrentCommand(String command) {
		run(new SetCommandWrapper(command));
	}
	
	public void append(char ch) {
		run(new AppendWrapper(ch));
	}
	
	public void nextCommand() {
		run(new NextCommandWrapper());
	}
	
	public void prevCommand() {
		run(new PrevCommandWrapper());
	}
	
	public void searchHistory() {
		run(new SearchHistoryWrapper());
	}
	
	public void repeatLastCommand() {
		run(new RepeatLastCommandWrapper());
	}
	
	public void repeatSecondToLastCommand() {
		run(new RepeatSecondToLastCommandWrapper());
	}
	
	public void submit() {
		run(new SubmitWrapper());
	}
	
	public void copy() {
		run(new CopyWrapper());
	}
	
	public void paste() {
		run(new PasteWrapper());
	}
	
	public void playSound(InputStream file) {
		run(new PlaySoundWrapper(file));
	}
	
	public boolean isStreamOpen(String streamName) {
		// This method is not allowed to use any SWT methods
		return viewer.isStreamOpen(streamName);
	}

	public Collection<IMacroVariable> getMacroVariables() {
		return viewer.getMacroVariables();
	}

	public IMacroCommand getMacroCommand(String id) {
		return viewer.getMacroCommand(id);
	}

	public Collection<WarlockMacro> getDefaultMacros() {
		return viewer.getDefaultMacros();
	}
	
	public void openCustomStream(String name) {
		run(new OpenCustomStreamWrapper(name));
	}
	
	public void printToCustomStream(String name, WarlockString text) {
		run(new PrintToCustomStreamWrapper(name, text));
	}
	
	public void clearCustomStream(String name) {
		run(new ClearCustomStreamWrapper(name));
	}
	
	public void send(ICommand command) {
		viewer.send(command);
	}
}
