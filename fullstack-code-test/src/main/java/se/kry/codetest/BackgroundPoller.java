package se.kry.codetest;

import io.vertx.core.Future;
import se.kry.codetest.service.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackgroundPoller {

  public void pollServices(Map<String, Service> services) {
    Set<String> entries = services.keySet();
    for(String key: entries) {
      String url = key;
      String status = getStatus(url);

      Service service = services.get(key);
      service.setStatus(status);
    }
  }

  /**
   * Ping method found on https://java2blog.com/how-to-ping-url-and-get-status-in-java/
   * @param url
   * @return
   * @throws IOException
   */
  private String getStatus(String url) {

    String result = "";
    try {
      URL urlObj = new URL(url);
      HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(1500);
      con.connect();

      int code = con.getResponseCode();
      if (code == 200) {
        result = "OK";
      }
    } catch (Exception e) {
      result = "KO";
    }
    return result;
  }
}
