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
package cc.warlock.rcp.ui.style;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;

import cc.warlock.core.client.IWarlockStyle;
import cc.warlock.rcp.ui.IStyleProvider;
import cc.warlock.rcp.ui.StyleRangeWithData;

public class DefaultStyleProvider implements IStyleProvider {
	
	protected static DefaultStyleProvider _instance;
	protected DefaultStyleProvider () { }
	
	public static DefaultStyleProvider instance()
	{
		if (_instance == null) {
			_instance = new DefaultStyleProvider();
		}
		return _instance;
	}
	
	public StyleRangeWithData getStyleRange (IWarlockStyle style)
	{	
		StyleRangeWithData range = new StyleRangeWithData();
		range.fontStyle = 0;
		
		for (IWarlockStyle.StyleType styleType : style.getStyleTypes())
		{
			if (styleType.equals(IWarlockStyle.StyleType.BOLD))
				range.fontStyle |= SWT.BOLD;
			else if (styleType.equals(IWarlockStyle.StyleType.ITALIC))
				range.fontStyle |= SWT.ITALIC;
			else if (styleType.equals(IWarlockStyle.StyleType.UNDERLINE) || styleType.equals(IWarlockStyle.StyleType.LINK))
				range.underline = true;
			else if (styleType.equals(IWarlockStyle.StyleType.MONOSPACE))
			{
				range.font = JFaceResources.getTextFont();
			}
		}
		
		return range;
	}
}
