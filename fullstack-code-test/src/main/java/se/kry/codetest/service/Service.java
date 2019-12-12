package se.kry.codetest.service;

import java.net.URL;
import java.util.Date;

public class Service {

    private String name;

    private String url;

    private String status;

    private Date creationDate;

    public Service(String url, String name, Date creationDate) {
        this.url = url;
        this.name = name;
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return url+" "+name + " " + creationDate + " " +status;
    }

}
