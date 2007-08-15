package cc.warlock.configuration.server;

import org.dom4j.Element;

public class HighlightString extends Preset {

	public static final String KEY_TEXT = "text";
	
	public static final String STORMFRONT_STRINGS_MARKUP_PREFIX = "<strings><<m>";
	public static final String STORMFRONT_STRINGS_MARKUP_SUFFIX = "</<m></strings>";
	public static final String STORMFRONT_NAMES_MARKUP_PREFIX = "<names><<m>";
	public static final String STORMFRONT_NAMES_MARKUP_SUFFIX = "</<m></names>";
	
	protected String text;
	protected boolean name;
	protected HighlightString originalString;
	
	protected HighlightString (ServerSettings serverSettings)
	{
		super(serverSettings);
	}
	
	public HighlightString (HighlightString other)
	{
		super(other);
		
		this.text = other.text == null ? null : new String(other.text);
		this.name = other.name;
		this.originalString = other;
	}
	
	public HighlightString (ServerSettings serverSettings, Element highlightElement, Palette palette)
	{
		super(serverSettings, highlightElement, palette);
		
		this.text = highlightElement.attributeValue("text");
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		if (!text.equals(this.text))
			needsUpdate = true;
		
		this.text = text;
	}
	
	public boolean isName() {
		return name;
	}

	public void setIsName(boolean name) {
		this.name = name;
	}

	protected void saveToDOM ()
	{
		setAttribute(KEY_FGCOLOR, foregroundColor);
		setAttribute(KEY_BGCOLOR, backgroundColor == null ? "" : backgroundColor);
		setAttribute(KEY_TEXT, getText());
		
		if (fillEntireLine)
			setAttribute(KEY_FILL_ENTIRE_LINE, "y");
	}
	
	protected String toStormfrontMarkup ()
	{
		return "<h text=\"" + getText() + "\" " +
			KEY_FGCOLOR + "=\"" + foregroundColor + "\" " +
			KEY_BGCOLOR + "=\"" + (backgroundColor == null ? "" : backgroundColor) + "\"" +
			(fillEntireLine ? (" " + KEY_FILL_ENTIRE_LINE + "=\"y\"/>") : "/>");
	}
	
	public static HighlightString createHighlightStringFromParent (ServerSettings serverSettings, Element parent)
	{
		Element element = parent.addElement("h");
		
		HighlightString string = new HighlightString(serverSettings, element, serverSettings.getPalette());
		return string;
	}
	
	@Override
	public int compareTo(Preset o) {
		if (o instanceof HighlightString)
		{
			HighlightString other = (HighlightString) o;
			return text.compareTo(other.text);
		}
		else return -1;
	}
	
	@Override
	public void setNeedsUpdate(boolean needsUpdate) {
		super.setNeedsUpdate(needsUpdate);
		if (!needsUpdate)
			this.originalString = null;
	}
	
	public HighlightString getOriginalHighlightString ()
	{
		return originalString;
	}
}