import com.github.m4schini.FancyLog.Log;

import net.freeutils.httpserver.HTTPServer;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Handler {
  private static DBConnection db = new DBConnection();
  private static PWAuth pw = new PWAuth();
  
  private static int sendReponse(HTTPServer.Response response, Integer status, JSONObject header, Object results) {
    try {
      header.put("status", status);
      response.getHeaders().add("Access-Control-Allow-Origin", "*");
      response.getHeaders().add("Content-Type", "application/json");
      response.send(status, answerString(header, results));
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }
  
  private static String answerString(JSONObject header, Object results) {
    JSONObject returnJSON = new JSONObject();
    returnJSON.put("header", header);
    returnJSON.put("results", results);
    return returnJSON.toString();
  }
  
  public static class CreateUser implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      Log.status("New 'CreateUser' Request");
      Map<String, String> params = request.getParams();
      pw = new PWAuth();
      db = new DBConnection();
      
      JSONObject header = new JSONObject();
      JSONObject results = new JSONObject();
      Log.status("Variables and Stuff");
      try {
        Log.status("Pre SQL Query");
  
        db.update("INSERT INTO user SET vName=?, nName=?, emailAdr=?, hashPassword=?, role=?, plz=?",
                params.get("vName"),
                params.get("nName"),
                params.get("emailAdr"),
                pw.hash(params.get("password").toCharArray()),
                params.get("role"),
                params.get("plz")
        );
        Log.status("Post SQL Query");
  
      } catch (NullPointerException e) {
        Log.exception(e);
        sendReponse(response, 400, header, results);
      } catch (SQLException e) {
        Log.exception(e);
        sendReponse(response, 500, header, results);
      }
      Log.status("Alles Fertig");
      return sendReponse(response, 200, header, results);
    }
  }
  
  public static class DeleteUser implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      Log.error("test");
      JSONObject header = new JSONObject();
      JSONObject results = new JSONObject();
      return sendReponse(response, 400, header, results);
      
    }
  }
  
  public static class LoginUser implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      Map<String, String> params = request.getParams();
      
      JSONObject header = new JSONObject();
      JSONObject results = new JSONObject();
      String storedPassword, userID;
      
      try {
        ResultSet resultSet = db.execute("SELECT Id,hashPassword FROM user WHERE emailAdr=?",
                params.get("emailadr"));
        
        resultSet.next();
        userID = resultSet.getString("id");
        storedPassword = resultSet.getString("hashPassword");
      } catch (SQLException e) {
        Log.error("Collection of Password failed");
        Log.exception(e);
        return sendReponse(response, 500, header, results);
      }
      
      if (pw.authenticate(params.get("password").toCharArray(), storedPassword)) {
        //If login is succesfull user is being redirected back to Referer with http-status 303
        return sendReponse(response, 200, header, results);
      } else {
        return sendReponse(response, 400, header, results);
      }
    }
  }
  
  public static class Test implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      Log.success("Yeahhh");
      return 0;
    }
  }
}



