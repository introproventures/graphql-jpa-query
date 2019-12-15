package com.introproventures.graphql.jpa.query.schema.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.stream.Stream;


class ResultStreamWrapper<T> {
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> wrap(Stream<T> stream) {
        return (List<T>) Proxy.newProxyInstance(ResultStreamWrapper.class.getClassLoader(), 
                                                new Class[] { List.class }, 
                                                new ListProxyInvocationHandler<T>(stream));
    }
    
    static class ListProxyInvocationHandler<T> implements InvocationHandler {
        private final Stream<T> stream;
        
        public ListProxyInvocationHandler(Stream<T> stream) {
            this.stream = stream;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("size".equals(method.getName())) {
                return 0; // FIXME maybe use query count?
            }
            else if("iterator".equals(method.getName())) {
                return stream.iterator();
            } else if ("equals".equals(method.getName())) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0]);
            }
            else if ("hashCode".equals(method.getName())) {
                // Use hashCode of service locator proxy.
                return System.identityHashCode(proxy);
            }
            
            throw new UnsupportedOperationException(method + " is not supported");
        }
    }
    
}
