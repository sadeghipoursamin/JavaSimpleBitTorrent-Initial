package tracker.controllers;

import common.models.CLICommands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TrackerCommands implements CLICommands {
	END("exit"),
	REFRESH_FILES("refresh_files"),
	RESET_CONNECTIONS("reset_connections"),
	LIST_PEERS("list_peers"),
	LIST_FILES("list_files\\s+([^:]+):(\\d+)"),
	GET_RECEIVES("get_receives\\s+([^:]+):(\\d+)"),
	GET_SENDS("get_sends\\s+([^:]+):(\\d+)");

	private final String regex;

	TrackerCommands(String regex) {
		this.regex = regex;
	}

	@Override
	public Matcher getMatcher(String input) {
		return Pattern.compile(regex).matcher(input);
	}
}