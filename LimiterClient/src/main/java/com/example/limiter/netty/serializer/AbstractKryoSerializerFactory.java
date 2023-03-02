package com.example.limiter.netty.serializer;

import com.esotericsoftware.kryo.Kryo;
/**
 * @author feng xud
 */
public abstract class AbstractKryoSerializerFactory {

    private static final AbstractKryoSerializerFactory KRYO_SERIALIZER_FACTORY = new AbstractKryoPoolSerializerFactory();

    protected AbstractKryoSerializerFactory() {
    }

    public static AbstractKryoSerializerFactory getDefaultSerializerFactory() {
        return KRYO_SERIALIZER_FACTORY;
    }

    protected static Kryo initialRegister() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        return kryo;
    }
}
