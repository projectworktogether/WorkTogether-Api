import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

class Sessions {
  private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
  
  static String create(Integer expireInDays) {
    return
            UUID.randomUUID().toString().replace("-", "")
                    + "AND"
                    + addDays(new Date(), expireInDays);
  }
  
  private static boolean isValidDate(String stringDate) throws ParseException {
    Date date = new SimpleDateFormat(DATE_PATTERN).parse(stringDate);
    return new Date().before(date);
  }
  
  static boolean validate(String sessionID, Integer userID) throws SQLException {
    ResultSet resultSet = new DBConnection().execute("SELECT * FROM sessions WHERE session = ?",
            sessionID);
    resultSet.next();
    String storedID = resultSet.getString("session");
    try {
      if (Sessions.isValidDate(storedID.split("AND")[1])
              && userID == resultSet.getInt("ID_user")) {
        
        return storedID.split("AND")[0].equals(sessionID.split("AND")[0]);
      } else {
        return false;
      }
    } catch (ParseException e) {
      e.printStackTrace();
      return false;
    }
  }

  private static String addDays(Date date, int days) {
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTime(date);
    cal.add(Calendar.DATE, days);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
    return simpleDateFormat.format(cal.getTime());
  }
}

/**
 * THIS WHOLE SECTION BELOW IS COPIED FROM SOME STACK OVERFLOW ANSWER I CANT FIND ANYMORE
 */
public class Password {
  /**
   * Each token produced by this class uses this identifier as a prefix.
   */
  public static final String ID = "$42$";
  
  /**
   * The minimum recommended cost, used by default
   */
  public static final int DEFAULT_COST = 16;
  
  private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
  
  private static final int SIZE = 128;
  
  private static final Pattern layout = Pattern.compile("\\$42\\$(\\d\\d?)\\$(.{43})");
  
  private final SecureRandom random;
  
  private final int cost;
  
  public Password()
  {
    this(DEFAULT_COST);
  }
  
  /**
   * Create a password manager with a specified cost
   *
   * @param cost the exponential computational cost of hashing a password, 0 to 30
   */
  public Password(int cost) {
    iterations(cost); /* Validate cost */
    this.cost = cost;
    this.random = new SecureRandom();
  }
  
  private static int iterations(int cost)
  {
    if ((cost < 0) || (cost > 30))
      throw new IllegalArgumentException("cost: " + cost);
    return 1 << cost;
  }
  
  /**
   * Hash a password for storage.
   *
   * @return a secure authentication token to be stored for later authentication
   */
  public String hash(char[] password) {
    byte[] salt = new byte[SIZE / 8];
    random.nextBytes(salt);
    byte[] dk = pbkdf2(password, salt, 1 << cost);
    byte[] hash = new byte[salt.length + dk.length];
    System.arraycopy(salt, 0, hash, 0, salt.length);
    System.arraycopy(dk, 0, hash, salt.length, dk.length);
    Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
    return ID + cost + '$' + enc.encodeToString(hash);
  }
  
  /**
   * Authenticate with a password and a stored password token.
   *
   * @return true if the password and token match
   */
  public boolean authenticate(char[] password, String token) {
    Matcher m = layout.matcher(token);
    if (!m.matches())
      throw new IllegalArgumentException("Invalid token format");
    int iterations = iterations(Integer.parseInt(m.group(1)));
    byte[] hash = Base64.getUrlDecoder().decode(m.group(2));
    byte[] salt = Arrays.copyOfRange(hash, 0, SIZE / 8);
    byte[] check = pbkdf2(password, salt, iterations);
    int zero = 0;
    for (int idx = 0; idx < check.length; ++idx)
      zero |= hash[salt.length + idx] ^ check[idx];
    return zero == 0;
  }
  
  private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
    KeySpec spec = new PBEKeySpec(password, salt, iterations, SIZE);
    try {
      SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
      return f.generateSecret(spec).getEncoded();
    }
    catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Missing algorithm: " + ALGORITHM, ex);
    }
    catch (InvalidKeySpecException ex) {
      throw new IllegalStateException("Invalid SecretKeyFactory", ex);
    }
  }
}
