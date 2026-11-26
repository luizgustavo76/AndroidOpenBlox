package com.openblox.android;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends Activity {

    static {
        System.loadLibrary("openblox");
    }

    public native void nativeInit(String baseDir, String mapFileName);
    public native void nativeResize(int w, int h);
    public native void nativeStep();
    public native void nativeSetKey(int keyId, int pressed);
    public native void nativeRotateCamera(float dx, float dy);

    private GLSurfaceView glView;
    private float lastTouchX, lastTouchY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        copyAssetToFiles("test.rbxl");
        new File(getFilesDir(), "content/fonts").mkdirs();
        copyAssetToFiles("content/fonts/character.rbxm");

        FrameLayout root = new FrameLayout(this);

        glView = new GLSurfaceView(this);
        glView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                nativeInit(getFilesDir().getAbsolutePath(), "test.rbxl");
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                nativeResize(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                nativeStep();
            }
        });

        // Arrastar o dedo na tela 3D gira a câmera
        glView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    final float dx = x - lastTouchX;
                    final float dy = y - lastTouchY;
                    glView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            nativeRotateCamera(dx, dy);
                        }
                    });
                }
                lastTouchX = x;
                lastTouchY = y;
                return true;
            }
        });

        root.addView(glView);
        root.addView(createLeftDPad());
        root.addView(createRightVerticalControls());
        setContentView(root);
    }

    private LinearLayout createLeftDPad() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM | Gravity.LEFT
        );
        params.setMargins(24, 0, 0, 24);
        container.setLayoutParams(params);

        // Linha de cima: W
        container.addView(makeKeyButton("▲", 0));

        // Linha do meio: A e D
        LinearLayout middle = new LinearLayout(this);
        middle.setOrientation(LinearLayout.HORIZONTAL);
        middle.addView(makeKeyButton("◄", 2));
        middle.addView(makeKeyButton("►", 3));
        container.addView(middle);

        // Linha de baixo: S
        container.addView(makeKeyButton("▼", 1));

        return container;
    }

    private LinearLayout createRightVerticalControls() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM | Gravity.RIGHT
        );
        params.setMargins(0, 0, 24, 24);
        container.setLayoutParams(params);

        container.addView(makeKeyButton("UP", 4));
        container.addView(makeKeyButton("DN", 5));

        return container;
    }

    private Button makeKeyButton(String label, final int keyId) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.argb(110, 40, 40, 40));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(96, 96);
        lp.setMargins(4, 4, 4, 4);
        btn.setLayoutParams(lp);

        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int pressed;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    pressed = 1;
                    v.setBackgroundColor(Color.argb(180, 90, 90, 90));
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    pressed = 0;
                    v.setBackgroundColor(Color.argb(110, 40, 40, 40));
                } else {
                    return false;
                }
                glView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        nativeSetKey(keyId, pressed);
                    }
                });
                return true;
            }
        });
        return btn;
    }

    private void copyAssetToFiles(String assetName) {
        try {
            File outFile = new File(getFilesDir(), assetName);
            InputStream in = getAssets().open(assetName);
            FileOutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
