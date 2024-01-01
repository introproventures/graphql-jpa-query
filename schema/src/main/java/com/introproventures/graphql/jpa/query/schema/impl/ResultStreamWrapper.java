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
import java.util.List;
import java.util.stream.Stream;

class ResultStreamWrapper<T> {

    @SuppressWarnings("unchecked")
    public static <T> List<T> wrap(Stream<T> stream, int size) {
        return (List<T>) Proxy.newProxyInstance(
            ResultStreamWrapper.class.getClassLoader(),
            new Class[] { List.class },
            new ListProxyInvocationHandler<T>(stream, size)
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> wrap(Collection<T> collection, int size) {
        return wrap(collection.stream(), size);
    }

    static class ListProxyInvocationHandler<T> implements InvocationHandler {

        private final Stream<T> stream;
        private final int size;

        public ListProxyInvocationHandler(Stream<T> stream, int size) {
            this.stream = stream;
            this.size = size;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("size".equals(method.getName())) {
                return size;
            } else if ("iterator".equals(method.getName())) {
                return stream.limit(size).iterator();
            } else if ("equals".equals(method.getName())) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0]);
            } else if ("hashCode".equals(method.getName())) {
                // Use hashCode of service locator proxy.
                return System.identityHashCode(proxy);
            } else if ("spliterator".equals(method.getName())) {
                return stream.spliterator();
            }
            throw new UnsupportedOperationException(method + " is not supported");
        }
    }
}
