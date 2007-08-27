/**
 * 
 */
package cc.warlock.script;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Match
{
	private String matchText;
	private Pattern regex;
	private boolean ignoreCase = true;
	private HashMap<String,String> data = new HashMap<String,String>();
	
	public void setMatchText(String text) {
		setMatchText(text, true);
	}
	
	public void setMatchText(String text, boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		
		if(ignoreCase) {
			matchText = text.toLowerCase();
		} else {
			matchText = text;
		}
	}
	
	public void setRegex(String text) {
		setRegex(text, false);
	}
	
	public void setRegex(String text, boolean ignoreCase) {
		int flags = 0;
		if(ignoreCase) flags |= Pattern.CASE_INSENSITIVE;
		
		regex = Pattern.compile(text, flags);
		this.ignoreCase = ignoreCase;
	}
	
	public String setAttribute(String key, String value) {
		return data.put(key, value);
	}
	
	public String getAttribute(String key) {
		return data.get(key);
	}
	
	public boolean matches(String text) {
		boolean rv = false;
		if(matchText != null) {
			if(ignoreCase) {
				if(text.toLowerCase().contains(matchText)) {
					rv = true;
				}
			} else {
				if(text.matches(matchText)) {
					rv = true;
				}
			}
			if(rv) {
				System.out.println("matched text: \"" + text + "\" with \"" + matchText + "\"");
			}
		}
		
		if(regex != null) {
			Matcher m = regex.matcher(text);
			if(m.matches()) {
				rv = true;
			}
			// TODO should set some variables for the groups here
		}
		
		return rv;
	}
}