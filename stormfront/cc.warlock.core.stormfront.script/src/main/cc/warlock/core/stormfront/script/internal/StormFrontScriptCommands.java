package cc.warlock.core.stormfront.script.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import cc.warlock.core.client.IProperty;
import cc.warlock.core.client.IPropertyListener;
import cc.warlock.core.script.IMatch;
import cc.warlock.core.script.IScript;
import cc.warlock.core.script.internal.RegexMatch;
import cc.warlock.core.script.internal.ScriptCommands;
import cc.warlock.core.stormfront.client.IStormFrontClient;
import cc.warlock.core.stormfront.script.IStormFrontScriptCommands;

public class StormFrontScriptCommands extends ScriptCommands implements IStormFrontScriptCommands, IPropertyListener<Integer> {

	protected IStormFrontClient sfClient;
	protected IScript script;
	
	public StormFrontScriptCommands (IStormFrontClient client, String name)
	{
		super(client, name);
		this.sfClient = client;
		waitingForRoundtime = false;
		
		client.getRoundtime().addListener(this);
		client.getDeathsStream().addStreamListener(this);
		client.getFamiliarStream().addStreamListener(this);
		client.addRoomListener(this);
	}
	
	public StormFrontScriptCommands (IStormFrontClient client, IScript script)
	{
		this(client, script.getName());
		
		this.script = script;
	}
	
	public IStormFrontClient getStormFrontClient() {
		return sfClient;
	}
	
	protected boolean waitingForRoundtime;
	public void waitForRoundtime ()
	{
		if(!interrupted) {
			assertPrompt();
			try {
				while(sfClient.getRoundtime().get() > 0 && !interrupted) {
					Thread.sleep((sfClient.getRoundtime().get() + 1) * 1000);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void waitNextRoom() {
		super.waitNextRoom();
		waitForRoundtime();
	}
	
	@Override
	public void pause(double seconds) {
		super.pause(seconds);
		waitForRoundtime();
	}
	
	@Override
	public void waitFor(IMatch match) {
		try {
			super.waitFor(match);
		} finally {
			waitForRoundtime();
		}
	}
	
	@Override
	public void waitForPrompt() {
		try {
			super.waitForPrompt();
		} finally {
			waitForRoundtime();
		}
	}
	
	
	public void propertyActivated(IProperty<Integer> property) {}
	public void propertyChanged(IProperty<Integer> property, Integer oldValue) {
		if (property.getName().equals("roundtime"))
		{
			if (property.get() == 0) waitingForRoundtime = false;
		}
	}
	
	public void propertyCleared(IProperty<Integer> property, Integer oldValue) {}
	
	private Map<IMatch, Runnable> actions = Collections.synchronizedMap(new HashMap<IMatch, Runnable>());
	
	protected  class ScriptActionThread implements Runnable {
		public void run() {
			LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
			textWaiters.add(queue);
			actionLoop: while(true) {
				String text = null;

				while(text == null) {
					try {
						text = queue.poll(100L, TimeUnit.MILLISECONDS);
						if(actions == null || interrupted) {
							break actionLoop;
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				synchronized(actions) {
					for(Map.Entry<IMatch, Runnable> action : actions.entrySet()) {
						if(action.getKey().matches(text))
							action.getValue().run();
					}
				}
			}
			textWaiters.remove(queue);
		}
	}
	
	public IMatch addAction(Runnable action, String text) {
		if(actions.size() == 0) {
			new Thread(new ScriptActionThread()).start();
		}
		IMatch m = new RegexMatch(text);
		synchronized(actions) {
			actions.put(m, action);
		}
		return m;
	}
	
	public void clearActions() {
		actions.clear();
	}
	
	public void removeAction(IMatch action) {
		synchronized(actions) {
			actions.remove(action);
		}
	}
	
	public void removeAction(String text) {
		synchronized(actions) {
			for(IMatch match : actions.keySet()) {
				// remove the element with the same name as text
				if(match.getText().equals(text)) {
					actions.remove(match);
				}
			}
		}
	}
	
}
