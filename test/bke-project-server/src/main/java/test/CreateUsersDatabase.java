package test;

import login.User;
import login.UserLogValidator;

public class CreateUsersDatabase {

	public static void main(String[] args) {
		User user1 = new User("test", "test");
		User user2 = new User("bke", "bke");
		User user3 = new User("admin", "admin");
		User unauthorized = new User("unauthorized", "unauthorized");
		
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
