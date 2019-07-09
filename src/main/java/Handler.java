import com.github.m4schini.FancyLog.Log;

import net.freeutils.httpserver.HTTPServer;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class Handler {
  private static DBConnection dbconnection;
  
  public Handler() {
    dbconnection = new DBConnection();
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
  
  static class CreateUser implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      Map<String, String> params = request.getParams();
      
      JSONObject header = new JSONObject();
      JSONObject results = new JSONObject();
  
      try {
        dbconnection.update("INSERT INTO Nutzer SET Vorname=?, Nachname=?, Mail=?, Passwort=?, Admin=?, PLZ=?",
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
  
  static class DeleteUser implements HTTPServer.ContextHandler {
    @Override
    public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
      return 0;
    }
  }
  
  
}



