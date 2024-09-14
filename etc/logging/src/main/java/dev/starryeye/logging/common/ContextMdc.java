package dev.starryeye.logging.common;

import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.util.context.Context;

import java.util.Map;

@Component
public class ContextMdc {

    private static final String CONTEXT_MDC_KEY = "MDC_KEY";

    //https://www.earlgrey02.com/post/1
    //https://ckddn9496.tistory.com/180

    @PostConstruct
    public void setUp() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                CONTEXT_MDC_KEY,
                MDC::getCopyOfContextMap,
                MDC::setContextMap,
                MDC::clear);
    }

    public Context createContext(ContextMdcKey key, String value) {
        return Context.of(CONTEXT_MDC_KEY, Map.of(key.getKey(), value));
    }

    public void put(ContextMdcKey key, String value) {
        Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        copyOfContextMap.put(key.getKey(), value);
        MDC.setContextMap(copyOfContextMap);
    }
}
