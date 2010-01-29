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

package cc.warlock.core.stormfront.client.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import cc.warlock.core.client.ICharacterStatus;
import cc.warlock.core.client.ICommand;
import cc.warlock.core.client.IProperty;
import cc.warlock.core.client.IRoomListener;
import cc.warlock.core.client.IStream;
import cc.warlock.core.client.IWarlockClient;
import cc.warlock.core.client.IWarlockClientViewer;
import cc.warlock.core.client.IWarlockSkin;
import cc.warlock.core.client.IWarlockStyle;
import cc.warlock.core.client.WarlockClientRegistry;
import cc.warlock.core.client.WarlockString;
import cc.warlock.core.client.internal.CharacterStatus;
import cc.warlock.core.client.internal.Property;
import cc.warlock.core.client.internal.WarlockClient;
import cc.warlock.core.client.internal.WarlockStyle;
import cc.warlock.core.client.settings.IClientSettings;
import cc.warlock.core.configuration.ConfigurationUtil;
import cc.warlock.core.script.IScript;
import cc.warlock.core.script.IScriptListener;
import cc.warlock.core.script.ScriptEngineRegistry;
import cc.warlock.core.script.configuration.ScriptConfiguration;
import cc.warlock.core.stormfront.client.IStormFrontClient;
import cc.warlock.core.stormfront.client.IStormFrontClientViewer;
import cc.warlock.core.stormfront.client.IStormFrontDialogMessage;
import cc.warlock.core.stormfront.network.StormFrontConnection;
import cc.warlock.core.stormfront.settings.IStormFrontClientSettings;
import cc.warlock.core.stormfront.settings.StormFrontServerSettings;
import cc.warlock.core.stormfront.settings.internal.StormFrontClientSettings;
import cc.warlock.core.stormfront.settings.skin.DefaultSkin;
import cc.warlock.core.stormfront.settings.skin.IStormFrontSkin;
import cc.warlock.core.stormfront.xml.StormFrontDocument;
import cc.warlock.core.stormfront.xml.StormFrontElement;

import com.martiansoftware.jsap.CommandLineTokenizer;

/**
 * @author Marshall
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StormFrontClient extends WarlockClient implements IStormFrontClient, IScriptListener, IRoomListener {

	protected ICharacterStatus status;
	protected Property<Integer> roundtime, casttime, monsterCount;
	protected Property<String> leftHand, rightHand, currentSpell;
	protected StringBuffer buffer = new StringBuffer();
	protected Property<String> playerId, characterName, gameCode, roomDescription;
	protected StormFrontClientSettings clientSettings;
	protected StormFrontServerSettings serverSettings;
	protected long timeDelta;
	protected Long roundtimeEnd, casttimeEnd;
	protected int roundtimeLength, casttimeLength;
	protected Thread roundtimeThread = null;
	protected Thread casttimeThread = null;
	protected ArrayList<IScript> runningScripts;
	protected ArrayList<IScriptListener> scriptListeners;
	protected DefaultSkin skin;
	protected HashMap<String, Property<String>> components =
		new HashMap<String, Property<String>>();
	protected HashMap<String, IStream> componentStreams = new HashMap<String, IStream>();
	protected HashMap<String, String> commands;
	protected HashMap<String, Property<IStormFrontDialogMessage>> dialogs =
		new HashMap<String, Property<IStormFrontDialogMessage>>();
	protected HashMap<String, String> vitals = new HashMap<String, String>();
	
	public StormFrontClient() {
		super();
		
		status = new CharacterStatus(this);
		leftHand = new Property<String>("leftHand", null);
		rightHand = new Property<String>("rightHand", null);
		currentSpell = new Property<String>("currentSpell", null);
		
		roundtime = new Property<Integer>("roundtime", 0);
		casttime = new Property<Integer>("casttime", 0);
		playerId = new Property<String>("playerId", null);
		characterName = new Property<String>("characterName", null);
		gameCode = new Property<String>("gameCode", null);
		roomDescription = new Property<String>("roomDescription", null);
		monsterCount = new Property<Integer>("monsterCount", null);

		roundtimeEnd = null;
		casttimeEnd = null;
		roundtimeLength = 0;
		casttimeLength = 0;
		runningScripts = new ArrayList<IScript>();
		scriptListeners = new ArrayList<IScriptListener>();
		
		WarlockClientRegistry.activateClient(this);
	}
	
	@Override
	protected IClientSettings createClientSettings() {
		clientSettings = new StormFrontClientSettings(this);

		skin = new DefaultSkin(clientSettings);
		skin.loadDefaultStyles(clientSettings.getHighlightConfigurationProvider());
		
		serverSettings = new StormFrontServerSettings();
		clientSettings.addChildProvider(serverSettings);
		
		return clientSettings;
	}
	
	@Override
	public void send(ICommand command) {
		String scriptPrefix = ScriptConfiguration.instance().getScriptPrefix();
		
		if (command.getCommand().startsWith(scriptPrefix)){
			runScript(command.getCommand().substring(scriptPrefix.length()));
		} else {
			super.send(command);
		}
	}
	
	public void runScript(String command) {
		command = command.replaceAll("[\\r\\n]", "");
		
		int firstSpace = command.indexOf(" ");
		String scriptName = command.substring(0, (firstSpace < 0 ? command.length() : firstSpace));
		String[] arguments = new String[0];
		
		if (firstSpace > 0)
		{
			String args = command.substring(firstSpace+1);
			arguments = CommandLineTokenizer.tokenize(args);
		}
		
		IScript script = ScriptEngineRegistry.startScript(scriptName, this, arguments);
		if (script != null)
		{
			script.addScriptListener(this);
			for (IScriptListener listener : scriptListeners) listener.scriptStarted(script);
			runningScripts.add(script);
		}
	}
	
	public void scriptPaused(IScript script) {
		for (IScriptListener listener : scriptListeners) listener.scriptPaused(script);
	}
	
	public void scriptResumed(IScript script) {
		for (IScriptListener listener : scriptListeners) listener.scriptResumed(script);
	}
	
	public void scriptStarted(IScript script) {
		for (IScriptListener listener : scriptListeners) listener.scriptStarted(script);
	}
	
	public void scriptStopped(IScript script, boolean userStopped) {
		runningScripts.remove(script);
		
		for (IScriptListener listener : scriptListeners) listener.scriptStopped(script, userStopped);
	}
	
	public IProperty<Integer> getRoundtime() {
		return roundtime;
	}
	
	public IProperty<Integer> getCasttime() {
		return casttime;
	}
	
	public IProperty<Integer> getMonsterCount() {
		return monsterCount;
	}
	
	public Property<IStormFrontDialogMessage> getDialog(String id) {
		Property<IStormFrontDialogMessage> dialog = dialogs.get(id);
		
		if(dialog == null) {
			dialog = new Property<IStormFrontDialogMessage>(id, null);
			dialogs.put(id, dialog);
		}
		
		return dialog;
	}
	
	private class RoundtimeThread extends Thread
	{		
		public void run()
		{
			for (;;) {
				long now = System.currentTimeMillis();
				long rt = 0;
				
				// Synchronize with external roundtime updates
				synchronized(StormFrontClient.this) {
					if (roundtimeEnd != null)
						rt = roundtimeEnd * 1000L + timeDelta - now;
					
					if (rt <= 0) {
						roundtimeThread = null;
						roundtimeEnd = null;
						roundtimeLength = 0;
						roundtime.set(0);
						StormFrontClient.this.notifyAll();
						return;
					}
				}
				
				// Update the world with the new roundtime
				// Avoid flicker caused by redundant updates
				int rtSeconds = (int)((rt + 999) / 1000);
				if (roundtime.get() != rtSeconds)
					roundtime.set(rtSeconds);
				
				// Compute how long until next roundtime update
				long waitTime = rt % 1000;
				if (waitTime == 0) waitTime = 1000;
				
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					// This is not supposed to happen.
					e.printStackTrace();
				}
			}
		}
	}
	
	private class CasttimeThread extends Thread
	{		
		public void run()
		{
			for (;;) {
				long now = System.currentTimeMillis();
				long ct = 0;
				
				// Synchronize with external casttime updates
				synchronized(StormFrontClient.this) {
					if (casttimeEnd != null)
						ct = casttimeEnd * 1000L + timeDelta - now;
					
					if (ct <= 0) {
						casttimeThread = null;
						casttimeEnd = null;
						casttimeLength = 0;
						casttime.set(0);
						StormFrontClient.this.notifyAll();
						return;
					}
				}
				
				// Update the world with the new casttime
				// Avoid flicker caused by redundant updates
				int ctSeconds = (int)((ct + 999) / 1000);
				if (casttime.get() != ctSeconds)
					casttime.set(ctSeconds);
				
				// Compute how long until next casttime update
				long waitTime = ct % 1000;
				if (waitTime == 0) waitTime = 1000;
				
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					// This is not supposed to happen.
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void setupRoundtime(Long end)
	{
		roundtimeEnd = end;
	}
	
	public synchronized void setupCasttime(Long end)
	{
		casttimeEnd = end;
	}
	
	public synchronized void syncTime(Long now)
	{
		if (roundtimeEnd == null && casttimeEnd == null) return;
		
		long newTimeDelta = System.currentTimeMillis() - now * 1000L;
		if (roundtimeThread != null || casttimeThread != null) {
			// Don't decrease timeDelta while roundtimes are active.
			if (newTimeDelta > timeDelta) timeDelta = newTimeDelta;
			return;
		}
		timeDelta = newTimeDelta;
		
		/* let's us know if we need to notify clients */
		boolean notify = false;
		
		if (roundtimeEnd != null) {
			if (roundtimeEnd > now) {
				// We need to do this now due to scheduling delays in the thread
				roundtimeLength = (int)(roundtimeEnd - now);
				roundtime.set(roundtimeLength);
				roundtimeThread = new RoundtimeThread();
				roundtimeThread.start();
			} else {
				roundtime.set(0);
				roundtimeEnd = null;
				roundtimeLength = 0;
				notify = true;
			}
		}
		
		if (casttimeEnd != null) {
			if (casttimeEnd > now) {
				// We need to do this now due to scheduling delays in the thread
				casttimeLength = (int)(casttimeEnd - now);
				casttime.set(casttimeLength);
				casttimeThread = new CasttimeThread();
				casttimeThread.start();
			} else {
				casttime.set(0);
				casttimeEnd = null;
				casttimeLength = 0;
				notify = true;
			}
		}
		
		if (notify) {
			StormFrontClient.this.notifyAll();
		}
	}
	
	public int getRoundtimeLength() {
		return roundtimeLength;
	}
	
	public int getCasttimeLength() {
		return casttimeLength;
	}
	
	public synchronized void waitForRoundtime(double delay) throws InterruptedException {
		while (roundtimeEnd != null) {
			wait();
			Thread.sleep((long)(delay * 1000));
		}
	}
	
	public synchronized void waitForCasttime(double delay) throws InterruptedException {
		while (casttimeEnd != null) {
			wait();
			Thread.sleep((long)(delay * 1000));
		}
	}
	
	public String getVital(String id) {
		return vitals.get(id);
	}
	
	public String setVital(String id, String value) {
		return vitals.put(id, value);
	}

	public void connect(String server, int port, String key) throws IOException {
		connection = new StormFrontConnection(this, key);
		connection.connect(server, port);
		
		WarlockClientRegistry.clientConnected(this);
	}
	
	public void streamCleared() {
		// TODO Auto-generated method stub
		
	}

	public Property<String> getPlayerId() {
		return playerId;
	}
	
	public IStormFrontClientSettings getStormFrontClientSettings() {
		return clientSettings;
	}
	
	public IProperty<String> getCharacterName() {
		return characterName;
	}
	
	public IProperty<String> getGameCode() {
		return gameCode;
	}
	
	public IProperty<String> getClientId() {
		return playerId;
	}
	
	public IProperty<String> getLeftHand() {
		return leftHand;
	}
	
	public IProperty<String> getRightHand() {
		return rightHand;
	}
	
	public IProperty<String> getCurrentSpell() {
		return currentSpell;
	}
	
	public ICharacterStatus getCharacterStatus() {
		return status;
	}
	
	public Collection<IScript> getRunningScripts() {
		return runningScripts;
	}
	
	public void addScriptListener(IScriptListener listener)
	{
		scriptListeners.add(listener);
	}
	
	public void removeScriptListener (IScriptListener listener)
	{
		scriptListeners.remove(listener);
	}
	
	public IWarlockSkin getSkin() {
		return skin;
	}
	
	public IStormFrontSkin getStormFrontSkin() {
		return skin;
	}
	
	@Override
	public IStream getStream(String streamName) {
		synchronized(streams) {
			IStream stream = super.getStream(streamName);
			if(stream == null) {
				StormFrontStream sfStream = new StormFrontStream(this, streamName);
				if (streamName.contains(IWarlockClient.DEFAULT_STREAM_NAME)) {
					sfStream.setLogging(true);
				}
				stream = sfStream;
				streams.put(streamName, stream);
			}
			return stream;
		}
	}
	
	public IStream getThoughtsStream() {
		return getStream(THOUGHTS_STREAM_NAME);
	}
	
	public IStream getInventoryStream() {
		return getStream(INVENTORY_STREAM_NAME);
	}
	
	public IStream getDeathsStream() {
		return getStream(DEATH_STREAM_NAME);
	}
	
	public IStream getRoomStream() {
		return getStream(ROOM_STREAM_NAME);
	}
	
	@Override
	public IStream getDefaultStream() {
		return getStream(DEFAULT_STREAM_NAME);
	}

	public IStream getFamiliarStream() {
		return getStream(FAMILIAR_STREAM_NAME);
	}
	
	public IStream getAssessStream() {
		return getStream(ASSESS_STREAM_NAME);
	}
	
	public IStream getExperienceStream() {
		return getStream(EXPERIENCE_STREAM_NAME);
	}
	
	public IStream getLogonsStream() {
		return getStream(LOGONS_STREAM_NAME);
	}
	
	public void setComponent (String name, String value, IStream stream)
	{
		if (!components.containsKey(name)) {
			components.put(name, new Property<String>(name, value));
		} else {
			components.get(name).set(value);
		}
		componentStreams.put(name, stream);
		//stream.addComponent(name);
	}
	
	public void updateComponent(String name, WarlockString value) {
		Property<String> component = components.get(name);
		if(component != null) {
			component.set(value.toString());
		} else {
			component = new Property<String>(name, value.toString());
			components.put(name, component);
		}
		
		IStream stream = componentStreams.get(name);
		if(stream != null)
			stream.updateComponent(name, value);
		// FIXME: else should we create the stream? -Sean
	}
	
	public IProperty<String> getComponent(String componentName) {
		return components.get(componentName);
	}
	
	@Override
	protected void finalize() throws Throwable {
		WarlockClientRegistry.removeClient(this);
		super.finalize();
	}
	
	public IWarlockStyle getCommandStyle() {
		IWarlockStyle style = clientSettings.getNamedStyle(StormFrontClientSettings.PRESET_COMMAND);
		if (style == null) {
			return new WarlockStyle();
		}
		return style;
	}
	
	public void loadCmdlist()
	{
		try {
			commands  = new HashMap<String, String>();
			FileInputStream stream = new FileInputStream(ConfigurationUtil.getConfigurationFile("cmdlist1.xml"));
			StormFrontDocument document = new StormFrontDocument(stream);
			stream.close();
			
			StormFrontElement cmdlist = document.getRootElement();
			for (StormFrontElement cliElement : cmdlist.elements())
			{
				if(cliElement.getName().equals("cli")) {
					String coord = cliElement.attributeValue("coord");
					String command = cliElement.attributeValue("command");
					
					if(coord != null && command != null)
						commands.put(coord, command);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getCommand(String coord) {
		if(commands == null) return null;
		return commands.get(coord);
	}
	/* Internal only.. meant for importing/exporting stormfront's savings */
	public StormFrontServerSettings getServerSettings() {
		return serverSettings;
	}
	
	public void launchURL(String url) {
		for (IWarlockClientViewer viewer : viewers)
		{
			if (viewer instanceof IStormFrontClientViewer)
			{
				try {
					((IStormFrontClientViewer)viewer).launchURL(new URL(url));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void appendImage(String pictureId) {
		try {
			URL url = new URL("http://www.play.net/bfe/DR-art/" + pictureId + "_t.jpg");
			
			for (IWarlockClientViewer viewer : viewers) {
				if (viewer instanceof IStormFrontClientViewer)
					((IStormFrontClientViewer) viewer).appendImage(url);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadClientSettings(IClientSettings settings) {
		for (IWarlockClientViewer viewer : viewers)
		{
			viewer.loadClientSettings(settings);
		}
	}
	
	public void startedDownloadingServerSettings() {
		for (IWarlockClientViewer viewer : viewers) {
			if (viewer instanceof IStormFrontClientViewer)
				((IStormFrontClientViewer)viewer).startedDownloadingServerSettings();
		}
	}
	
	public void finishedDownloadingServerSettings(String str) {
		String playerId = getPlayerId().get();
		File settingsFile = ConfigurationUtil.getConfigurationFile("serverSettings_" + playerId + ".xml");
		InputStream inStream = new ByteArrayInputStream(str.getBytes());
		
		try {
			FileWriter writer = new FileWriter(settingsFile);

			StormFrontDocument document = new StormFrontDocument(inStream);
			document.saveTo(writer, true);
			
			writer.close();
			inStream.close();
			buffer.setLength(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			InputStream stream = new FileInputStream(settingsFile);
			
			serverSettings.importServerSettings(stream, clientSettings);
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(IWarlockClientViewer viewer : viewers) {
			if (viewer instanceof IStormFrontClientViewer)
				((IStormFrontClientViewer)viewer).finishedDownloadingServerSettings();
		}
	}
	
	public void receivedServerSetting(String setting) {
		for (IWarlockClientViewer viewer : viewers) {
			if (viewer instanceof IStormFrontClientViewer)
				((IStormFrontClientViewer)viewer).receivedServerSetting(setting);
		}
	}
}
