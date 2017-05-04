package zjm.cst.dhu.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by zjm on 2017/5/4.
 */

public class Utils {
    public static FloatBuffer floatToBuffer(float[] array) {
        //初始化byte缓冲 1float=4byte
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 4);
        //用本机字节顺序修改缓冲区字节顺序
        byteBuffer.order(ByteOrder.nativeOrder());
        //转换为float缓冲
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        //将数组写入缓冲区
        floatBuffer.put(array);
        //设置此缓冲区的位置。如果标记已定义并且大于新的位置，则要丢弃该标记。
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static int byte4ToInt(byte[] bytes, int offset) {
        int b3 = bytes[offset + 3] & 0xFF;
        int b2 = bytes[offset + 2] & 0xFF;
        int b1 = bytes[offset + 1] & 0xFF;
        int b0 = bytes[offset + 0] & 0xFF;
        return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }

    public static short byte2ToShort(byte[] bytes, int offset) {
        int b1 = bytes[offset + 1] & 0xFF;
        int b0 = bytes[offset + 0] & 0xFF;
        return (short) ((b1 << 8) | b0);
    }

    public static float byte4ToFloat(byte[] bytes, int offset) {
        return Float.intBitsToFloat(byte4ToInt(bytes, offset));
    }

    private static Point modelsBorder(List<Model> models, boolean isMin) {
        Point p;
        if (isMin) {
            p = new Point(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        } else {
            p = new Point(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        }
        for (Model model : models) {
            if (isMin) {
                if (model.minX < p.x) {
                    p.x = model.minX;
                }
                if (model.minY < p.y) {
                    p.y = model.minY;
                }
                if (model.minZ < p.z) {
                    p.z = model.minZ;
                }
            } else {
                if (model.maxX > p.x) {
                    p.x = model.maxX;
                }
                if (model.maxY > p.y) {
                    p.y = model.maxY;
                }
                if (model.maxZ > p.z) {
                    p.z = model.maxZ;
                }
            }
        }
        return p;
    }

    public static Point getCenterPoint(List<Model> models) {
        Point min = modelsBorder(models, true);
        Point max = modelsBorder(models, false);
        float x = min.x + (max.x - min.x) / 2;
        float y = min.y + (max.y - min.y) / 2;
        float z = min.z + (max.z - min.z) / 2;
        return new Point(x, y, z);
    }

    public static float getRadius(List<Model> models) {
        Point min = modelsBorder(models, true);
        Point max = modelsBorder(models, false);
        float x = max.x - min.x;
        float y = max.y - min.y;
        float z = max.z - min.z;
        float r = (float) (Math.sqrt(x * x + y * y + z * z) / 2);
        return r;
    }
}
