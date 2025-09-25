package com.limelight;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;

import com.limelight.binding.PlatformBinding;
import com.limelight.binding.crypto.AndroidCryptoProvider;
import com.limelight.computers.ComputerManagerListener;
import com.limelight.computers.ComputerManagerService;
import com.limelight.grid.PcGridAdapter;
import com.limelight.grid.assets.DiskAssetLoader;
import com.limelight.nvstream.http.ComputerDetails;
import com.limelight.nvstream.http.NvApp;
import com.limelight.nvstream.http.NvHTTP;
import com.limelight.nvstream.http.PairingManager;
import com.limelight.nvstream.http.PairingManager.PairState;
import com.limelight.nvstream.wol.WakeOnLanSender;
import com.limelight.preferences.GlPreferences;
import com.limelight.preferences.PreferenceConfiguration;
import com.limelight.preferences.StreamSettings;
import com.limelight.ui.AdapterFragment;
import com.limelight.ui.AdapterFragmentCallbacks;
import com.limelight.utils.Dialog;
import com.limelight.utils.HelpLauncher;
import com.limelight.utils.ServerHelper;
import com.limelight.utils.ShortcutHelper;
import com.limelight.utils.UiHelper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import org.xmlpull.v1.XmlPullParserException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PcView extends Activity {
    private boolean freezeUpdates, runningPolling, inForeground, completeOnCreateCalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Assume we're in the foreground when created to avoid a race
        // between binding to CMS and onResume()
        inForeground = true;

        // Create a GLSurfaceView to fetch GLRenderer unless we have
        // a cached result already.
        final GlPreferences glPrefs = GlPreferences.readPreferences(this);
        if (!glPrefs.savedFingerprint.equals(Build.FINGERPRINT) || glPrefs.glRenderer.isEmpty()) {
            GLSurfaceView surfaceView = new GLSurfaceView(this);
            surfaceView.setRenderer(new GLSurfaceView.Renderer() {
                @Override
                public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
                    // Save the GLRenderer string so we don't need to do this next time
                    glPrefs.glRenderer = gl10.glGetString(GL10.GL_RENDERER);
                    glPrefs.savedFingerprint = Build.FINGERPRINT;
                    glPrefs.writePreferences();

                    LimeLog.info("Fetched GL Renderer: " + glPrefs.glRenderer);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }

                @Override
                public void onSurfaceChanged(GL10 gl10, int i, int i1) {
                }

                @Override
                public void onDrawFrame(GL10 gl10) {
                }
            });
            setContentView(surfaceView);
        }
        else {
            LimeLog.info("Cached GL Renderer: " + glPrefs.glRenderer);
            completeOnCreateCalled = true;
            Intent intent = new Intent(PcView.this, Game.class);
            intent.putExtra(Game.EXTRA_HOST, "");
            intent.putExtra(Game.EXTRA_PORT, 0);
            intent.putExtra(Game.EXTRA_HTTPS_PORT, 0);
            intent.putExtra(Game.EXTRA_APP_NAME, "");
            intent.putExtra(Game.EXTRA_APP_ID, 0);
            intent.putExtra(Game.EXTRA_APP_HDR, true);
            intent.putExtra(Game.EXTRA_UNIQUEID, "");
            intent.putExtra(Game.EXTRA_PC_UUID, "");
            intent.putExtra(Game.EXTRA_PC_NAME, "");
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        inForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        inForeground = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public static class ComputerObject {
        public ComputerDetails details;

        public ComputerObject(ComputerDetails details) {
            if (details == null) {
                throw new IllegalArgumentException("details must not be null");
            }
            this.details = details;
        }

        @Override
        public String toString() {
            return details.name;
        }
    }
}
