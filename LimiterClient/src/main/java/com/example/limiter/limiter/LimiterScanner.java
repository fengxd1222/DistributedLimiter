package com.example.limiter.limiter;

import com.example.limiter.netty.remote.LimiterDefinition;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author feng xud
 */
public class LimiterScanner {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LimiterScanner.class);
    private final Map<String, LimiterDefinition> definitionMap = new HashMap<>();

    public List<LimiterDefinition> doScan() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath("com.example.limiter.controller") + "/**/*.class");
            SimpleMetadataReaderFactory factory = new SimpleMetadataReaderFactory(resolver.getClassLoader());
            for (Resource resource : resources) {

                String className = factory.getMetadataReader(resource).getClassMetadata().getClassName();
                Class<?> aClass = org.apache.commons.lang3.ClassUtils.getClass(className);

                Method[] methods = aClass.getMethods();
                for (Method method : methods) {
                    DistributedQPSLimiter declaredAnnotation = method.getDeclaredAnnotation(DistributedQPSLimiter.class);
                    if (declaredAnnotation == null) {
                        continue;
                    }
                    String methodKey = className + "." + method.getName();
                    if(definitionMap.containsKey(methodKey)){
                        log.warn("LimiterDefinitionMap has repeat methodKey : "+methodKey);
                        continue;
                    }
                    LimiterDefinition limiterDefinition = new LimiterDefinition(declaredAnnotation.qps(), declaredAnnotation.limit(), declaredAnnotation.time(), methodKey);
                    definitionMap.put(methodKey, limiterDefinition);
                }
            }
            if(definitionMap.isEmpty()){
                throw new RuntimeException("definitionMap is empty");
            }
            return new ArrayList<>(definitionMap.values());
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
