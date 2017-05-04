package zjm.cst.dhu.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zjm on 2017/5/4.
 */

public class ModelRenderer implements GLSurfaceView.Renderer {

    private List<Model> models = new ArrayList<>();
    private Model model;

    private Point eyePoint = new Point(0, 0, -3);
    private Point centerPoint = new Point(0, 0, 0);
    private Point upPoint = new Point(0, -1, 0);

    private float[] lightAmb = {0.9f, 0.9f, 0.9f, 1.0f,};
    private float[] lightDiff = {0.5f, 0.5f, 0.5f, 1.0f,};
    private float[] lightSpec = {1.0f, 1.0f, 1.0f, 1.0f,};
    private float[] lightPosition = {0.5f, 0.5f, 0.5f, 0.0f,};

    private float[] materialAmb = {0.4f, 0.4f, 1.0f, 1.0f};
    private float[] materialDiff = {0.0f, 0.0f, 1.0f, 1.0f};
    private float[] materialSpec = {1.0f, 0.5f, 0.0f, 1.0f};

    private float scale = 1;
    private float degree = 0;
    private Point modelCenterPoint;

    private Context context = null;

    public ModelRenderer(Context context, String name) {
        this.context = context;
        try {
            model = new STLReader().parseStlByAssets(context, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //r是半径，不是直径，因此用0.5/r可以算出放缩比例
        scale = 0.5f / Utils.getRadius(models);
        modelCenterPoint = Utils.getCenterPoint(models);
    }

    public ModelRenderer(String path) {
        try {
            model = new STLReader().parseStlByPath(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        scale = 0.5f / Utils.getRadius(models);
        modelCenterPoint = Utils.getCenterPoint(models);
    }

    public ModelRenderer(Context context, String name, int counts) {
        this.context = context;
        for (int i = 1; i <= counts; i++) {
            try {
                models.add(new STLReader().parseStlWithPxyByAssets(context, name + "/" + i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        scale = 0.5f / Utils.getRadius(models);
        modelCenterPoint = Utils.getCenterPoint(models);
    }

    public ModelRenderer(String path, int counts) {
        for (int i = 1; i <= counts; i++) {
            try {
                models.add(new STLReader().parseStlByPath(path + "/" + i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        scale = 0.5f / Utils.getRadius(models);
        modelCenterPoint = Utils.getCenterPoint(models);
    }

    public void setDegree(float degree) {
        this.degree = degree;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //开启深度测试
        gl.glEnable(GL10.GL_DEPTH_TEST);

        //给深度缓存设定默认值
        //一个模型深度值取值和范围为[0,1]
        gl.glClearDepthf(1.0f);

        //比较深度缓存以决定是否绘制
        //GL10.GL_NEVER：永不绘制
        // GL10.GL_LESS：只绘制模型中像素点的z值<默认设定像素z值的部分
        // GL10.GL_EQUAL：只绘制模型中像素点的z值=默认设定像素z值的部分
        // GL10.GL_LEQUAL：只绘制模型中像素点的z值<=默认设定像素z值的部分
        // GL10.GL_GREATER ：只绘制模型中像素点的z值>默认设定像素z值的部分
        // GL10.GL_NOTEQUAL：只绘制模型中像素点的z值!=默认设定像素z值的部分
        // GL10.GL_GEQUAL：只绘制模型中像素点的z值>=默认设定像素z值的部分
        // GL10.GL_ALWAYS：总是绘制
        gl.glDepthFunc(GL10.GL_LEQUAL);

        //设置着色器模式
        //GL_SMOOTH：根据顶点的不同颜色，最终以渐变的形式填充图形。
        // GL_FLAT：假设有n个三角片，则取最后n个顶点的颜色填充着n个三角片。
        gl.glShadeModel(GL10.GL_SMOOTH);

        //启用光照功能
//        gl.glEnable(GL10.GL_LIGHTING);
        //开启0号灯，默认颜色为白色，漫反射和镜面反射也为白色
//        gl.glEnable(GL10.GL_LIGHT0);

        //void glLightfv(int light,int pname, FloatBuffer params)
        //light: 指光源的序号，OpenGL ES可以设置从0到7共八个光源。
        // pname: 光源参数名称，可以有如下：
        // GL_SPOT_DIRECTION 用于设置聚光灯位置
        // GL_SPOT_EXPONENT 用于设置聚光灯聚光程度，为0表示各个方向光照强度相同，数值越大，聚光越强
        // GL_SPOT_CUTOFF 用于设置聚光角度，它是光源发射光线所覆盖角度的一半，其取值范围在0到90之间，取180度表示，不适用聚光灯
        // GL_CONSTANT_ATTENUATION 用于设置常衰减(与距离无关)
        // GL_LINEAR_ATTENUATION 用于设置距离线性衰减
        // GL_QUADRATIC_ATTENUATION 用于设置距离以二次函数衰减
        // GL_AMBIENT 用于设置环境光颜色
        // GL_DIFFUSE 用于设置漫反射光颜色
        // GL_SPECULAR 用于设置镜面反射光颜色
        // GL_POSITION 用于设置光源位置
        // params: 参数的值（数组或是Buffer类型） 数组里面
        // 颜色的值为(R,G,B,A)，位置的值为(x,y,z,w)
        // 衰减因子的值为  1 / (k1 + k2 * d + k3 * k3 * d)
        // k1,k2,k3分别对应常，线性，二次衰减参数
//        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, Utils.floatToBuffer(lightAmb));
//        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, Utils.floatToBuffer(lightDiff));
//        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, Utils.floatToBuffer(lightSpec));
//        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, Utils.floatToBuffer(lightPosition));

        //void glMaterialfv(int face,int pname,FloatBuffer params)
        // face : 在OpenGL ES中只能使用GL_FRONT_AND_BACK，表示修改物体的前面和后面的材质光线属性
        // pname: 参数类型，这些参数用在光照方程。可以取如下值：
        // GL_AMBIENT 用于设置对环境光反射率
        // GL_DIFFUSE 用于设置对漫反射光反射率
        // GL_SPECULAR 用于设置对镜面反射光反射率
        // GL_SHININESS 用于设置镜面指数，取值范围是0到128
        // 该值小，表示材质粗糙，点光源照射到上面，也可产生大亮点
        // 该值大，表示材质越类似镜面，光源照射到上面，产生小的亮点
        // GL_EMISSION 用于设置一种颜色，认为该材质本身就微微的向外发射光线，眼睛感觉到它有这样的颜色，但较弱，不会影响其它物体的颜色
        // param：指定反射的颜色。
//        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Utils.floatToBuffer(materialAmb));
//        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Utils.floatToBuffer(materialDiff));
//        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, Utils.floatToBuffer(materialSpec));

        for (Model model : models) {
            Bitmap bitmap = null;
            try {
                //判断是否在assets文件夹中
                if (context == null) {
                    bitmap = BitmapFactory.decodeFile(model.getTextureBitmapName());
                } else {
                    bitmap = BitmapFactory.decodeStream(context.getAssets().open(model.getTextureBitmapName()));
                }

                // 纹理id数组
                int[] textures = new int[1];

                //生成纹理对象索引
                //void glGenTextures (int n, int[] textures,int offset)
                //n：生成纹理对象索引数量
                //textures：纹理对象id保存数组
                //offset：偏移量
                gl.glGenTextures(1, textures, 0);
                model.setTextureId(textures);

                // 将生成的纹理id绑定到当前纹理通道
                //void glBindTexture (int target, int texture)
                //target：目标纹理通道 GL_TEXTURE_2D 2D纹理
                //texture：纹理id
                gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

                //设置纹理参数
                //void glTexParameterx (int target,int pname,int param)
                //target：目标纹理通道 GL_TEXTURE_2D 2D纹理
                //pname：单一纹理参数名
                // GL_TEXTURE_MAG_FILTER 映射到物体上放大情况
                // GL_TEXTURE_MIN_FILTER 映射到物体上缩小情况
                //param：单一纹理参数
                //GL_NEAREST 最近邻算法
                //GL_LINEAR 双线性插值算法
                gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                        GL10.GL_NEAREST);
                gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                        GL10.GL_LINEAR);

                // 将bitmap应用到纹理通道
                //void texImage2D (int target, int level,Bitmap bitmap,int border)
                //target：目标纹理通道 GL_TEXTURE_2D 2D纹理
                //level：执行的细节级别 0为最基础
                //bitmap：纹理图片
                //border：边框宽度 必须为0
                GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

                //绑定到纹理id为0的纹理上（关闭贴纹理，取消绑定）
                gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                bitmap.recycle();
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        //GLU.gluPerspective(gl,fovy,aspect,near,far);
        //aspect=width/height
        //fovy为观察物体需要的角度
        GLU.gluPerspective(gl, 45.0f, ((float) width) / height, 1f, 100f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        //eyeX,eyeY,eyeZ: 观测点坐标（相机坐标）
        // centerX,centerY,centerZ：观察位置的坐标
        // upX,upY,upZ ：相机观察方向在世界坐标系中的方向
        GLU.gluLookAt(gl,
                eyePoint.x, eyePoint.y, eyePoint.z,
                centerPoint.x, centerPoint.y, centerPoint.z,
                upPoint.x, upPoint.y, upPoint.z);

        //void glRotatef (float angle, float x,float y,float z)
        //沿向量x,y,z旋转angle角度
        gl.glRotatef(degree, 0, 1f, 0);

        //x,y,z方向各放缩scale倍
        gl.glScalef(scale, scale, scale);

        gl.glTranslatef(-modelCenterPoint.x, -modelCenterPoint.y, -modelCenterPoint.z);


        //设置向量数组指针
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        //开启2d纹理功能
        gl.glEnable(GL10.GL_TEXTURE_2D);

        for (Model model : models) {
            //设置向量数组指针
            //void glNormalPointer(int type,int stride,Buffer pointer)
            //type: 数组中每个顶点的坐标类型。取值：GL_BYTE,GL_SHORT, GL_FIXED, GL_FLOAT。
            //stride：数组中每个顶点间的间隔，步长（字节位移）。取值若为0，表示数组是连续的
            //pointer：即存储顶点的Buffer
            gl.glNormalPointer(GL10.GL_FLOAT, 0, model.getVertexVectorBuffer());
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, model.getVertexGridBuffer());

            //设置纹理数组指针
            //void glTexCoordPointer (int size, int type,int stride,Buffer pointer)
            //size：每个点用几个数值描述
            //type：数组中每个顶点的坐标类型。取值：GL_BYTE,GL_SHORT, GL_FIXED, GL_FLOAT。
            //stride：数组中每个顶点间的间隔，步长（字节位移）。取值若为0，表示数组是连续的
            //pointer：即存储顶点的Buffer
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, model.getTextureGridBuffer());

            //绑定纹理id
            gl.glBindTexture(GL10.GL_TEXTURE_2D, model.getTextureId()[0]);

            gl.glDrawArrays(GL10.GL_TRIANGLES, 0, model.getTriangleCounts() * 3);
        }

        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);

        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        //关闭2d纹理功能
        gl.glDisable(GL10.GL_TEXTURE_2D);

        gl.glFinish();
    }
}
