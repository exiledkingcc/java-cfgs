package org.exiledkingcc.java.cfgs.source;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.exiledkingcc.java.cfgs.ConfigSource;

import java.io.*;

public class HttpSource implements ConfigSource {

    private final String basUrl;
    private final HttpClient httpClient;

    public HttpSource(String path) {
        this.basUrl = path;
        this.httpClient = HttpClients.createDefault();
        try {
            this.httpClient.execute(new HttpGet(this.basUrl));
        } catch (IOException e) {
            throw  new IllegalArgumentException("Path <" + path + "> is NOT a valid url path", e);
        }
    }

    @Override
    public InputStream getConfigData(String name) {
        InputStream inputStream;
        HttpResponse response;
        try {
            response = this.httpClient.execute(new HttpGet(this.getURL(name)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new RuntimeException("Can NOT get config <" + name + ">: "
                    + response.getStatusLine());
        }
        try {
            inputStream = response.getEntity().getContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    private String getURL(String name) {
        if (this.basUrl.endsWith("/")) {
            return this.basUrl + name;
        } else {
            return this.basUrl + "/" + name;
        }
    }
}
