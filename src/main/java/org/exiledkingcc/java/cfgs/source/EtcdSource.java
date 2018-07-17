package org.exiledkingcc.java.cfgs.source;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.ClientBuilder;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import org.exiledkingcc.java.cfgs.ConfigSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EtcdSource implements ConfigSource {

    private final Client client;
    private final String prefix;

    public EtcdSource(String path) {
        path = path.substring(7);
        ClientBuilder clientBuilder = Client.builder();
        if (path.contains("@")) {
            int p = path.indexOf('@');
            int q = path.indexOf(':');
            String user = path.substring(0, q);
            String password = path.substring(q + 1, p);
            clientBuilder.user(ByteSequence.fromString(user))
                    .password(ByteSequence.fromString(password));
            path = path.substring(p + 1);
        }
        String prefix = "";
        if (path.contains("/")) {
            int p = path.indexOf("/");
            prefix = path.substring(p + 1);
            path = path.substring(0, p);
        }
        this.prefix = prefix;
        String[] pp = path.split(",");
        List<String> endpoints = new ArrayList<>(pp.length);
        for (String p: pp) {
            endpoints.add("http://" + p);
        }
        clientBuilder.endpoints(endpoints);
        this.client = clientBuilder.build();
    }

    @Override
    protected void finalize() throws Throwable {
        this.client.close();
        super.finalize();
    }

    @Override
    public InputStream getConfigData(String name) {
        ByteSequence key = ByteSequence.fromString(this.prefix + name);
        GetResponse getResponse = this.client.getKVClient().get(key).join();
        List<KeyValue> kvs = getResponse.getKvs();
        if (kvs.isEmpty()) {
            throw new RuntimeException("Can NOT get config for <" + name + "> from etcd");
        }
        byte[] data = kvs.get(0).getValue().getBytes();
        return new ByteArrayInputStream(data);
    }

}
