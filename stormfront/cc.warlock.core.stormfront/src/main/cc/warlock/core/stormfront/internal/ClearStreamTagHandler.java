package cc.warlock.core.stormfront.internal;

import cc.warlock.core.stormfront.IStormFrontProtocolHandler;
import cc.warlock.core.stormfront.xml.StormFrontAttributeList;


public class ClearStreamTagHandler extends DefaultTagHandler {
	
	/**
	 * @param handler
	 */
	public ClearStreamTagHandler(IStormFrontProtocolHandler handler) {
		super(handler);
	}

	@Override
	public String[] getTagNames() {
		return new String[] { "clearStream"};
	}
	
	@Override
	public void handleStart(StormFrontAttributeList attributes) {
		String id = attributes.getValue("id");
		
		handler.getClient().getStream(id).clear();
	}
}
