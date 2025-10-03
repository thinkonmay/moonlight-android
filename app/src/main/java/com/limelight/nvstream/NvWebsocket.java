package com.limelight.nvstream;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This example demonstrates how to create a websocket connection to a server. Only the most
 * important callbacks are overloaded.
 */
public class NvWebsocket extends WebSocketClient {
    private Queue<byte[]> queue = new ConcurrentLinkedQueue<byte[]>();
    public NvWebsocket(String serverURI) throws URISyntaxException {
        super(new URI(serverURI));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send("Hello, it is me. Mario :)");
        System.out.println("opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {
        this.queue.add(message.getBytes());
    }

    public int recv(byte[] buffer) {
        while (true) {
            var res = this.queue.poll();
            if (res == null) {
                try {
                    Thread.sleep(10);
                    continue;
                } catch (InterruptedException e) {
                    return -1;
                }
            }

            System.arraycopy(res,0,buffer,0,res.length);
            return res.length;
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The close codes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }
}