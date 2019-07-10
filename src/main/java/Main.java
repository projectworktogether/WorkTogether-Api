import com.github.m4schini.FancyLog.Log;

import net.freeutils.httpserver.HTTPServer;

import java.io.IOException;
import java.util.Scanner;

public class Main {
  
  public static void main(String[] args) throws IOException {
    
    HTTPServer server = new HTTPServer(4200);
    HTTPServer.VirtualHost host = server.getVirtualHost(null);
    host.addContext("/user/create", new Handler.CreateUser(), "POST");
    host.addContext("/user/login", new Handler.LoginUser(), "POST");
    host.addContext("/user/delete", new Handler.DeleteUser());
    server.start();
  
    Log.success("Server started");
  
    Config.load(Config.PATH_CONFIG);
    
    Scanner scanner = new Scanner(System.in);
    while (true) try {
      //
    } catch (Exception e) {
      Log.exception(e);
    }
  }
}
