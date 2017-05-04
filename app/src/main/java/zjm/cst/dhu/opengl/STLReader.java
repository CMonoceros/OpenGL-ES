package zjm.cst.dhu.opengl;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zjm on 2017/5/4.
 */

public class STLReader {

    public static interface StlLoadListener {
        void onStart();

        void onLoading(int index, int total);

        void onFinished();

        void onFailure(Exception e);
    }

    private StlLoadListener stlLoadListener;

    public Model parseStlByPath(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        } else {
            return parseStl(new FileInputStream(file));
        }
    }

    public Model parseStlWithPxyByPath(String path) throws IOException {
        File pxyFile = new File(path + ".pxy");
        File stlFile = new File(path + ".stl");
        if (!stlFile.exists() || !pxyFile.exists()) {
            return null;
        } else {
            return parseStlWithPxy(new FileInputStream(stlFile),
                    new FileInputStream(pxyFile),
                    path + ".jpg");
        }
    }

    public Model parseStlByAssets(Context context, String fileName) throws IOException {
        return parseStl(context.getAssets().open(fileName));
    }

    public Model parseStlWithPxyByAssets(Context context, String fileName) throws IOException {
        AssetManager assetManager = context.getAssets();
        return parseStlWithPxy(assetManager.open(fileName + ".stl"),
                assetManager.open(fileName + ".pxy"),
                fileName + ".jpg");
    }

    public Model parseStl(InputStream inputStream) throws IOException {
        if (stlLoadListener != null) {
            stlLoadListener.onStart();
        }
        Model model = new Model();

        //前80字节为文件头，存贮文件名
        inputStream.skip(80);

        //紧接着用 4 个字节的整数来描述模型的三角面片个数
        byte[] triangleCountsByte = new byte[4];
        inputStream.read(triangleCountsByte);
        int triangleCounts = Utils.byte4ToInt(triangleCountsByte, 0);
        model.setTriangleCounts(triangleCounts);
        if (triangleCounts == 0) {
            inputStream.close();
            return model;
        }

        //后面逐个给出每个三角面片的几何信息。每个三角面片占用固定的50个字节，依次是:
        // 3个4字节浮点数(角面片的法矢量)
        // 3个4字节浮点数(1个顶点的坐标)
        // 3个4字节浮点数(2个顶点的坐标)
        // 3个4字节浮点数(3个顶点的坐标)个
        // 最后2个字节用来描述三角面片的属性信息
        byte[] triangleFaceByte = new byte[50 * triangleCounts];
        inputStream.read(triangleFaceByte);
        inputStream.close();

        // 保存所有顶点坐标信息,一个三角形3个顶点，一个顶点3个坐标轴
        float[] vertexGridArray = new float[triangleCounts * 3 * 3];
        // 保存所有三角面对应的法向量位置，
        // 一个三角面对应一个法向量，一个法向量有3个点
        // 而绘制模型时，是针对需要每个顶点对应的法向量，因此存储长度需要*3
        // 又同一个三角面的三个顶点的法向量是相同的，
        // 因此后面写入法向量数据的时候，只需连续写入3个相同的法向量即可
        float[] vertexVectorArray = new float[triangleCounts * 3 * 3];
        //保存所有三角面的属性信息
        short[] remarks = new short[triangleCounts];

        int stlOffset = 0;
        try {
            for (int i = 0; i < triangleCounts; i++) {
                if (stlLoadListener != null) {
                    stlLoadListener.onLoading(i, triangleCounts);
                }
                for (int j = 0; j < 4; j++) {
                    float x = Utils.byte4ToFloat(triangleFaceByte, stlOffset);
                    float y = Utils.byte4ToFloat(triangleFaceByte, stlOffset + 4);
                    float z = Utils.byte4ToFloat(triangleFaceByte, stlOffset + 8);
                    stlOffset += 12;

                    //法向量
                    if (j == 0) {
                        for (int k = 0; k < 9; k++) {
                            if (k % 3 == 0) {
                                vertexVectorArray[i * 9 + k] = x;
                            } else if (k % 3 == 1) {
                                vertexVectorArray[i * 9 + k] = y;
                            } else {
                                vertexVectorArray[i * 9 + k] = z;
                            }
                        }
                    }
                    //顶点
                    else {
                        vertexGridArray[i * 9 + (j - 1) * 3] = x;
                        vertexGridArray[i * 9 + (j - 1) * 3 + 1] = y;
                        vertexGridArray[i * 9 + (j - 1) * 3 + 2] = z;

                        //最大值最小值初始化及记录
                        if (i == 0 && j == 1) {
                            model.maxX = model.minX = x;
                            model.maxY = model.minY = y;
                            model.maxZ = model.minZ = z;
                        } else {
                            model.maxX = Math.max(model.maxX, x);
                            model.maxY = Math.max(model.maxY, y);
                            model.maxZ = Math.max(model.maxZ, z);
                            model.minX = Math.min(model.minX, x);
                            model.minY = Math.min(model.minY, y);
                            model.minZ = Math.min(model.minZ, z);
                        }
                    }
                }

                //属性信息
                short r = Utils.byte2ToShort(triangleFaceByte, stlOffset);
                stlOffset += 2;
                remarks[i] = r;
            }
        } catch (Exception e) {
            if (stlLoadListener != null) {
                stlLoadListener.onFailure(e);
            } else {
                e.printStackTrace();
            }
        }
        model.setVertexGridArray(vertexGridArray);
        model.setVertexVectorArray(vertexVectorArray);
        model.setRemarksArray(remarks);


        if (stlLoadListener != null) {
            stlLoadListener.onFinished();
        }
        return model;
    }

    public Model parseStlWithPxy(InputStream stlInputStream, InputStream pxyInputStream, String bitmapName) throws IOException {
        Model model = parseStl(stlInputStream);
        model = parsePxy(model, pxyInputStream);
        model.setTextureBitmapName(bitmapName);
        return model;
    }

    private Model parsePxy(Model model, InputStream inputStream) throws IOException {
        int triangleCounts = model.getTriangleCounts();

        //三角面片有3个顶点，一个顶点有2个坐标轴数据，每个坐标轴数据是float类型（4字节）
        byte[] textureBytes = new byte[triangleCounts * 3 * 2 * 4];
        inputStream.read(textureBytes);

        //保存纹理坐标数组 三角面个数有三个顶点，一个顶点对应纹理二维坐标
        float[] textureArray = new float[triangleCounts * 3 * 2];
        int textureOffset = 0;
        for (int i = 0; i < triangleCounts * 3; i++) {
            //坐标的取值范围为[0,1],表示的坐标位置是在纹理图片上的对应宽高比例
            float x = Utils.byte4ToFloat(textureBytes, textureOffset);
            float y = Utils.byte4ToFloat(textureBytes, textureOffset + 4);

            textureArray[i * 2] = x;
            //pxy文件原点是在左下角，因此需要用1减去y坐标值
            textureArray[i * 2 + 1] = 1 - y;

            textureOffset += 8;
        }
        model.setTextureGridArray(textureArray);
        return model;
    }
}
