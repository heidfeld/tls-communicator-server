package test;

import login.Encrypter;
import login.User;
import login.UserLogValidator;

public class CreateUsersDatabase {

	public static void main(String[] args) {
		
		String login1 = "test";
		String login2 = "bke";
		String login3 = "admin";
		String login4 = "unauthorized";
		
		String password1 = Encrypter.cryptWithMD5("test");
		String password2 = Encrypter.cryptWithMD5("bke");
		String password3 = Encrypter.cryptWithMD5("admin");
		String password4 = Encrypter.cryptWithMD5("unauthorized");
		
		User user1 = new User(login1, password1);
		User user2 = new User(login2, password2);
		User user3 = new User(login3, password3);
		User unauthorized = new User(login4, password4);
		
		UserLogValidator ulv = new UserLogValidator();
		ulv.addNewUser(user1);
		ulv.addNewUser(user2);
		ulv.addNewUser(user3);
		System.out.println(user1.toString() + " Authorization result: " + ulv.isAuthorized(user1));
		System.out.println(user2.toString() + " Authorization result: " + ulv.isAuthorized(user2));
		System.out.println(user3.toString() + " Authorization result: " + ulv.isAuthorized(user3));
		System.out.println(unauthorized.toString() + " Authorization result: " + ulv.isAuthorized(unauthorized));

	}

}
