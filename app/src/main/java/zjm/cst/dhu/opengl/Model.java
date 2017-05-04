package zjm.cst.dhu.opengl;

import android.graphics.Bitmap;

import java.nio.FloatBuffer;

/**
 * Created by zjm on 2017/5/4.
 */

public class Model {
    //三角形面数
    private int triangleCounts;
    //三角形顶点坐标数组
    private float[] vertexGridArray;
    //三角形顶点法向量数组
    private float[] vertexVectorArray;
    //三角面属性信息数组
    private short[] remarksArray;
    //纹理坐标数组
    private float[] textureGridArray;
    //纹理图片名称
    private String textureBitmapName;
    //纹理ID数组
    private int[] textureId;

    private FloatBuffer vertexGridBuffer;
    private FloatBuffer vertexVectorBuffer;
    private FloatBuffer textureGridBuffer;

    //所有点在x,y,z方向最大值及最小值
    float maxX, maxY, maxZ;
    float minX, minY, minZ;

    //获取模型中心点
    public Point getCenterPoint() {
        return new Point(minX + (maxX - minX) / 2,
                minY + (maxY - minY) / 2,
                minZ + (maxZ - minZ) / 2);
    }

    //获取包裹模型最大半径
    public float getMaxRadius() {
        float x = maxX - minX;
        float y = maxY - minY;
        float z = maxZ - minZ;
        return (float) (Math.sqrt(x * x + y * y + z * z) / 2);
    }

    public void setVertexGridArray(float[] grid) {
        this.vertexGridArray = grid;
        vertexGridBuffer = Utils.floatToBuffer(vertexGridArray);
    }

    public void setVertexVectorArray(float[] vector) {
        this.vertexVectorArray = vector;
        vertexVectorBuffer = Utils.floatToBuffer(vertexVectorArray);
    }

    public void setTriangleCounts(int counts) {
        this.triangleCounts = counts;
    }

    public void setRemarksArray(short[] remarksArray) {
        this.remarksArray = remarksArray;
    }

    public void setTextureGridArray(float[] texture) {
        this.textureGridArray = texture;
        textureGridBuffer = Utils.floatToBuffer(textureGridArray);
    }

    public void setTextureBitmapName(String textureBitmapName) {
        this.textureBitmapName = textureBitmapName;
    }

    public void setTextureId(int[] id) {
        this.textureId = id;
    }

    public FloatBuffer getVertexGridBuffer() {
        return vertexGridBuffer;
    }

    public FloatBuffer getVertexVectorBuffer() {
        return vertexVectorBuffer;
    }

    public int getTriangleCounts() {
        return triangleCounts;
    }

    public FloatBuffer getTextureGridBuffer() {
        return textureGridBuffer;
    }

    public String getTextureBitmapName() {
        return textureBitmapName;
    }

    public int[] getTextureId() {
        return textureId;
    }

}
