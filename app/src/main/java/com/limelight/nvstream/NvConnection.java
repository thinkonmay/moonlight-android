package com.limelight.nvstream;

import com.limelight.nvstream.av.audio.AudioRenderer;
import com.limelight.nvstream.av.video.VideoDecoderRenderer;

import org.jetbrains.annotations.NotNull;
import java.net.InetSocketAddress;
import java.util.Arrays;

import io.github.thibaultbee.srtdroid.core.enums.ErrorType;
import io.github.thibaultbee.srtdroid.core.enums.SockOpt;
import io.github.thibaultbee.srtdroid.core.enums.Transtype;
import io.github.thibaultbee.srtdroid.core.models.SrtSocket;

public class NvConnection implements SrtSocket.ClientListener {
    private boolean stopped = false;
    private Thread recvThread;
    public NvConnection(StreamConfiguration config)
    {
//        var socket = new SrtSocket();
//        socket.connect("127.0.0.1",50000);
//        // TODO java port of kotlin's assert true
//        socket.setSockFlag(SockOpt.TRANSTYPE, Transtype.LIVE);
//        socket.setClientListener(this);
//        recvThread = this.createRecvThread(this,socket);
//        recvThread.start();
    }

    private Thread createRecvThread(NvConnection conn,SrtSocket socket ) {
        return new Thread() {
            public void run() {
                while (!conn.stopped) {
                    var arr = new byte[2400];
                    var size = socket.recv(arr,0,2400);
                    conn.onPacketRecv(Arrays.copyOf(arr,size));
                }
            }
        };
    }

    public void stop() {
        this.stopped = true;
        if (this.recvThread != null) {
            this.recvThread.interrupt();
            this.recvThread = null;
        }
    }


    private void onPacketRecv(byte[] data) {
    }
    
    public void start(final AudioRenderer audioRenderer, final VideoDecoderRenderer videoDecoderRenderer, final NvConnectionListener connectionListener)
    {
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
}
