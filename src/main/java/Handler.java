import com.github.m4schini.FancyLog.Log;

import net.freeutils.httpserver.HTTPServer;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Handler {
  private static DBConnection db;
  private static PWAuth pw;
  
  public Handler() {
    db = new DBConnection();
    pw = new PWAuth();
  }
  
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
      Map<String, String> params = request.getParams();
      
      JSONObject header = new JSONObject();
      JSONObject results = new JSONObject();
  
      try {
        db.update("INSERT INTO Nutzer SET Vorname=?, Nachname=?, Mail=?, Passwort=?, Admin=?, PLZ=?",
                params.get("Vorname"),
                params.get("Nachname"),
                params.get("Mail"),
                params.get("Passwort"),
                params.get("Admin"),
                params.get("PLZ")
        );
        
      } catch (SQLException e) {
        Log.exception(e);
      }
      sendReponse(response, 200, header, results);
      return 0;
    }
  }
  
  public static class DeleteUser implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      return 0;
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
        try {
          header.put("status", 303);
      
          response.getHeaders().add("Access-Control-Allow-Origin", "*");
          response.getHeaders().add("Location", request.getHeaders().get("Referer"));
          response.send(303, "You should be redirected any second");
        } catch (IOException e) {
          Log.error("Redirect was unsuccessful");
          //e.printStackTrace();
          return -1;
        }
        return 0;
      } else {
        return sendReponse(response, 400, header, results);
      }
    }
  }
}



