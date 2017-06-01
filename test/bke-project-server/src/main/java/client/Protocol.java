package client;

import java.util.Objects;

import login.User;
import login.UserLogValidator;

/**
 * Login format: #LOGIN#login;password
 * Message format: #MESS#message
 * 
 * @author £ukasz
 *
 */

public class Protocol {

	public enum ServerCommand {
		LOGIN, MESS, NONE
	}

	public String getServerResponseForCommand(String command) {
		ServerCommand sc = matchServerCommand(command);
		if(sc.equals(ServerCommand.LOGIN)) {
			boolean isAuthorized = validateUser(command);
			return Boolean.toString(isAuthorized);
		} else if(sc.equals(ServerCommand.MESS)) {
			return getMessage(command);
		} else {
			return "";
		}
	}
	
	private ServerCommand matchServerCommand(String command) {
		Objects.requireNonNull(command);
		String[] parts = command.split("#");
		if (parts.length > 0) {
			String serverCommand = parts[1];
			for (ServerCommand sc : ServerCommand.values()) {
				if(sc.toString().equalsIgnoreCase(serverCommand)) {
					return sc;
				}
			}
		}
		return ServerCommand.NONE;
	}
	
	private boolean validateUser(String command) {
		String info = getInfoFromCommand(command);
		String[] parts = info.split(";");
		String login = parts[0];
		String password = parts[1];
		UserLogValidator ulv = new UserLogValidator();
		User user = new User(login, password);
		boolean authorized = ulv.isAuthorized(user);
		return authorized;
	}
	
	private String getMessage(String command) {
		return getInfoFromCommand(command);
	}
	
	private String getInfoFromCommand(String command) {
		ServerCommand cmd = matchServerCommand(command);
		int startIndex = cmd.toString().length() + 2;
		return command.substring(startIndex);
	}
	
	public String buildLoginCommand(String login, String password) {
		return "#LOGIN#" + login + ";" + password;
	}
	
	public String buildMessageCommand(String message) {
		return "#MESS#" + message;
	}

}
