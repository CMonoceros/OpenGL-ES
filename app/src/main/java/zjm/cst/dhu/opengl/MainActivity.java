package zjm.cst.dhu.opengl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.SeekBar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zjm on 2017/5/3.
 */

public class MainActivity extends Activity {

    private GLSurfaceView myGLSurfaceView;
    private ModelRenderer myModelRenderer;
    private TriangleRenderer myTriangleRenderer;
    private SeekBar mySeekBar;
    private float romateDegree = 0;
    private boolean isRun = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            myModelRenderer.setDegree(msg.getData().getFloat("degree"));
            myGLSurfaceView.requestRender();
        }
    };

    private Thread thread = new Thread() {
        @Override
        public void run() {
            while (isRun) {
                try {
                    sleep(100);
                    romateDegree += 5;
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putFloat("degree", romateDegree);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySeekBar = (SeekBar) findViewById(R.id.SeekBar);
        mySeekBar.setMax(100);

        if (checkSupported()) {
            myGLSurfaceView = (GLSurfaceView) findViewById(R.id.GlSurfaceView);
            myModelRenderer = new ModelRenderer(this, "godness", 6);
            myGLSurfaceView.setRenderer(myModelRenderer);
            //设置渲染模式
            //RENDERMODE_CONTINUOUSLY连续不断画 适合动画
            //RENDERMODE_WHEN_DIRTY需要重画的时候 画
            myGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            //请求渲染
            myGLSurfaceView.requestRender();

            mySeekBar.setProgress((int) (myModelRenderer.getScale() * 100));
            mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    myModelRenderer.setScale(1f * progress / 100);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myGLSurfaceView != null) {
            mySeekBar.setProgress((int) (myModelRenderer.getScale() * 100));
            myGLSurfaceView.onResume();
            isRun = true;
            thread.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myGLSurfaceView != null) {
            myGLSurfaceView.onPause();
            isRun = false;
        }
    }

    private boolean checkSupported() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x2000;

        boolean isEmulator = Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"));

        return supportsEs2 || isEmulator;
    }
}
