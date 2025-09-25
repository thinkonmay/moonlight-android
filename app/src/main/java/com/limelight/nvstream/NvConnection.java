package com.limelight.nvstream;

import android.media.browse.MediaBrowser;

import com.limelight.LimeLog;
import com.limelight.nvstream.av.audio.AudioRenderer;
import com.limelight.nvstream.av.video.VideoDecoderRenderer;
import java.net.InetAddress;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.thibaultbee.srtdroid.core.enums.ErrorType;
import io.github.thibaultbee.srtdroid.core.enums.SockOpt;
import io.github.thibaultbee.srtdroid.core.enums.Transtype;
import io.github.thibaultbee.srtdroid.core.models.SrtSocket;

public class NvConnection implements SrtSocket.ClientListener {
    private static String url = "https://thinkmay.net/play/index.html?server=haiphong.thinkmay.net&audio=4676534b-9934-4bb1-808d-76ca209814d7&codec=h264&video=44a2f3a5-039f-4342-93c9-1c7b8146ff3b&vmid=ebbac54d-4b9e-4996-9f55-1e7369ffe49b&data=d7c36895-4b21-44ed-806b-919b249f6518&ref=huyhoangdo0205&demo=huyhoangdo0205";
    private boolean stopped = false;
    private String codec = "h264";
    private Thread videoThread,audioThread,hidThread,microphoneThread;
    private SrtSocket audioSocket,videoSocket,microphoneSocket;
    private NvWebsocket hidSocket;
    private VideoDecoderRenderer videoRenderer;
    private AudioRenderer audioRenderer;
    private NvConnectionListener listener;

    private static int VIDEO = 0;
    private static int AUDIO = 0;
    private static int HID = 0;
    private static int MICROPHONE = 0;
    public NvConnection(StreamConfiguration config) throws UnsupportedEncodingException, UnknownHostException, URISyntaxException {
        var params = NvConnection.getQueryParams(NvConnection.url);
        var server = params.get("server");
        var vmid = params.get("vmid");
        var audio = params.get("audio");
        var video = params.get("video");
        var codec = params.get("codec");
        var data = params.get("data");
        this.codec = codec;
        assert server != null;

        this.videoSocket = new SrtSocket();
        this.videoThread = this.createMediaThread(this,this.videoSocket,server,vmid,video,NvConnection.VIDEO);
        this.videoThread.start();

        this.audioSocket = new SrtSocket();
        this.audioThread = this.createMediaThread(this,this.audioSocket,server,vmid,audio,NvConnection.AUDIO);
        this.audioThread.start();

        this.hidSocket = new NvWebsocket(new URI("https://"+server+":444/broadcasters/websocket?vmid="+vmid+"&token="+data));
        this.hidThread = this.createDataThread(this,this.hidSocket,NvConnection.HID);
        this.hidThread.start();
    }

    private Thread createMediaThread(NvConnection conn, SrtSocket socket, String hostname, String vmid, String token, int type) {
        return new Thread() {
            public void run() {
                String inetAddr = null;
                try {
                    inetAddr = InetAddress.getByName(hostname).getHostAddress();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }

                assert inetAddr != null;
                socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE);
                socket.setSockFlag(SockOpt.STREAMID,vmid+":"+token+":1456");
                socket.setSockFlag(SockOpt.LATENCY,300);
                socket.connect(inetAddr,50006);

                var arr = new byte[2400];
                while (!conn.stopped) {
                    var size = socket.recv(arr,0,2400);
                    conn.onPacketRecv(type,Arrays.copyOf(arr,size));
                }
            }
        };
    }

    private Thread createDataThread(NvConnection conn,NvWebsocket client,int type) {
        return new Thread() {
            public void run() {
                var arr = new byte[2400];
                while (!conn.stopped) {
                    var size = client.recv(arr);
                    conn.onPacketRecv(type,Arrays.copyOf(arr,size));
                }
            }
        };
    }

    public void stop() {
        this.stopped = true;
        if (this.videoThread != null) {
            this.videoThread.interrupt();
            this.videoThread = null;
        }
    }


    private void onPacketRecv(int type, byte[] data) {
    }


    public void start(final AudioRenderer audioRenderer, final VideoDecoderRenderer videoDecoderRenderer, final NvConnectionListener connectionListener)
    {
        this.audioRenderer = audioRenderer;
        this.videoRenderer = videoDecoderRenderer;
        this.listener = connectionListener;
    }
    
    public void sendMouseMove(final short deltaX, final short deltaY)
    {
    }

    public void sendMousePosition(short x, short y, short referenceWidth, short referenceHeight)
    {
    }

    public void sendMouseMoveAsMousePosition(short deltaX, short deltaY, short referenceWidth, short referenceHeight)
    {
    }

    public void sendMouseButtonDown(final byte mouseButton)
    {
    }
    
    public void sendMouseButtonUp(final byte mouseButton)
    {
    }
    
    public void sendControllerInput(final short controllerNumber,
            final short activeGamepadMask, final int buttonFlags,
            final byte leftTrigger, final byte rightTrigger,
            final short leftStickX, final short leftStickY,
            final short rightStickX, final short rightStickY)
    {
    }

    public void sendKeyboardInput(final short keyMap, final byte keyDirection, final byte modifier, final byte flags) {
    }
    
    public void sendMouseScroll(final byte scrollClicks) {
    }

    public void sendMouseHScroll(final byte scrollClicks) {
    }

    public void sendMouseHighResScroll(final short scrollAmount) {
    }

    public void sendMouseHighResHScroll(final short scrollAmount) {
    }

    public int sendTouchEvent(byte eventType, int pointerId, float x, float y, float pressureOrDistance,
                              float contactAreaMajor, float contactAreaMinor, short rotation) {
        return 0;
    }

    public int sendPenEvent(byte eventType, byte toolType, byte penButtons, float x, float y,
                            float pressureOrDistance, float contactAreaMajor, float contactAreaMinor,
                            short rotation, byte tilt) {
        return 0;
    }

    public int sendControllerArrivalEvent(byte controllerNumber, short activeGamepadMask, byte type,
                                          int supportedButtonFlags, short capabilities) {
        return 0;
    }

    public int sendControllerTouchEvent(byte controllerNumber, byte eventType, int pointerId,
                                        float x, float y, float pressure) {
        return 0;
    }

    public int sendControllerMotionEvent(byte controllerNumber, byte motionType,
                                         float x, float y, float z) {
        return 0;
    }

    public void sendControllerBatteryEvent(byte controllerNumber, byte batteryState, byte batteryPercentage) {
    }

    public void sendUtf8Text(final String text) {
        return;
    }

    @Override
    public void onConnectionLost(@NotNull SrtSocket srtSocket, @NotNull ErrorType errorType, @NotNull InetSocketAddress inetSocketAddress, int i) {
        // TODO
    }

    private static Map<String, String> getQueryParams(String urlString) {
        Map<String, String> queryParams = new HashMap<>();
        try {
            URL url = new URL(urlString);
            String query = url.getQuery();

            if (query != null && !query.isEmpty()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) { // Ensure there's a key and a value
                        String key = pair.substring(0, idx);
                        String value = pair.substring(idx + 1);
                        queryParams.put(key, value);
                    } else if (idx == -1) { // Handle parameters without a value (e.g., "?key")
                        queryParams.put(pair, "");
                    }
                }
            }
        } catch (MalformedURLException e) {
            System.err.println("Invalid URL: " + e.getMessage());
            // Handle the exception appropriately, e.g., throw a custom exception
        }
        return queryParams;
    }
}
