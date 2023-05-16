package com.introproventures.graphql.jpa.query.schema;

import static graphql.util.TraversalControl.CONTINUE;

import graphql.language.NamedNode;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLTypeVisitorStub;
import graphql.schema.SchemaTransformer;
import graphql.schema.idl.SchemaGeneratorPostProcessing;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;
import java.util.function.Function;

public class JavaScalarsWiringPostProcessor implements SchemaGeneratorPostProcessing {

    public JavaScalarsWiringPostProcessor() {}

    @Override
    public GraphQLSchema process(GraphQLSchema originalSchema) {
        Visitor visitor = new Visitor();

        return SchemaTransformer.transformSchema(originalSchema, visitor);
    }

    class Visitor extends GraphQLTypeVisitorStub {

        private boolean schemaChanged = false;

        public boolean schemaChanged() {
            return schemaChanged;
        }

        private TraversalControl changOrContinue(
            GraphQLSchemaElement node,
            GraphQLSchemaElement newNode,
            TraverserContext<GraphQLSchemaElement> context
        ) {
            if (node != newNode) {
                TreeTransformerUtil.changeNode(context, newNode);
                schemaChanged = true;
            }
            return CONTINUE;
        }

        private boolean isIntrospectionType(GraphQLNamedType type) {
            return type.getName().startsWith("__");
        }

        private <T extends GraphQLNamedType> boolean notSuitable(T node, Function<T, NamedNode<?>> suitableFunc) {
            if (isIntrospectionType(node)) {
                return true;
            }
            NamedNode<?> definition = suitableFunc.apply(node);
            return definition == null;
        }

        @Override
        public TraversalControl visitGraphQLScalarType(
            GraphQLScalarType node,
            TraverserContext<GraphQLSchemaElement> context
        ) {
            if (notSuitable(node, GraphQLScalarType::getDefinition)) {
                return CONTINUE;
            }

            GraphQLScalarType newNode = JavaScalars.of(node.getName()).orElse(node);

            return changOrContinue(node, newNode, context);
        }
    }
}
