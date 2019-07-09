import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class Config {
  static final String PATH_CONFIG = "config.properties";
  
  /**
   * Load {@link Properties} from specified file.
   *
   * @param path to a properties file
   * @return Properties of specified file
   * @throws IOException, FileNotFoundException thrown by InputStream and Properties
   */
  static Properties load(String path) throws IOException {
    Properties propertiesFile = new Properties();
    FileInputStream inputStream = new FileInputStream(path);
    propertiesFile.load(inputStream);
    inputStream.close();
    return propertiesFile;
  }
  
  /**
   * Saves specified {@link Properties} to specified file
   *
   * @param path to destination
   * @param properties to save
   * @throws IOException IOException, FileNotFoundException thrown by OutputStream and Properties
   */
  static void save(String path, Properties properties) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(path);
    properties.store(outputStream, "WorkTogether-Api Config");
    outputStream.close();
  }
}