package zjm.cst.dhu.opengl;

import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zjm on 2017/5/3.
 */

public class TriangleRenderer implements GLSurfaceView.Renderer {

    //x轴 y轴 z轴
    private float[] mTriangleArray = {
            0f, 1f, 0f,
            -1f, -1f, 0f,
            1f, -1f, 0f
    };

    //R G B A
    private float[] mColorArray = {
            1, 1, 0, 1,
            0, 1, 1, 1,
            1, 0, 1, 1
    };

    //三角形float缓冲
    private FloatBuffer mTriangleBuffer;
    //颜色float缓冲
    private FloatBuffer mColorBuffer;

    public TriangleRenderer() {
        //初始化byte缓冲 1float=4byte
        ByteBuffer mTriangleByteBuffer = ByteBuffer.allocateDirect(mTriangleArray.length * 4);
        //用本机字节顺序修改缓冲区字节顺序
        mTriangleByteBuffer.order(ByteOrder.nativeOrder());
        //转换为float缓冲
        mTriangleBuffer = mTriangleByteBuffer.asFloatBuffer();
        //将数组写入缓冲区
        mTriangleBuffer.put(mTriangleArray);
        //设置此缓冲区的位置。如果标记已定义并且大于新的位置，则要丢弃该标记。
        mTriangleBuffer.position(0);

        ByteBuffer mColorByteBuffer = ByteBuffer.allocateDirect(mColorArray.length * 4);
        mColorByteBuffer.order(ByteOrder.nativeOrder());
        mColorBuffer = mColorByteBuffer.asFloatBuffer();
        mColorBuffer.put(mColorArray);
        mColorBuffer.position(0);
    }

    //调用一次，用来配置View的OpenGL ES环境
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //白色清屏
        gl.glClearColor(1, 1, 1, 1);
    }

    //如果View的几何形态发生变化时会被调用，例如当设备的屏幕方向发生改变时。
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //计算宽高比
        float ratio = (float) width / height;
        //设置OpenGL场景的大小,(0,0)表示窗口内部视口的左下角，(w,h)指定了视口的大小
        gl.glViewport(0, 0, width, height);
        //设置投影变换模式
        gl.glMatrixMode(GL10.GL_PROJECTION);
        //初始化为单位矩阵
        gl.glLoadIdentity();
        //设置投影映射关系
        //glFrustumf (float left, float right, float bottom, float top, float zNear, float zFar)
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    //每次重新绘制View时被调用。
    @Override
    public void onDrawFrame(GL10 gl) {
        //清除颜色及深度缓存
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        //设置模型变换模式
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        //初始化为单位矩阵
        gl.glLoadIdentity();
        //允许设置顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //允许设置颜色数组
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        //z轴移动-2f
        gl.glTranslatef(0f, 0.0f, -2.0f);

        //设置顶点数组指针
        //void glVertexPointer(int size,int type,int stride,Buffer pointer)
        //size: 每个顶点用几个数值描述。必须是2，3 ，4 之一。
        //type: 数组中每个顶点的坐标类型。取值：GL_BYTE,GL_SHORT, GL_FIXED, GL_FLOAT。
        //stride：数组中每个顶点间的间隔，步长（字节位移）。取值若为0，表示数组是连续的
        //pointer：即存储顶点的Buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mTriangleBuffer);
        //设置颜色数组指针
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

        //绘制数组里面所有点构成的各个三角片
        //void glDrawArrays(int mode,int first,int count)
        //mode：有三种取值
        // GL_TRIANGLES：每三个顶点之间绘制三角形，之间不连接
        // GL_TRIANGLE_FAN：以V0 V1 V2,V0 V2 V3,V0 V3 V4，……的形式绘制三角形
        // GL_TRIANGLE_STRIP：顺序在每三个顶点之间均绘制三角形。这个方法可以保证从相同的方向上所有三角形均被绘制。以V0 V1 V2 ,V1 V2 V3,V2 V3 V4,……的形式绘制三角形
        // first：从数组缓存中的哪一位开始绘制，一般都定义为0
        // count：顶点的数量
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

        //取消颜色设置
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        //取消顶点设置
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        //绘制结束
        gl.glFinish();
    }
}
