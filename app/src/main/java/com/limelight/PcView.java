package com.limelight;

import com.limelight.preferences.GlPreferences;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PcView extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
}
