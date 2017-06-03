package login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserLogValidator {
	
	public static final String DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL = "jdbc:sqlite:users.db";
 
    private Connection conn;
    private Statement stat;
    
    public UserLogValidator() {
        try {
            Class.forName(UserLogValidator.DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Brak sterownika JDBC");
            e.printStackTrace();
        }
 
        try {
            conn = DriverManager.getConnection(DB_URL);
            stat = conn.createStatement();
        } catch (SQLException e) {
            System.err.println("Problem z otwarciem polaczenia");
            e.printStackTrace();
        }
 
        createTables();
    }
 
    public boolean createTables()  {
    	String dropUsers = "DROP TABLE IF EXISTS users;";
        String createUsers = "CREATE TABLE IF NOT EXISTS users (login varchar(255) PRIMARY KEY, password varchar(255))";
        try {
        	stat.execute(dropUsers);
            stat.execute(createUsers);
        } catch (SQLException e) {
            System.err.println("Blad przy tworzeniu tabeli");
            e.printStackTrace();
            return false;
        }
        return true;
    }
	
	public boolean addNewUser(User user) {
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "insert into users values (?, ?);");
            prepStmt.setString(1, user.getName());
            prepStmt.setString(2, user.getPassword());
            prepStmt.execute();
        } catch (SQLException e) {
            System.err.println("Blad przy wstawianiu uzytkownika");
            e.printStackTrace();
            return false;
        }
        return true;
	}
	
	public boolean isAuthorized(User user) {
		try {
            ResultSet result = stat.executeQuery("SELECT * FROM users");
            String login, password;
            while(result.next()) {
                login = result.getString("login");
                password = result.getString("password");
                if(login.equalsIgnoreCase(user.getName()) && password.equals(user.getPassword())) {
                	return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
		return false;
	}

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Problem z zamknieciem polaczenia");
            e.printStackTrace();
        }
    }
	
}
