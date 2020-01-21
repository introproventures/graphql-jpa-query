package com.introproventures.graphql.jpa.query.schema.relay;

import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.getSelectionField;
import static com.introproventures.graphql.jpa.query.support.GraphQLSupport.searchByFieldName;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaQueryFactory;
import com.introproventures.graphql.jpa.query.schema.impl.PagedResult;

import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class GraphQLJpaRelayDataFetcher implements DataFetcher<Page<Object>> {
    private final static Logger logger = LoggerFactory.getLogger(GraphQLJpaRelayDataFetcher.class);

    private static final String EDGES = "edges";
    private static final String FIRST = "first";
    private static final String AFTER = "after";
    private static final String PAGE_INFO = "pageInfo";

    private final int defaultMaxResults;
    private final int defaultFirstSize;
    private final GraphQLJpaQueryFactory queryFactory;

    private GraphQLJpaRelayDataFetcher(Builder builder) {
        this.queryFactory = builder.queryFactory;
        this.defaultMaxResults = builder.defaultMaxResults;
        this.defaultFirstSize = builder.defaultFirstSize;
    }
    
    @Override
    public Page<Object> get(DataFetchingEnvironment environment) throws Exception {
        final Field rootNode = environment.getField();
        
        Optional<Field> edgesSelection = searchByFieldName(rootNode, EDGES);
        Optional<Field> pageInfoSelection = getSelectionField(rootNode, PAGE_INFO);
        
        final Integer first = Optional.<Integer> ofNullable(environment.getArgument(FIRST))
                             .orElse(defaultFirstSize);
        
        final String after = Optional.<String>ofNullable(environment.getArgument(AFTER))
                               .orElse(new OffsetBasedCursor(0L).toConnectionCursor()
                                                                .toString());

        final OffsetBasedCursor cursor = OffsetBasedCursor.fromCursor(after);
        
        final int firstResult = Integer.parseInt(Long.toString(cursor.getOffset()));
        final int maxResults = Integer.min(first, defaultMaxResults);
        
        final PagedResult.Builder<Object> pagedResult = PagedResult.builder()
                .withOffset(firstResult)
                .withLimit(maxResults);
        
        if (edgesSelection.isPresent()) {
            final List<Object> keys = queryFactory.queryKeys(environment,
                                                             firstResult,
                                                             maxResults);
            if(!keys.isEmpty()) {
                final List<Object> resultList = queryFactory.queryResultList(environment,
                                                                             maxResults,
                                                                             keys);
                pagedResult.withSelect(resultList);
            }
        }
        
        if (pageInfoSelection.isPresent()) {
            
            final Long total = queryFactory.queryTotalCount(environment);

            pagedResult.withTotal(total);
        }
        
        PagedResult<Object> result = pagedResult.build();
        
        return PageFactory.createOffsetBasedPage(result.getSelect(), 
                                                 result.getTotal(), 
                                                 result.getOffset());
    }

    /**
     * Creates builder to build {@link GraphQLJpaRelayDataFetcher}.
     * @return created builder
     */
    public static IQueryFactoryStage builder() {
        return new Builder();
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IQueryFactoryStage {

        /**
        * Builder method for queryFactory parameter.
        * @param queryFactory field to set
        * @return builder
        */
        public IDefaultMaxResultsStage withQueryFactory(GraphQLJpaQueryFactory queryFactory);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IDefaultMaxResultsStage {

        /**
        * Builder method for defaultMaxResults parameter.
        * @param defaultMaxResults field to set
        * @return builder
        */
        public IDefaultFirstSizeStage withDefaultMaxResults(int defaultMaxResults);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IDefaultFirstSizeStage {

        /**
        * Builder method for defaultFirstSize parameter.
        * @param defaultFirstSize field to set
        * @return builder
        */
        public IBuildStage withDefaultFirstSize(int defaultFirstSize);
    }

    /**
     * Definition of a stage for staged builder.
     */
    public interface IBuildStage {

        /**
        * Builder method of the builder.
        * @return built class
        */
        public GraphQLJpaRelayDataFetcher build();
    }

    /**
     * Builder to build {@link GraphQLJpaRelayDataFetcher}.
     */
    public static final class Builder implements IQueryFactoryStage, IDefaultMaxResultsStage, IDefaultFirstSizeStage, IBuildStage {

        private GraphQLJpaQueryFactory queryFactory;
        private int defaultMaxResults;
        private int defaultFirstSize;

        private Builder() {
        }

        @Override
        public IDefaultMaxResultsStage withQueryFactory(GraphQLJpaQueryFactory queryFactory) {
            this.queryFactory = queryFactory;
            return this;
        }

        @Override
        public IDefaultFirstSizeStage withDefaultMaxResults(int defaultMaxResults) {
            this.defaultMaxResults = defaultMaxResults;
            return this;
        }

        @Override
        public IBuildStage withDefaultFirstSize(int defaultFirstSize) {
            this.defaultFirstSize = defaultFirstSize;
            return this;
        }

        @Override
        public GraphQLJpaRelayDataFetcher build() {
            return new GraphQLJpaRelayDataFetcher(this);
        }
    }

}
