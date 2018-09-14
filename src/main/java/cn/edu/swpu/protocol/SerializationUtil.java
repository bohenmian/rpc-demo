package cn.edu.swpu.protocol;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationUtil {

    // Class对象和Schema的关系映射
    private static Map<Class<?>, Schema<?>> schemaMap = new ConcurrentHashMap<>();

    // 创建objenesis对象创建器
    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil() {

    }

    /**
     * 根据Class对象获取Schema
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        // 根据Class获取Schema对象
        Schema<T> schema = (Schema<T>) schemaMap.get(clazz);
        if (schema == null) {
            // 如果Schema不存在则创建一个并加入到map中去
            schema = RuntimeSchema.createFrom(clazz);
            if (schema != null) {
                schemaMap.put(clazz, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化(对象 -> 字节数组)
     * @param obj
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        // 获取对象的Class对象
        Class<T> clazz = (Class<T>) obj.getClass();
        // LinkedBuffer中是一个byte数组和一个指向下一个LinkedBuffer的链表,这里是为数组分配默认大小
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            // 将给定的schema对象序列化成一个byte数组
            Schema<T> schema = getSchema(clazz);
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化(字节数组 -> 对象)
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            // 通过objenesis构建实例
            T message = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            // 使用给定schema对象和对象的byte数组合并得到序列化后的对象
            ProtobufIOUtil.mergeFrom(bytes, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
