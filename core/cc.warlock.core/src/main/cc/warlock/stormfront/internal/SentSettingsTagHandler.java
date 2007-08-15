package cc.warlock.stormfront.internal;

import cc.warlock.stormfront.IStormFrontProtocolHandler;


public class SentSettingsTagHandler extends DefaultTagHandler {

	public SentSettingsTagHandler(IStormFrontProtocolHandler handler) {
		super(handler);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String[] getTagNames() {
		return new String[] { "sentSettings" };
	}

	@Override
	public void handleEnd() {
		handler.getClient().send("");
	}
}