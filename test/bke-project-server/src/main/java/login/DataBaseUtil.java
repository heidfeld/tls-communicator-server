package login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseUtil {
	
	public static final String DRIVER = "org.sqlite.JDBC";
    public static final String DB_URL = "jdbc:sqlite:users.db";
 
    private static Connection conn;
    private static Statement stat;
    
    static {
        try {
            Class.forName(DataBaseUtil.DRIVER);
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
 
    public static boolean createTables()  {
    	//String dropUsers = "DROP TABLE IF EXISTS users;";
        String createUsers = "CREATE TABLE IF NOT EXISTS users (login varchar(255) PRIMARY KEY, password varchar(255))";
        String createTls = "CREATE TABLE IF NOT EXISTS tls (password varchar(255));";
        try {
        	//stat.execute(dropUsers);
            stat.execute(createUsers);
            stat.execute(createTls);
        } catch (SQLException e) {
            System.err.println("Blad przy tworzeniu tabeli");
            e.printStackTrace();
            return false;
        }
        return true;
    }
	
	public static boolean addNewUser(User user) {
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
	
	public static boolean setTlsPassword(String password) {
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "insert into tls values (?);");
            prepStmt.setString(1, password);
            prepStmt.execute();
        } catch (SQLException e) {
            System.err.println("Insert error");
            e.printStackTrace();
            return false;
        }
        return true;
	}
	
	public static char[] getTlsPassword() {
		return getTextTlsPassword().toCharArray();
	}
	
	private static String getTextTlsPassword() {
		try {
            ResultSet result = stat.executeQuery("SELECT password FROM tls");
            String password = "";
            while(result.next()) {
                password = result.getString("password");
                break;
            }
            return password;
        } catch (SQLException e) {
            throw new RuntimeException("Cannot get tls password from database.");
        }
	}
	
	public static boolean isAuthorized(User user) {
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

    public static void closeConnection() {
        try {
        	stat.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Problem z zamknieciem polaczenia");
            e.printStackTrace();
        }
    }
	
}
