package com.example.dsd20252.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.dsd20252.model.BuyRequest;
import com.example.dsd20252.model.BuyResponse;
import com.example.dsd20252.model.Chunk;
import com.example.dsd20252.model.SearchRequest;
import com.example.dsd20252.model.Store;
import com.example.dsd20252.ui.SettingsActivity;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class NetworkClient {
    private final String masterHost;
    private final int    masterPort;

    private static final String DEFAULT_HOST = "10.0.2.2";
    private static final int    DEFAULT_PORT = 5555;

    /** Default: use defaults */
    public NetworkClient() {
        this.masterHost = DEFAULT_HOST;
        this.masterPort = DEFAULT_PORT;
    }

    /** Read host/port from Settings */
    public NetworkClient(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(
                SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE
        );
        this.masterHost = p.getString(SettingsActivity.KEY_HOST, DEFAULT_HOST);
        this.masterPort = p.getInt(SettingsActivity.KEY_PORT, DEFAULT_PORT);
    }

    @SuppressWarnings("unchecked")
    public List<Store> searchStores(SearchRequest req) throws Exception {
        try (Socket sock = new Socket()) {
            sock.connect(new InetSocketAddress(masterHost, masterPort));
            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(sock.getInputStream());

            // 10 = SEARCH, 20 = BUY (as per your Chunk.java)
            Chunk request = new Chunk("10", 0, req);
            out.writeObject(request);
            out.flush();

            Chunk response = (Chunk) in.readObject();
            return (List<Store>) response.getData();
        }
    }

    public BuyResponse buyProducts(BuyRequest req) throws Exception {
        try (Socket sock = new Socket()) {
            sock.connect(new InetSocketAddress(masterHost, masterPort));
            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(sock.getInputStream());

            Chunk request = new Chunk("20", 0, req);
            out.writeObject(request);
            out.flush();

            Chunk response = (Chunk) in.readObject();
            return (BuyResponse) response.getData();
        }
    }
}
