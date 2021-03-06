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
package cc.warlock.core.stormfront.settings.server;

import cc.warlock.core.stormfront.xml.StormFrontElement;

public abstract class ServerSetting {

	public static final String UPDATE_PREFIX = "<<m>";
	public static final String UPDATE_SUFFIX = "</<m>";
	public static final String DELETE_PREFIX = "<<d>";
	public static final String DELETE_SUFFIX = "</<d>";
	public static final String ADD_PREFIX = "<<a>";
	public static final String ADD_SUFFIX = "</<a>";
	
	/// what the hell does R stand for?
	public static final String R_PREFIX = "<<r>";
	public static final String R_SUFFIX = "</<r>";
	
	protected StormFrontElement element;
	protected ServerSettings serverSettings;
	
	public ServerSetting (ServerSettings serverSettings)
	{
		this.serverSettings = serverSettings;
	}
	
	public ServerSetting (ServerSettings serverSettings, StormFrontElement element)
	{
		this.element = element;
		this.serverSettings = serverSettings;
	}
	
	protected abstract void saveToDOM ();
	
	protected abstract String toStormfrontMarkup();
	
	protected void setAttribute (String attrName, String value)
	{
		setAttribute(element, attrName, value);
	}
	
	protected void deleteFromDOM ()
	{
		element.getParent().removeElement(element);
	}
	
	protected StormFrontElement getElement ()
	{
		return element;
	}
	
	protected static void setAttribute (StormFrontElement element, String attrName, String value)
	{
		element.setAttribute(attrName, value);
	}

	// seems like stormfront uses 96 dpi resolution vs. standard 72 dpi
	protected static int getPixelSizeInPoints (int pixelSize)
	{
		double points = pixelSize * (72.0/96.0);
		return (int) Math.round(points);
	}
	
	protected static int getPointSizeInPixels (int pointSize)
	{
		double pixels = pointSize * (96.0/72.0);
		
		return (int) Math.round(pixels);
	}
	
	public String deleteMarkup()
	{
		return DELETE_PREFIX + toStormfrontMarkup() + DELETE_SUFFIX;
	}
	
	public static String updateMarkup(String childMarkup)
	{
		return UPDATE_PREFIX + childMarkup + UPDATE_SUFFIX;
	}
}
