package com.arcaner.warlock.configuration.skin;

import java.util.Hashtable;

import com.arcaner.warlock.client.stormfront.WarlockColor;

/**
 * The default skin handles any attributes who's values are "skin"
 * @author marshall
 */
public class DefaultSkin implements IWarlockSkin {

	public static final int DEFAULT_FONT_SIZE = 12;
	
	protected static Hashtable<String, WarlockColor> fgColors = new Hashtable<String, WarlockColor>();
	protected static Hashtable<String, WarlockColor> bgColors = new Hashtable<String, WarlockColor>();
	
	protected static WarlockColor skinColor (String color)
	{
		WarlockColor c = new WarlockColor(color);
		c.setSkinColor(true);
		return c;
	}
	
	static {
		fgColors.put("bold", skinColor("#FFFF00"));
		fgColors.put("roomName", skinColor("#FFFFFF"));
		fgColors.put("speech", skinColor("#80FF80"));
		fgColors.put("thought", skinColor("#FF8000"));
		fgColors.put("cmdline", skinColor("#FFFFFF"));
		fgColors.put("whisper", skinColor("#80FFFF"));
		fgColors.put("watching", skinColor("#FFFF00"));
		fgColors.put("link", skinColor("#62B0FF"));
		fgColors.put("selectedLink", skinColor("#000000"));
		fgColors.put("command", skinColor("#FFFFFF"));
		fgColors.put("main", skinColor("#F0F0FF"));
		
		bgColors.put("roomName", skinColor("#0000FF"));
		WarlockColor mainBG = skinColor("#191932");
		bgColors.put("bold", mainBG);
		bgColors.put("speech", mainBG);
		bgColors.put("whisper", mainBG);
		bgColors.put("thought", mainBG);
		bgColors.put("watching", mainBG);
		bgColors.put("link", mainBG);
		bgColors.put("main", mainBG);
		bgColors.put("cmdline", skinColor("#000000"));
		bgColors.put("selectedLink", skinColor("#62B0FF"));
		bgColors.put("command", skinColor("#404040"));
	}
	
	public WarlockColor getColor(ColorType type) {
		if (type == ColorType.MainWindow_Background)
			return getSkinBackgroundColor("main");
		else if (type == ColorType.MainWindow_Foreground)
			return getSkinForegroundColor("main");
		else if (type == ColorType.CommandLine_Background)
			return getSkinBackgroundColor("cmdline");
		else if (type == ColorType.CommandLine_Foreground)
			return getSkinForegroundColor("cmdline");
		
		return WarlockColor.DEFAULT_COLOR;
	}

	public String getFontFace(FontFaceType type) {
		if (System.getProperties().getProperty("os.name").indexOf("Windows") != -1)
		{
			return "Verdana";
		}
		return "Sans";
	}

	public int getFontSize(FontSizeType type) {
		return DEFAULT_FONT_SIZE; 
	}
	
	// These are hard coded for now, we should either have our own "skin" defined in a configuration somewhere,
	// or try to pull from stormfront's binary "skn" file somehow?
	// At any rate -- these look to be the right "default" settings for stormfront..
	public WarlockColor getSkinForegroundColor (String presetId)
	{
		WarlockColor color = WarlockColor.DEFAULT_COLOR;
		
		if (fgColors.containsKey(presetId))
		{
			color = fgColors.get(presetId);
		}
		
		return color;
	}
	
	public WarlockColor getSkinBackgroundColor (String presetId)
	{
		WarlockColor color = WarlockColor.DEFAULT_COLOR;
		
		if (bgColors.containsKey(presetId))
		{
			color = bgColors.get(presetId);
		}
		
		return color;
	}

}