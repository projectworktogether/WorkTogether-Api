import com.github.m4schini.FancyLog.Log;

import net.freeutils.httpserver.HTTPServer;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.Map;

public class Handler {
  
  private static int sendResponse(HTTPServer.Response response, Integer status, JSONObject header, Object results) {
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
        Map<String, String> params = request.getParams();
        Password pw = new Password();
      
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
          sendResponse(response, 400, header, results);
        } catch (SQLException e) {
          Log.exception(e);
          sendResponse(response, 500, header, results);
        }
        Log.success("[User.Create] Successful request");
        return sendResponse(response, 200, header, results);
      }
    }
  
    public static class Delete implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Log.error("test");
        JSONObject header = new JSONObject();
        JSONObject results = new JSONObject();
        return sendResponse(response, 400, header, results);
      
      }
    }
  
    public static class Login implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Map<String, String> params = request.getParams();
        JSONObject header = new JSONObject();
        JSONObject results = new JSONObject();
        if (params.keySet().contains("session")) {
          try {
            if (Sessions.validate(params.get("session"), Integer.parseInt(params.get("ID")))) {
              Log.success("[Login] Successful request");
              return sendResponse(response, 200, new JSONObject().put("info", "login with session"), new JSONObject());
            } else {
              Log.warning("[Login] Failed request");
              return sendResponse(response, 400, new JSONObject(), new JSONObject());
            }
          } catch (SQLException e) {
            Log.exception(e);
            return sendResponse(response, 500, new JSONObject(), new JSONObject());
          } catch (NumberFormatException e) {
            return sendResponse(response, 500, new JSONObject().put("causedBy", "No user ID were given"), new JSONObject());
          }
        } else {
          DBConnection db;
          String storedPassword;
          Integer userID;
  
          try {
            db = new DBConnection();
            ResultSet resultSet = db.execute("SELECT ID,hashPassword FROM user WHERE emailAdr=?",
                    params.get("emailadr"));
            resultSet.next();
            userID = resultSet.getInt("ID");
    
            storedPassword = resultSet.getString("hashPassword");
          } catch (SQLDataException e) {
            Log.warning("[Login] Failed request");
            return sendResponse(response, 400, header, results);
          } catch (SQLException e) {
            Log.error("[Login] Collection of Password failed");
            Log.exception(e);
            return sendResponse(response, 500, header, results);
          }
  
          if (new Password().authenticate(params.get("password").toCharArray(), storedPassword)) {
            String sessionID = Sessions.create(90);
            try {
              db.update("INSERT INTO sessions VALUES(?,?)",
                      sessionID,
                      userID);
            } catch (SQLException e) {
              Log.exception(e);
              return sendResponse(response, 500, new JSONObject(), new JSONObject());
            }
            header.put("ID", userID);
            header.put("session", sessionID);
            header.put("info", "login with logindata");
            Log.success("[Login] Successful request");
            return sendResponse(response, 200, header, results);
          } else {
            Log.warning("[Login] Failed request");
            return sendResponse(response, 400, header, results);
          }
        }
      }
    }
    
    public static class Validate implements HTTPServer.ContextHandler {
    
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Map<String, String> params = request.getParams();
        try {
          if (Sessions.validate(params.get("session"), Integer.parseInt(params.get("userID")))) {
            return sendResponse(response, 200, new JSONObject(), new JSONObject());
          } else {
            return sendResponse(response, 400, new JSONObject(), new JSONObject());
          }
        } catch (SQLException e) {
          Log.exception(e);
          return sendResponse(response, 500, new JSONObject(), new JSONObject());
        }
      }
    }
  }
  
  static class Event {
    public static class Create implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Log.status("Req: Create");
        
        return sendResponse(response, 501, new JSONObject(), new JSONObject());
      }
    }
  
    public static class Delete implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Log.status("Req: Delete");
        return sendResponse(response, 501, new JSONObject(), new JSONObject());
      }
    }
  
    public static class Update implements HTTPServer.ContextHandler {
      @Override
      public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
        Log.status("Req: Update");
        return sendResponse(response, 501, new JSONObject(), new JSONObject());
      }
    }
  }
  
  public static class Test implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      Log.success("Yeahhh");
      return sendResponse(
              response,
              200,
              new JSONObject().put("desc", "This is a test"),
              new JSONObject()
      );
    }
  }
}



