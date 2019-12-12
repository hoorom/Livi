package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.service.Service;
import se.kry.codetest.service.ServiceFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private Map<String, Service> services = new HashMap<>();
  private DBConnector connector;
  private BackgroundPoller poller = new BackgroundPoller();
  private ServiceFactory factory = new ServiceFactory();

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    initServices();

    vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(services));
    setRoutes(router);
    vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(8080, result -> {
              if (result.succeeded()) {
                System.out.println("KRY code test service started");
                startFuture.complete();
              } else {
                startFuture.fail(result.cause());
              }
            });
  }

  /**
   * Retrieve services from DB
   * @throws InterruptedException
   */
  private void initServices() {

    Future<ResultSet> query = connector.query("select * from service");

    query.setHandler(done -> {
      if(done.succeeded()){
        ResultSet resultSet = query.result();

        List<JsonObject> rows = resultSet.getRows();

        for(JsonObject json: rows) {
          Service service = factory.getService(json);
          services.put(service.getUrl(), service);
        }
        poller.pollServices(services);
      } else {
        done.cause().printStackTrace();
      }
    });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());
    router.get("/service").handler(req -> {
      List<JsonObject> jsonServices = services
              .entrySet()
              .stream()
              .map(service ->
                      new JsonObject()
                              .put("url", service.getValue().getUrl())
                              .put("name", service.getValue().getName())
                              .put("status", service.getValue().getStatus())
                              .put("creationDate", service.getValue().getCreationDate().toString()))
              .collect(Collectors.toList());
      req.response()
              .putHeader("content-type", "application/json")
              .end(new JsonArray(jsonServices).encode());
    });
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();

      boolean inserted = addNewService(jsonBody);
      req.response()
              .putHeader("content-type", "text/plain")
              .end(inserted?"OK":"KO");
    });
    router.post("/delete").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();

      boolean deleted = deleteService(jsonBody);

      req.response()
              .putHeader("content-type", "text/plain")
              .end(deleted?"OK":"KO");
    });

  }

  /**
   * Add a new service from the JsonObject
   * @param jsonBody
   * @return il the service as been registered
   */
  private boolean addNewService(JsonObject jsonBody) {

    String url = jsonBody.getString("url");
    if(isServiceRegistered(url)) {
      return false;
    }

    Service service = factory.getService(jsonBody);
    services.put(url, service);
    insertNewService(service);

    return true;
  }

  /**
   * Insert a service into the DB
   * @param service The service to insert
   */
  private void insertNewService(Service service) {
    String query = "INSERT INTO service (url, name) values ('" + service.getUrl() + "', '" + service.getName() + "')";
    Future<ResultSet> result = connector.query(query);
    result.setHandler(done -> {
      if(done.succeeded()){
        System.out.println(service.getUrl() + " : inserted");
      } else {
        done.cause().printStackTrace();
      }
    });
  }

  /**
   * Delete a service
   * @return If the service has been deleted
   */
  private boolean deleteService(JsonObject json) {
    String url = json.getString("url");
    if(!isServiceRegistered(url)) {
      return false;
    }

    String deleteQuery = "DELETE FROM service WHERE url = '" + url + "'";
    Future<ResultSet> result = connector.query(deleteQuery);
    result.setHandler(done -> {
      if(done.succeeded()){
        services.remove(url);
        System.out.println(url + " : deleted");
      } else {
        done.cause().printStackTrace();
      }
    });

    return true;
  }

  private boolean isServiceRegistered(String url) {
    Service registeredService = services.get(url);
    if(registeredService != null) {
      return true;
    }
    return false;
  }

}



