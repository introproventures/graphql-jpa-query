/*
 * Copyright 2017 IntroPro Ventures, Inc. and/or its affiliates.
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
package com.introproventures.graphql.jpa.query.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;

import graphql.DeferredExecutionResult;
import graphql.ExecutionResult;
import graphql.GraphQL;

/**
 * Spring Boot GraphQL Query Rest Controller with HTTP mapping endpoints for GraphQLExecutor relay
 *
 * @see <a href="http://graphql.org/learn/serving-over-http/">Serving GraphQL over HTTP</a>
 *
 */
@RestController
@Transactional
public class GraphQLController {

    private static final String PATH = "${spring.graphql.jpa.query.path:/graphql}";
    public static final String APPLICATION_GRAPHQL_VALUE = "application/graphql";

    private final GraphQLExecutor   graphQLExecutor;
    private final ObjectMapper  mapper;

    /**
     * Creates instance of Spring GraphQLController RestController
     *
     * @param graphQLExecutor {@link GraphQLExecutor} instance
     * @param mapper {@link ObjectMapper} instance
     */
    public GraphQLController(GraphQLExecutor graphQLExecutor, ObjectMapper mapper) {
        super();
        this.graphQLExecutor = graphQLExecutor;
        this.mapper = mapper;
    }
    
    @GetMapping(value = PATH,
                consumes = MediaType.TEXT_EVENT_STREAM_VALUE,
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getEventStream(@RequestParam(name = "query") final String query,
                                     @RequestParam(name = "variables", required = false) final String variables) throws IOException {
        Map<String, Object> variablesMap = variablesStringToMap(variables);

        ExecutionResult executionResult = graphQLExecutor.execute(query, variablesMap);
        
        SseEmitter sseEmitter = new SseEmitter(180_000L); // FIXME need to add parameter
        sseEmitter.onTimeout(sseEmitter::complete);
        
        if(!executionResult.getErrors().isEmpty()) {
            sseEmitter.send(executionResult.toSpecification(), MediaType.APPLICATION_JSON);
            sseEmitter.completeWithError(new RuntimeException(executionResult.getErrors().toString()));
            return sseEmitter;
        }
        
        Publisher<ExecutionResult> deferredResults = executionResult.getData(); 
        
        // now send each deferred part which is given to us as a reactive stream
        // of deferred values
        deferredResults.subscribe(new Subscriber<ExecutionResult>() {
            Subscription subscription;
            Long id = 0L;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(1);
            }

            @Override
            public void onNext(ExecutionResult executionResult) {
                subscription.request(1);

                try {
                    Map<String, Object> result = executionResult.getData();
                    String name = result.keySet().iterator().next();
                    Object data = result.values().iterator().next();
                    
                    sseEmitter.send(SseEmitter.event()
                                              .id((id++).toString())
                                              .name(name)
                                              .data(data, MediaType.APPLICATION_JSON));
                } catch (IOException e) {
                    sseEmitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                sseEmitter.completeWithError(t);
            }

            @Override
            public void onComplete() {
                sseEmitter.complete();
            }
        });        
        
        return sseEmitter;
    }    

    /**
     * Handle standard GraphQL POST request that consumes
     * "application/json" content type with a JSON-encoded body
     * of the following format:
     * <pre>
     * {
     *   "query": "...",
     *   "variables": { "myVariable": "someValue", ... }
     * }
     * </pre>
     * @param queryRequest object
     * @return {@link ExecutionResult} response
     * @throws IOException exception
     */
    @PostMapping(value = PATH,
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void postJson(@RequestBody @Valid final GraphQLQueryRequest queryRequest,
                         HttpServletResponse httpServletResponse) throws IOException
    {
        ExecutionResult executionResult = graphQLExecutor.execute(queryRequest.getQuery(),
                                                                  queryRequest.getVariables());
        sendResponse(httpServletResponse, executionResult);
    }

    /**
     * Handle HTTP GET request.
     * The GraphQL query should be specified in the "query" query string.
     * i.e. <pre> http://server/graphql?query={query{name}}</pre>
     *
     * Query variables can be sent as a JSON-encoded string in an additional
     * query parameter called variables.
     *
     * @param query encoded JSON string
     * @param variables encoded JSON string
     * @return {@link ExecutionResult} response
     * @throws IOException exception
     */
    @GetMapping(value = PATH,
                consumes = {APPLICATION_GRAPHQL_VALUE},
                produces=MediaType.APPLICATION_JSON_VALUE)
    public void getQuery(@RequestParam(name = "query") final String query,
                         @RequestParam(name = "variables", required = false) final String variables,
                         HttpServletResponse httpServletResponse) throws IOException {
        
        Map<String, Object> variablesMap = variablesStringToMap(variables);

        ExecutionResult executionResult = graphQLExecutor.execute(query, variablesMap);
        
        sendResponse(httpServletResponse, executionResult);
    }

    /**
     * Handle HTTP FORM POST request.
     * The GraphQL query should be specified in the "query" query parameter string.
     *
     * Query variables can be sent as a JSON-encoded string in an additional
     * query parameter called variables.

     * @param query encoded JSON string
     * @param variables encoded JSON string
     * @return {@link ExecutionResult} response
     * @throws IOException exception
     */
    @PostMapping(value = PATH,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public void postForm(@RequestParam(name = "query") final String query,
                         @RequestParam(name = "variables", required = false) final String variables,
                         HttpServletResponse httpServletResponse) throws IOException    {
        Map<String, Object> variablesMap = variablesStringToMap(variables);

        ExecutionResult executionResult = graphQLExecutor.execute(query, variablesMap);
        
        sendResponse(httpServletResponse, executionResult);
    }

    /**
     * Handle POST with the "application/graphql" Content-Type header.
     * Treat the HTTP POST body contents as the GraphQL query string.
     *
     *
     * @param query a valid {@link GraphQLQueryRequest} input argument
     * @return {@link ExecutionResult} response
     * @throws IOException exception
     */
    @PostMapping(value = PATH,
            consumes = APPLICATION_GRAPHQL_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public void postApplicationGraphQL(@RequestBody final String query,
                                       HttpServletResponse httpServletResponse) throws IOException    {
        ExecutionResult executionResult = graphQLExecutor.execute(query, null);

        sendResponse(httpServletResponse, executionResult);
    }

    /**
     * Convert String argument to a Map as expected by {@link GraphQLJpaExecutor#execute(String, Map)}. GraphiQL posts both
     * query and variables as JSON encoded String, so Spring MVC mapping is useless here.
     * See: http://graphql.org/learn/serving-over-http/
     *
     * @param json JSON encoded string variables
     * @return a {@link HashMap} object of variable key-value pairs
     * @throws IOException exception
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> variablesStringToMap(final String json) throws IOException {
        Map<String, Object> variables = null;

        if (json != null && !json.isEmpty())
            variables = mapper.readValue(json, Map.class);

        return variables;
    }

    /**
     * GraphQL JSON HTTP Request Wrapper Class
     */
    @Validated
    public static class GraphQLQueryRequest {

        @NotNull
        private String query;

        private  Map<String, Object> variables;

        GraphQLQueryRequest() {}

        /**
         * @param query string
         */
        public GraphQLQueryRequest(String query) {
            super();
            this.query = query;
        }

        /**
         * @return the query
         */
        public String getQuery() {
            return this.query;
        }

        /**
         * @param query the query to set
         */
        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * @return the variables
         */
        public Map<String, Object> getVariables() {
            return this.variables;
        }

        /**
         * @param variables the variables to set
         */
        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }

    }
    
    private void sendResponse(HttpServletResponse response, ExecutionResult executionResult) throws IOException {
        if (hasDeferredResults(executionResult)) {
            sendDeferredResponse(response, executionResult, executionResult.getExtensions());
        } 
        else if (hasPublisherResults(executionResult)) {
            sendMultipartResponse(response, executionResult, executionResult.getData());
        } else {
            sendNormalResponse(response, executionResult);
        }
    }

    private void sendNormalResponse(HttpServletResponse response, ExecutionResult executionResult) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(response.getOutputStream(), executionResult.toSpecification());
    }    
    
    private boolean hasDeferredResults(ExecutionResult executionResult) {
        return Optional.ofNullable(executionResult.getExtensions())
                       .map(it -> it.containsKey(GraphQL.DEFERRED_RESULTS))
                       .orElse(false);
    }

    private boolean hasPublisherResults(ExecutionResult executionResult) {
        return Publisher.class.isInstance(executionResult.getData());
    }
    
    private static final String CRLF = "\r\n";

    @SuppressWarnings("unchecked")
    private void sendDeferredResponse(HttpServletResponse response, 
                                      ExecutionResult executionResult, 
                                      Map<Object, Object> extensions) {
        Publisher<DeferredExecutionResult> deferredResults = (Publisher<DeferredExecutionResult>) extensions.get(GraphQL.DEFERRED_RESULTS);
        try {
            sendMultipartResponse(response, executionResult, deferredResults);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMultipartResponse(HttpServletResponse response, 
                                       ExecutionResult executionResult, 
                                       Publisher<? extends ExecutionResult> deferredResults) {
        // this implements this Apollo defer spec: https://github.com/apollographql/apollo-server/blob/defer-support/docs/source/defer-support.md
        // the spec says CRLF + "-----" + CRLF is needed at the end, but it works without it and with it we get client
        // side errors with it, so let's skip it
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader("Content-Type", "multipart/mixed; boundary=\"-\"");
        response.setHeader("Connection", "keep-alive");

        // send the first "un deferred" part of the result
        if(hasDeferredResults(executionResult)) {
           writeAndFlushPart(response, executionResult.toSpecification());
        }

        // now send each deferred part which is given to us as a reactive stream
        // of deferred values
        deferredResults.subscribe(new Subscriber<ExecutionResult>() {
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(1);
            }

            @Override
            public void onNext(ExecutionResult executionResult) {
                subscription.request(1);

                writeAndFlushPart(response, executionResult.toSpecification());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace(System.err);
            }

            @Override
            public void onComplete() {
            }
        });

    }

    private void writeAndFlushPart(HttpServletResponse response, Map<String, Object> result) {
        DeferMultiPart deferMultiPart = new DeferMultiPart(result);
        StringBuilder sb = new StringBuilder();
        sb.append(CRLF).append("---").append(CRLF);
        String body = deferMultiPart.write();
        sb.append(body);
        writeAndFlush(response, sb);
    }

    private void writeAndFlush(HttpServletResponse response, StringBuilder sb) {
        try {
            PrintWriter writer = response.getWriter();
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private class DeferMultiPart {

        private Object body;

        public DeferMultiPart(Object data) {
            this.body = data;
        }

        public String write() {
            StringBuilder result = new StringBuilder();
            String bodyString = bodyToString();
            result.append("Content-Type: application/json").append(CRLF);
            result.append("Content-Length: ").append(bodyString.length()).append(CRLF).append(CRLF);
            result.append(bodyString);
            return result.toString();
        }

        private String bodyToString() {
            try {
                return mapper.writeValueAsString(body);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(e);
            }
        }
    }    

}
