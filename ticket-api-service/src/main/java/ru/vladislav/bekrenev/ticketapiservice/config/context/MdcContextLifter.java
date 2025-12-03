package ru.vladislav.bekrenev.ticketapiservice.config.context;

import org.apache.logging.log4j.ThreadContext;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;
import ru.vladislav.bekrenev.ticketapiservice.filter.CorrelationIdWebFilter;

import java.util.Map;

public class MdcContextLifter<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> delegate;

    public MdcContextLifter(CoreSubscriber<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Context currentContext() {
        return delegate.currentContext();
    }

    @Override
    public void onSubscribe(Subscription s) {
        restoreThreadContext();
        delegate.onSubscribe(s);
    }

    @Override
    public void onNext(T t) {
        restoreThreadContext();
        delegate.onNext(t);
    }

    @Override
    public void onError(Throwable t) {
        restoreThreadContext();
        delegate.onError(t);
    }

    @Override
    public void onComplete() {
        restoreThreadContext();
        delegate.onComplete();
    }

    private void restoreThreadContext() {
        Context reactorContext = delegate.currentContext();

        if (reactorContext.hasKey(CorrelationIdWebFilter.MDC_CONTEXT_KEY)) {
            Map<String, String> contextMap = reactorContext.get(
                    CorrelationIdWebFilter.MDC_CONTEXT_KEY
            );

            ThreadContext.clearAll();
            if (contextMap != null && !contextMap.isEmpty()) {
                ThreadContext.putAll(contextMap);
            }
        }

    }
}