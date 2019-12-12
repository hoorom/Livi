package se.kry.codetest.service;

import io.netty.util.internal.StringUtil;
import io.vertx.core.json.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServiceFactory {

    public Service getService(JsonObject json) {

        String url = json.getString("url");
        String name = json.getString("name");
        String creationDateString = json.getString("creationDate");

        Date creationDate = null;
        if(StringUtil.isNullOrEmpty(creationDateString)) {
            creationDate = new Date();
        } else {
            String pattern = "yyyy-MM-dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            try {
                creationDate = simpleDateFormat.parse(creationDateString);
            } catch(ParseException e) {
                System.err.println("Impossible to parse : " + creationDateString);
            }
        }

        Service service = new Service(url, name, creationDate);
        return service;
    }
}
