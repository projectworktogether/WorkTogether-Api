import com.github.m4schini.FancyLog.Log;

import net.freeutils.httpserver.HTTPServer;

import java.io.IOException;
import java.util.Scanner;

public class Main {
  
  public static void main(String[] args) throws IOException {
    Config.load(Config.PATH_CONFIG);
  
    HTTPServer server = new HTTPServer(4200);
    HTTPServer.VirtualHost host = server.getVirtualHost(null);
    
    host.addContext("/test", new Handler.Test());
    
    host.addContext("/user/create",   new Handler.User.Create(), "POST");
    host.addContext("/user/login" ,   new Handler.User.Login(), "POST");
    host.addContext("/user/delete",   new Handler.User.Delete(), "POST");
    host.addContext("/user/validate", new Handler.User.Validate(), "POST");
  
    host.addContext("/event/create",  new Handler.Event.Create(), "POST");
    host.addContext("/event/delete",  new Handler.Event.Delete(), "POST");
    host.addContext("/event/update",  new Handler.Event.Update(), "POST");
  
    server.start();
  
    Log.success("Server started");
    
    Scanner scanner = new Scanner(System.in);
    while (true) try {
      //
    } catch (Exception e) {
      Log.exception(e);
    }
  }
}
