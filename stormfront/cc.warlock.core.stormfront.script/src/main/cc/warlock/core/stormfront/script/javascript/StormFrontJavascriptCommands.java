/**
 * 
 */
package cc.warlock.core.stormfront.script.javascript;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

import cc.warlock.core.script.Match;
import cc.warlock.core.script.javascript.JavascriptCommands;
import cc.warlock.core.script.javascript.JavascriptCommands.JavascriptStopException;
import cc.warlock.core.stormfront.script.IStormFrontScriptCommand;
import cc.warlock.core.stormfront.script.IStormFrontScriptCommands;

public class StormFrontJavascriptCommands 
{
	protected JavascriptCommands commands;
	protected IStormFrontScriptCommands sfCommands;
	
	public StormFrontJavascriptCommands (JavascriptCommands commands, IStormFrontScriptCommands sfCommands)
	{
		this.commands = commands;
		this.sfCommands = sfCommands;
	}

	protected class StormFrontJavascriptCommand implements IStormFrontScriptCommand 
	{
		protected Function function;
		
		public StormFrontJavascriptCommand (Function function)
		{
			this.function = function;
		}
		
		public void execute() {
			function.call(commands.getScript().getContext(), commands.getScript().getScope(), null, new Object[] {});
		}
	}
	
	// IStormFrontScriptCommands delegated methods
	public void addAction(Function action, String text) {
		sfCommands.addAction(new StormFrontJavascriptCommand(action), text);
	}

	public void removeAction(String text) {
		sfCommands.removeAction(text);
	}
	
	public void addMatch(Match match) {
		sfCommands.addMatch(match);
	}

	public void clearActions() {
		sfCommands.clearActions();
	}

	public Match matchWait(double timeout) {
		return sfCommands.matchWait(timeout);
	}

	public void nextRoom() {
		sfCommands.nextRoom();
	}

	public void pause(double seconds) {
		sfCommands.pause(seconds);
	}

	public void waitForRoundtime() {
		sfCommands.waitForRoundtime();
	}

	public void waitNextRoom() {
		sfCommands.waitNextRoom();
	}
	
	// JavascriptCommands delegated methods
	public void echo(String text) {
		commands.echo(text);
	}

	public void exit() throws JavascriptStopException {
		commands.exit();
	}

	public void include(String otherScript) {
		commands.include(otherScript);
	}

	public Match match(String text, Function function, Scriptable object) {
		return commands.match(text, function, object);
	}

	public Match match(String text, Function function) {
		return commands.match(text, function);
	}

	public Match matchRe(String text, Function function,
			Boolean ignoreCase, Scriptable object) {
		return commands.matchRe(text, function, ignoreCase, object);
	}

	public Match matchRe(String text, Function function, Boolean ignoreCase) {
		return commands.matchRe(text, function, ignoreCase);
	}

	public Match matchRe(String text, Function function) {
		return commands.matchRe(text, function);
	}

	public Match matchWait(NativeArray matches) {
		return commands.matchWait(matches);
	}

	public void move(String direction) {
		commands.move(direction);
	}

	public void pause(int seconds) {
		commands.pause(seconds);
	}

	public void put(String text) {
		commands.put(text);
	}

	public void stop() {
		commands.stop();
	}

	public void waitFor(Match match) {
		commands.waitFor(match);
	}

	public void waitFor(String string) {
		commands.waitFor(string);
	}

	public void waitForPrompt() {
		commands.waitForPrompt();
	}

	public void waitForRe(String string, Boolean ignoreCase) {
		commands.waitForRe(string, ignoreCase);
	}

	public void waitForRe(String string) {
		commands.waitForRe(string);
	}
}