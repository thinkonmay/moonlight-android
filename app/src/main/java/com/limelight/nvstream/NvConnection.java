package com.limelight.nvstream;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.IpPrefix;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.xmlpull.v1.XmlPullParserException;

import com.limelight.LimeLog;
import com.limelight.nvstream.av.audio.AudioRenderer;
import com.limelight.nvstream.av.video.VideoDecoderRenderer;
import com.limelight.nvstream.http.ComputerDetails;
import com.limelight.nvstream.http.HostHttpResponseException;
import com.limelight.nvstream.http.LimelightCryptoProvider;
import com.limelight.nvstream.http.NvApp;
import com.limelight.nvstream.http.NvHTTP;
import com.limelight.nvstream.http.PairingManager;
import com.limelight.nvstream.input.MouseButtonPacket;
import com.limelight.nvstream.jni.MoonBridge;

public class NvConnection {
    // Context parameters
    private final boolean isMonkey;

    public NvConnection(StreamConfiguration config)
    {
        this.isMonkey = true;
    }
    

    public void stop() {
    }

    
    public void start(final AudioRenderer audioRenderer, final VideoDecoderRenderer videoDecoderRenderer, final NvConnectionListener connectionListener)
    {
    }
    
    public void sendMouseMove(final short deltaX, final short deltaY)
    {
        if (!isMonkey) {
            MoonBridge.sendMouseMove(deltaX, deltaY);
        }
    }

    public void sendMousePosition(short x, short y, short referenceWidth, short referenceHeight)
    {
        if (!isMonkey) {
            MoonBridge.sendMousePosition(x, y, referenceWidth, referenceHeight);
        }
    }

    public void sendMouseMoveAsMousePosition(short deltaX, short deltaY, short referenceWidth, short referenceHeight)
    {
        if (!isMonkey) {
            MoonBridge.sendMouseMoveAsMousePosition(deltaX, deltaY, referenceWidth, referenceHeight);
        }
    }

    public void sendMouseButtonDown(final byte mouseButton)
    {
        if (!isMonkey) {
            MoonBridge.sendMouseButton(MouseButtonPacket.PRESS_EVENT, mouseButton);
        }
    }
    
    public void sendMouseButtonUp(final byte mouseButton)
    {
        if (!isMonkey) {
            MoonBridge.sendMouseButton(MouseButtonPacket.RELEASE_EVENT, mouseButton);
        }
    }
    
    public void sendControllerInput(final short controllerNumber,
            final short activeGamepadMask, final int buttonFlags,
            final byte leftTrigger, final byte rightTrigger,
            final short leftStickX, final short leftStickY,
            final short rightStickX, final short rightStickY)
    {
        if (!isMonkey) {
            MoonBridge.sendMultiControllerInput(controllerNumber, activeGamepadMask, buttonFlags,
                    leftTrigger, rightTrigger, leftStickX, leftStickY, rightStickX, rightStickY);
        }
    }

    public void sendKeyboardInput(final short keyMap, final byte keyDirection, final byte modifier, final byte flags) {
        if (!isMonkey) {
            MoonBridge.sendKeyboardInput(keyMap, keyDirection, modifier, flags);
        }
    }
    
    public void sendMouseScroll(final byte scrollClicks) {
        if (!isMonkey) {
            MoonBridge.sendMouseHighResScroll((short)(scrollClicks * 120)); // WHEEL_DELTA
        }
    }

    public void sendMouseHScroll(final byte scrollClicks) {
        if (!isMonkey) {
            MoonBridge.sendMouseHighResHScroll((short)(scrollClicks * 120)); // WHEEL_DELTA
        }
    }

    public void sendMouseHighResScroll(final short scrollAmount) {
        if (!isMonkey) {
            MoonBridge.sendMouseHighResScroll(scrollAmount);
        }
    }

    public void sendMouseHighResHScroll(final short scrollAmount) {
        if (!isMonkey) {
            MoonBridge.sendMouseHighResHScroll(scrollAmount);
        }
    }

    public int sendTouchEvent(byte eventType, int pointerId, float x, float y, float pressureOrDistance,
                              float contactAreaMajor, float contactAreaMinor, short rotation) {
        if (!isMonkey) {
            return MoonBridge.sendTouchEvent(eventType, pointerId, x, y, pressureOrDistance,
                    contactAreaMajor, contactAreaMinor, rotation);
        }
        else {
            return MoonBridge.LI_ERR_UNSUPPORTED;
        }
    }

    public int sendPenEvent(byte eventType, byte toolType, byte penButtons, float x, float y,
                            float pressureOrDistance, float contactAreaMajor, float contactAreaMinor,
                            short rotation, byte tilt) {
        if (!isMonkey) {
            return MoonBridge.sendPenEvent(eventType, toolType, penButtons, x, y, pressureOrDistance,
                    contactAreaMajor, contactAreaMinor, rotation, tilt);
        }
        else {
            return MoonBridge.LI_ERR_UNSUPPORTED;
        }
    }

    public int sendControllerArrivalEvent(byte controllerNumber, short activeGamepadMask, byte type,
                                          int supportedButtonFlags, short capabilities) {
        return MoonBridge.sendControllerArrivalEvent(controllerNumber, activeGamepadMask, type, supportedButtonFlags, capabilities);
    }

    public int sendControllerTouchEvent(byte controllerNumber, byte eventType, int pointerId,
                                        float x, float y, float pressure) {
        if (!isMonkey) {
            return MoonBridge.sendControllerTouchEvent(controllerNumber, eventType, pointerId, x, y, pressure);
        }
        else {
            return MoonBridge.LI_ERR_UNSUPPORTED;
        }
    }

    public int sendControllerMotionEvent(byte controllerNumber, byte motionType,
                                         float x, float y, float z) {
        if (!isMonkey) {
            return MoonBridge.sendControllerMotionEvent(controllerNumber, motionType, x, y, z);
        }
        else {
            return MoonBridge.LI_ERR_UNSUPPORTED;
        }
    }

    public void sendControllerBatteryEvent(byte controllerNumber, byte batteryState, byte batteryPercentage) {
        MoonBridge.sendControllerBatteryEvent(controllerNumber, batteryState, batteryPercentage);
    }

    public void sendUtf8Text(final String text) {
        if (!isMonkey) {
            MoonBridge.sendUtf8Text(text);
        }
    }

    public static String findExternalAddressForMdns(String stunHostname, int stunPort) {
        return MoonBridge.findExternalAddressIP4(stunHostname, stunPort);
    }
}
