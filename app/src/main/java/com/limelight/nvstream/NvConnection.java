package com.limelight.nvstream;

import com.limelight.LimeLog;
import com.limelight.nvstream.av.audio.AudioRenderer;
import com.limelight.nvstream.av.video.VideoDecoderRenderer;
import com.limelight.nvstream.jni.MoonBridge;
import com.limelight.utils.BitReader;

import java.net.InetAddress;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.thibaultbee.srtdroid.core.enums.ErrorType;
import io.github.thibaultbee.srtdroid.core.enums.SockOpt;
import io.github.thibaultbee.srtdroid.core.enums.Transtype;
import io.github.thibaultbee.srtdroid.core.models.SrtSocket;

public class NvConnection implements SrtSocket.ClientListener {
    private static String url = "http://localhost:3000/play/?server=dev.thinkmay.net&audio=ddf6af86-4596-4018-91e7-6fb74cd7c36e&codec=h265&video=ea375efc-4dbf-4953-b143-eecce083b0f5&vmid=dee2a363-5b98-43e7-a334-0657df174af1&data=61434e07-29d1-4ed3-9845-2849c2c3142c";
    private boolean stopped = false;
    private Thread videoThread,audioThread,hidThread,microphoneThread,connectionThread;
    private SrtSocket audioSocket,videoSocket,microphoneSocket;
    private NvWebsocket hidSocket;
    private VideoDecoderRenderer videoRenderer;
    private AudioRenderer audioRenderer;
    private NvConnectionListener listener;

    private static int VIDEO = 0;
    private static int AUDIO = 1;
    private static int HID = 2;
    private static int MICROPHONE = 3;


    class NaluReceiveContext {
        public NaluReceiveContext() {
            this.buffer = new byte[]{};
            this.fullfilled = 0;
            this.total = 0;
        }
        byte[] buffer;
        long fullfilled;
        long total;
        long frameIndex;
        short naluIndex;
        boolean isIDR;
        boolean isRFI;
        boolean isPPS;
        boolean isSPS;
        boolean isVPS;

        long startTime;
        long finishTime;
    }


    public NvConnection(StreamConfiguration config) throws URISyntaxException {
        var params = NvConnection.getQueryParams(NvConnection.url);
        var server = params.get("server");
        var vmid = params.get("vmid");
        var audio = params.get("audio");
        var video = params.get("video");
        var codec = params.get("codec");
        var data = params.get("data");
        assert server != null;

        this.videoSocket = new SrtSocket();
        this.videoThread = this.createMediaThread(this,
                this.videoSocket,
                server,
                vmid,
                video,
                codec,
                NvConnection.VIDEO);

        this.audioSocket = new SrtSocket();
        this.audioThread = this.createMediaThread(this,
                this.audioSocket,
                server,
                vmid,
                audio,
                "opus",
                NvConnection.AUDIO);

        this.hidSocket = new NvWebsocket("wss://"+server+":444/broadcasters/websocket?vmid="+vmid+"&token="+data);
        this.hidThread = this.createDataThread(this,this.hidSocket,NvConnection.HID);
    }


    @Override
    public void onConnectionLost(@NotNull SrtSocket srtSocket, @NotNull ErrorType errorType, @NotNull InetSocketAddress inetSocketAddress, int i) {
        if (listener != null) {
            listener.connectionTerminated(404);
        }
    }

    private Thread createMediaThread(NvConnection conn, SrtSocket socket, String hostname, String vmid, String token, String codec, int type) {
        return new Thread() {
            public void run() {
                String inetAddr = null;
                try {
                    inetAddr = InetAddress.getByName(hostname).getHostAddress();

                    assert inetAddr != null;
                    socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE);
                    socket.setSockFlag(SockOpt.STREAMID,vmid+":"+token+":1456:"+codec);
                    socket.setSockFlag(SockOpt.LATENCY,300);
                    socket.setSockFlag(SockOpt.CONNTIMEO,10000);
                    socket.connect(inetAddr,50006);

                    if (listener != null) {
                        listener.connectionStatusUpdate(200 + type);
                    }

                    var arr = new byte[2400];
                    var ctx = new NaluReceiveContext();
                    while (!conn.stopped) {
                        var size = socket.recv(arr,0,2400);
                        conn.onFragmentRecv(type,ctx,Arrays.copyOf(arr,size));
                    }
                } catch (Exception e) {
                    LimeLog.warning("thread " +type+ " got exception " + e);
                    if (listener != null) {
                        listener.connectionTerminated(404);
                    }
                }
            }
        };
    }

    private Thread createDataThread(NvConnection conn,NvWebsocket client,int type) {
        return new Thread() {
            public void run() {
                try {
                    var arr = new byte[2400];
                    while (!conn.stopped) {
                        var size = client.recv(arr);
                        conn.onHIDPacketRecv(Arrays.copyOf(arr,size));
                    }
                } catch (Exception e) {
                    LimeLog.warning("thread " +type+ " got exception " + e);
                    if (listener != null) {
                        listener.connectionTerminated(404);
                    }
                }
            }
        };
    }

    public void stop() {
        this.stopped = true;
        if (this.hidSocket != null) {
            this.hidSocket.close();
        }
        if (this.audioSocket != null) {
            this.audioSocket.close();
        }
        if (this.videoSocket != null) {
            this.videoSocket.close();
        }
        if (this.videoThread != null) {
            this.videoThread.interrupt();
            this.videoThread = null;
        }
        if (this.audioThread != null) {
            this.audioThread.interrupt();
            this.audioThread= null;
        }
        if (this.hidThread != null) {
            this.hidThread.interrupt();
            this.hidThread= null;
        }
    }


    private void onFragmentRecv(int type, NaluReceiveContext ctx, byte[] data) {
        if (type == NvConnection.AUDIO) { // TODO
            return;
        }
        if (data.length < 24) {
            return;
        }

        try {

            var reader = new BitReader(data);
            var index = reader.readUint64LE();
            var timestamp = reader.readUint64LE();

            var naluIndex = reader.readByte();
            var finalFlags = reader.readByte();
            var naluLength = reader.readUint24LE();

            var packetEntry = reader.readUint24LE();
            var rest = reader.left();

            ctx.isIDR = (finalFlags & (1 << 0)) > 0;
            ctx.isRFI = (finalFlags & (1 << 1)) > 0;
            ctx.isSPS = (finalFlags & (1 << 2)) > 0;
            ctx.isPPS = (finalFlags & (1 << 3)) > 0;
            ctx.isVPS = (finalFlags & (1 << 4)) > 0;
            ctx.total = naluLength;
            if (ctx.buffer.length == 0) {
                ctx.buffer = new byte[(int)ctx.total];
                ctx.startTime = System.currentTimeMillis();
            }
            System.arraycopy(rest,0, ctx.buffer,(int)packetEntry,rest.length);
            ctx.fullfilled += rest.length;

            if (ctx.fullfilled == ctx.total) {
                ctx.finishTime = System.currentTimeMillis();
                this.onVideoPacketRecv(index,timestamp,ctx,ctx.buffer);
                ctx.buffer = new byte[]{};
                ctx.total = 0;
                ctx.fullfilled = 0;
            }
        } catch (Exception e) {
            LimeLog.warning("failed to depacketize "+e.getMessage());
        }

    }

    private void onVideoPacketRecv(long index, long timestamp, NaluReceiveContext ctx, byte[] buffer) {
        if (this.videoRenderer == null) {
            return;
        }

        try {
            this.videoRenderer.submitDecodeUnit(
                    buffer,buffer.length,
                    ctx.isPPS   ? MoonBridge.BUFFER_TYPE_PPS
                    : ctx.isSPS ? MoonBridge.BUFFER_TYPE_SPS
                    : ctx.isVPS ? MoonBridge.BUFFER_TYPE_VPS
                    : 0,
                    (int)index,
                    ctx.isIDR ? MoonBridge.FRAME_TYPE_IDR : MoonBridge.FRAME_TYPE_PFRAME,
                    (char)0,ctx.startTime,ctx.finishTime);
        } catch (Exception e) {
            LimeLog.warning("decode exception "+e.getMessage());
        }
    }

    private void onAudioPacketRecv(long index, long timestamp, byte[] buffer) {
    }
    private void onHIDPacketRecv(byte[] buffer) {
    }


    public void start(final AudioRenderer AudioRenderer, final VideoDecoderRenderer videoDecoderRenderer, final NvConnectionListener connectionListener)
    {
        this.connectionThread = new Thread(new Runnable() {
            public void run() {
                audioRenderer = AudioRenderer;
                videoRenderer = videoDecoderRenderer;
                listener = connectionListener;

                listener.connectionStarted();

                videoRenderer.setup(MoonBridge.VIDEO_FORMAT_H265,1920,1080,120);
                videoRenderer.start();


                hidSocket.connect();
                audioThread.start();
                videoThread.start();
                hidThread.start();
            }
        });

        this.connectionThread.start();
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
