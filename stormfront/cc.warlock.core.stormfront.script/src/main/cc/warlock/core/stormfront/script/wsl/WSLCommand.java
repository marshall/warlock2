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
package cc.warlock.core.stormfront.script.wsl;

public class WSLCommand extends WSLAbstractCommand {

	private WSLScript script;
	private IWSLValue value;
	
	public WSLCommand(int lineNumber, WSLScript script, IWSLValue value) {
		super(lineNumber);
		this.script = script;
		this.value = value;
	}
	
	public void execute() throws InterruptedException {
		 // we can have empty commands at labels
		if(value == null)
			return;
		
		script.execute(value.toString());
	}
}
