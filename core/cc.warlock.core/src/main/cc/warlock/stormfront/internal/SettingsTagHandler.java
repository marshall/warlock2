package cc.warlock.stormfront.internal;

import java.io.File;
import java.io.FileOutputStream;

import org.xml.sax.Attributes;

import cc.warlock.client.IWarlockClientViewer;
import cc.warlock.client.stormfront.IStormFrontClientViewer;
import cc.warlock.configuration.WarlockConfiguration;
import cc.warlock.stormfront.IStormFrontProtocolHandler;


public class SettingsTagHandler extends DefaultTagHandler {

	private StringBuffer buffer = new StringBuffer();
	private boolean handlingSettings = false;
	
	public SettingsTagHandler(IStormFrontProtocolHandler handler) {
		super(handler);
		// TODO Auto-generated constructor stub
	}

	private static interface ViewerVisitor {
		public void visit (IStormFrontClientViewer viewer);
	}
	
	private void visitViewers (ViewerVisitor visitor)
	{
		for (IWarlockClientViewer viewer : handler.getClient().getViewers())
		{
			if (viewer instanceof IStormFrontClientViewer)
			{
				IStormFrontClientViewer sfViewer = (IStormFrontClientViewer) viewer;
				visitor.visit(sfViewer);
			}
		}
	}
	
	private static final String[] onlySettingsTag = new String[] { "settings" };
	private static final String[] settingsTags = new String[] {
		"settings", "presets", "strings", "stream", "scripts",
		"names", "macros", "palette", "vars", "dialog",
		"panels", "toggles", "misc", "options", "cmdline",
		"keys", "p", "k", "i", "w", "h", "k", "v", "s", "m", "o",
		"font", "columnFont", "detach", "ignores", "builtin",
		"group", "toggles", "app", "display"};
	
	@Override
	public String[] getTagNames() {
		return handlingSettings ? settingsTags : onlySettingsTag;
	}
	
	private void reregister ()
	{
		//forced re-registration to change which tags we handle
		handler.removeHandler(this);
		handler.registerHandler(this, 0);
	}
	
	@Override
	public void handleStart(Attributes atts) {
		if ("settings".equals(getCurrentTag()))
		{
			handlingSettings = true;
			reregister();
			
			buffer.setLength(0);	
			handler.startSavingRawXML(buffer, "settings");
			visitViewers(new ViewerVisitor() {
				public void visit(IStormFrontClientViewer viewer) {
					viewer.startDownloadingServerSettings();
				}
			});
		}
	}

	@Override
	public void handleEnd() {
		if ("settings".equals(getCurrentTag()))
		{
			handlingSettings = false;
			reregister();
			
			handler.stopSavingRawXML();
			buffer.insert(0, "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<settings>\n");
			buffer.append("</settings>");
			
			String playerId = handler.getClient().getPlayerId().get();
			File serverSettings = WarlockConfiguration.getConfigurationFile("serverSettings_" + playerId + ".xml");
			
			try {
	
				FileOutputStream stream = new FileOutputStream(serverSettings);
				stream.write(buffer.toString().getBytes());
				stream.close();
				buffer = null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			handler.getClient().getServerSettings().load(handler.getClient().getPlayerId().get());
			visitViewers(new ViewerVisitor() {
				public void visit(IStormFrontClientViewer viewer) {
					viewer.finishedDownloadingServerSettings();
				}
			});
		}
		else 
		{
			IStormFrontClientViewer.SettingType setting = null;
			if ("presets".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Presets;
			else if ("strings".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Strings;
			else if ("stream".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Streams;
			else if ("scripts".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Scripts;
			else if ("names".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Names;
			else if ("macros".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Macros;
			else if ("palette".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Palette;
			else if ("vars".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Vars;
			else if ("dialog".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Dialogs;
			else if ("panels".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Panels;
			else if ("toggles".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Toggles;
			else if ("misc".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Misc;
			else if ("options".equals(getCurrentTag())) setting = IStormFrontClientViewer.SettingType.Options;
			
			if (setting !=  null)
			{
				final IStormFrontClientViewer.SettingType finalSetting = setting;
				visitViewers(new ViewerVisitor() {
					public void visit(IStormFrontClientViewer viewer) {
						viewer.receivedServerSetting(finalSetting);
					}
				});
			}
		}
	}
	
	@Override
	public boolean handleCharacters(char[] ch, int start, int length) {
		return true;
	}
}