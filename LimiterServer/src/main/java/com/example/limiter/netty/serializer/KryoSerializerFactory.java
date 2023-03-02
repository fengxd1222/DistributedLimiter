package com.example.limiter.netty.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.example.limiter.netty.remote.ClientLimiterRequest;
import com.example.limiter.netty.remote.LimiterDefinition;

import java.util.*;

public abstract class KryoSerializerFactory {

    private static final KryoSerializerFactory kryoSerializerFactory = new KryoPoolSerializerFactory();

    protected KryoSerializerFactory() {
    }

    public static KryoSerializerFactory getDefaultSerializerFactory() {
        return kryoSerializerFactory;
    }

    protected static Kryo initialRegister() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        return kryo;
    }
}
