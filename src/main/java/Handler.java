import com.github.m4schini.FancyLog.Log;

import net.freeutils.httpserver.HTTPServer;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Handler {
  
  private static int sendReponse(HTTPServer.Response response, Integer status, JSONObject header, Object results) {
    try {
      header.put("status", status);
      response.getHeaders().add("Access-Control-Allow-Origin", "*");
      response.getHeaders().add("Content-Type", "application/json");
      
      JSONObject returnJSON = new JSONObject();
      returnJSON.put("header", header);
      returnJSON.put("results", results);
      
      response.send(status, returnJSON.toString());
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }
  
  static class User {
    public static class Create implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Log.status("'CreateUser' Request");
        Map<String, String> params = request.getParams();
        PWAuth pw = new PWAuth();
      
        JSONObject header = new JSONObject();
        JSONObject results = new JSONObject();
        try {
          new DBConnection().update("INSERT INTO user SET vName=?, nName=?, emailAdr=?, hashPassword=?, role=?, plz=?",
                  params.get("vName"),
                  params.get("nName"),
                  params.get("emailAdr"),
                  pw.hash(params.get("password").toCharArray()),
                  params.get("role"),
                  params.get("plz")
          );
        } catch (NullPointerException e) {
          Log.exception(e);
          sendReponse(response, 400, header, results);
        } catch (SQLException e) {
          Log.exception(e);
          sendReponse(response, 500, header, results);
        }
        return sendReponse(response, 200, header, results);
      }
    }
  
    public static class Delete implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Log.error("test");
        JSONObject header = new JSONObject();
        JSONObject results = new JSONObject();
        return sendReponse(response, 400, header, results);
      
      }
    }
  
    public static class Login implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Map<String, String> params = request.getParams();
      
        JSONObject header = new JSONObject();
        JSONObject results = new JSONObject();
        String storedPassword, userID;
      
        try {
          ResultSet resultSet = new DBConnection().execute("SELECT Id,hashPassword FROM user WHERE emailAdr=?",
                  params.get("emailadr"));
          resultSet.next();
        
          userID = resultSet.getString("id");
          storedPassword = resultSet.getString("hashPassword");
        } catch (SQLException e) {
          Log.error("Collection of Password failed");
          Log.exception(e);
          return sendReponse(response, 500, header, results);
        }
      
        if (new PWAuth().authenticate(params.get("password").toCharArray(), storedPassword)) {
          //If login is succesfull user is being redirected back to Referer with http-status 303
          return sendReponse(response, 200, header, results);
        } else {
          return sendReponse(response, 400, header, results);
        }
      }
    }
  }
  
  static class Event {
    public static class Create implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Log.status("Req: Create");
        return sendReponse(response, 501, new JSONObject(), new JSONObject());
      }
    }
  
    public static class Delete implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Log.status("Req: Delete");
        return sendReponse(response, 501, new JSONObject(), new JSONObject());
      }
    }
  
    public static class Update implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Log.status("Req: Update");
        return sendReponse(response, 501, new JSONObject(), new JSONObject());
      }
    }
  }
  
  public static class Test implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      Log.success("Yeahhh");
      return sendReponse(
              response,
              200,
              new JSONObject().put("desc", "This is a test"),
              new JSONObject()
      );
    }
  }
}



