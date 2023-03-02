package com.example.limiter.netty.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.ByteArrayOutputStream;

public class KryoPoolSerializerFactory extends KryoSerializerFactory {

    private static Pool<Kryo> kryoPool = new Pool<Kryo>(true, false, 8) {
        protected Kryo create() {
            return initialRegister();
        }
    };

    public static byte[] serialize(Object object) {
        Kryo kryo = kryoPool.obtain();
        Output output = new Output(new ByteArrayOutputStream());
        try {
            kryo.writeClassAndObject(output, object);
            return output.toBytes();
        } finally {
            kryoPool.free(kryo);
            output.close();
            output.flush();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(ByteBuf out) {
        Kryo kryo = kryoPool.obtain();
        Input input = new Input(new ByteBufInputStream(out));
        try {
            return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            kryoPool.free(kryo);
            input.close();
        }
    }
}

