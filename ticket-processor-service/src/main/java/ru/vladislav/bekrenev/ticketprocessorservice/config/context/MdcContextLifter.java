package ru.vladislav.bekrenev.ticketprocessorservice.config.context;


import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.Map;

import org.apache.logging.log4j.ThreadContext;

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
        wrap(() -> delegate.onSubscribe(s));
    }

    @Override
    public void onNext(T t) {
        wrap(() -> delegate.onNext(t));
    }

    @Override
    public void onError(Throwable t) {
        wrap(() -> delegate.onError(t));
    }

    @Override
    public void onComplete() {
        wrap(delegate::onComplete);
    }

    private void wrap(Runnable runnable) {
        try {
            setupMdcFromContext();
            runnable.run();
        } finally {
            ThreadContext.clearAll();
        }
    }

    private void setupMdcFromContext() {
        Context context = delegate.currentContext();

        if (context.hasKey("mdc-context")) {
            Map<String, String> mdcContext = context.get("mdc-context");
            if (!mdcContext.isEmpty()) {
                ThreadContext.clearAll();
                mdcContext.forEach(ThreadContext::put);
            }
        }
    }
}