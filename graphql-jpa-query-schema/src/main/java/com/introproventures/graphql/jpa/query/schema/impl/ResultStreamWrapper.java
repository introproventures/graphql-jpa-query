/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.introproventures.graphql.jpa.query.schema.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;


class ResultStreamWrapper<T> {
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> wrap(Stream<T> stream,
                                   int size) {
        return (List<T>) Proxy.newProxyInstance(ResultStreamWrapper.class.getClassLoader(), 
                                                new Class[] { List.class }, 
                                                new ListProxyInvocationHandler<T>(stream.iterator(),
                                                                                  size));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> List<T> wrap(Collection<T> stream,
                                   int size) {
        return (List<T>) Proxy.newProxyInstance(ResultStreamWrapper.class.getClassLoader(), 
                                                new Class[] { List.class }, 
                                                new ListProxyInvocationHandler<T>(stream.iterator(),
                                                                                  size));
    }
    
    static class ListProxyInvocationHandler<T> implements InvocationHandler {
        private final Iterator<T> stream;
        private final int size;
        
        public ListProxyInvocationHandler(Iterator<T> stream,
                                          int size) {
            this.stream = stream;
            this.size = size;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("size".equals(method.getName())) {
                return size; 
            }
            else if("iterator".equals(method.getName())) {
                return new ResultIteratorWrapper(stream,
                                                 size);
            } else if ("equals".equals(method.getName())) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0]);
            }
            else if ("hashCode".equals(method.getName())) {
                // Use hashCode of service locator proxy.
                return System.identityHashCode(proxy);
            }
            else if ("spliterator".equals(method.getName())) {
                // Use hashCode of service locator proxy.
                return new ResultSpliteratorWrapper(new ResultIteratorWrapper(stream,
                                                                              size));
            }
            throw new UnsupportedOperationException(method + " is not supported");
        }

        public class ResultSpliteratorWrapper implements Spliterator<T> {
            final ResultIteratorWrapper delegate;

            public ResultSpliteratorWrapper(ResultIteratorWrapper delegate) {
                this.delegate = delegate;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (delegate.hasNext()) {
                    action.accept(delegate.next());
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<T> trySplit() {
                throw new UnsupportedOperationException("method trySplit is not supported");
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return 0;
            }
        }
        
        class ResultIteratorWrapper implements Iterator<T> {
            
            final Iterator<T> delegate;
            final int size;
            int current = 0;
            
            ResultIteratorWrapper(Iterator<T> delegate,
                                  int size) {
                this.delegate = delegate;
                this.size = size;
                
            }

            @Override
            public boolean hasNext() {
                return (current < size) && delegate.hasNext();
            }

            @Override
            public T next() {
                T result = delegate.next();
                
                try {
                    return result;
                } finally {
                    current++;
                }
            }
        }
    }
}
