package login;

import java.security.MessageDigest;

public class Encrypter {

	private static MessageDigest md;

	public static String cryptWithMD5(String pass) {
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] passBytes = pass.getBytes();
			md.reset();
			byte[] digested = md.digest(passBytes);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < digested.length; i++) {
				sb.append(Integer.toHexString(0xff & digested[i]));
			}
			return sb.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Cannot encrypt password with MD5", ex);
		}
	}
}
