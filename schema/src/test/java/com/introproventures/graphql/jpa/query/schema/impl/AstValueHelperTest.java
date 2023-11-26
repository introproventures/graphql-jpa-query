package com.introproventures.graphql.jpa.query.schema.impl;

import static com.introproventures.graphql.jpa.query.schema.impl.AstValueHelper.astFromValue;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.language.BooleanValue.newBooleanValue;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

class AstValueHelperTest {

    @Test
    void convertsBooleanValuesToASTs() {
        assertThat(astFromValue(true, GraphQLBoolean).isEqualTo(newBooleanValue(true).build())).isTrue();

        assertThat(astFromValue(false, GraphQLBoolean).isEqualTo(newBooleanValue(false).build())).isTrue();

        assertThat(astFromValue(null, GraphQLBoolean)).isNull();

        assertThat(astFromValue(0, GraphQLBoolean).isEqualTo(newBooleanValue(false).build())).isTrue();

        assertThat(astFromValue(1, GraphQLBoolean).isEqualTo(newBooleanValue(true).build())).isTrue();

        var NonNullBoolean = nonNull(GraphQLBoolean);

        assertThat(astFromValue(0, NonNullBoolean).isEqualTo(newBooleanValue(false).build())).isTrue();
    }

    BigInteger bigInt(int i) {
        return new BigInteger(String.valueOf(i));
    }

    @Test
    void convertsIntValuesToIntASTs() {
        assertThat(astFromValue(123.0, GraphQLInt).isEqualTo(IntValue.newIntValue(bigInt(123)).build())).isTrue();

        assertThat(astFromValue(1e4, GraphQLInt).isEqualTo(IntValue.newIntValue(bigInt(10000)).build())).isTrue();
    }

    @Test
    void convertsFloatValuesToIntFloatASTs() {
        assertThat(
            astFromValue(123.0, GraphQLFloat).isEqualTo(FloatValue.newFloatValue(BigDecimal.valueOf(123.0)).build())
        )
            .isTrue();

        assertThat(
            astFromValue(123.5, GraphQLFloat).isEqualTo(FloatValue.newFloatValue(BigDecimal.valueOf(123.5)).build())
        )
            .isTrue();

        assertThat(
            astFromValue(1e4, GraphQLFloat).isEqualTo(FloatValue.newFloatValue(BigDecimal.valueOf(10000.0)).build())
        )
            .isTrue();

        assertThat(
            astFromValue(1e40, GraphQLFloat).isEqualTo(FloatValue.newFloatValue(BigDecimal.valueOf(1.0e40)).build())
        )
            .isTrue();
    }

    @Test
    void convertsStringValuesToStringASTs() {
        assertThat(astFromValue("hello", GraphQLString).isEqualTo(new StringValue("hello"))).isTrue();

        assertThat(astFromValue("VALUE", GraphQLString).isEqualTo(new StringValue("VALUE"))).isTrue();

        assertThat(
            astFromValue("VA\n\t\f\r\b\\LUE", GraphQLString).isEqualTo(new StringValue("VA\\n\\t\\f\\r\\b\\\\LUE"))
        )
            .isTrue();

        assertThat(astFromValue("VA/LUE", GraphQLString).isEqualTo(new StringValue("VA\\/LUE"))).isTrue();

        assertThat(astFromValue("VA\\L\"UE", GraphQLString).isEqualTo(new StringValue("VA\\\\L\\\"UE"))).isTrue();

        assertThat(astFromValue(123, GraphQLString).isEqualTo(new StringValue("123"))).isTrue();

        assertThat(astFromValue(false, GraphQLString).isEqualTo(new StringValue("false"))).isTrue();

        assertThat(astFromValue(null, GraphQLString)).isNull();
    }

    @Test
    void convertsIDValuesToIntStringASTs() {
        assertThat(astFromValue("hello", GraphQLID).isEqualTo(new StringValue("hello"))).isTrue();

        assertThat(astFromValue("VALUE", GraphQLID).isEqualTo(new StringValue("VALUE"))).isTrue();

        // Note: EnumValues cannot contain non-identifier characters
        assertThat(astFromValue("VA\nLUE", GraphQLID).isEqualTo(new StringValue("VA\\nLUE"))).isTrue();

        // Note: IntValues are used when possible.
        assertThat(astFromValue(123, GraphQLID).isEqualTo(new IntValue(bigInt(123)))).isTrue();

        assertThat(astFromValue(null, GraphQLID)).isNull();
    }

    @Test
    void doesNotConvertsNonNullValuesToNullValue() {
        var NonNullBoolean = nonNull(GraphQLBoolean);
        assertThat(astFromValue(null, NonNullBoolean)).isNull();
    }

    Map<String, Object> complexValue = Map.of("someArbitrary", "complexValue");

    GraphQLEnumType myEnum = new GraphQLEnumType.Builder()
        .name("MyEnum")
        .value("HELLO")
        .value("GOODBYE")
        .value("COMPLEX", complexValue)
        .build();

    @Test
    void convertsStringValuesToEnumASTsIfPossible() {
        assertThat(astFromValue("HELLO", myEnum).isEqualTo(new EnumValue("HELLO"))).isTrue();

        assertThat(astFromValue(complexValue, myEnum).isEqualTo(new EnumValue("COMPLEX"))).isTrue();
        //        // Note: case sensitive
        //        assertThat(astFromValue("hello", myEnum)).isNull();
        //
        //        // Note: Not a valid enum value
        //        assertThat(astFromValue("VALUE", myEnum)).isNull();
    }

    @Test
    void convertsArrayValuesToListASTs() {
        assertThat(
            astFromValue(new String[] { "FOO", "BAR" }, list(GraphQLString))
                .isEqualTo(new ArrayValue(List.of(new StringValue("FOO"), new StringValue("BAR"))))
        )
            .isTrue();

        assertThat(
            astFromValue(new String[] { "HELLO", "GOODBYE" }, list(myEnum))
                .isEqualTo(new ArrayValue(List.of(new EnumValue("HELLO"), new EnumValue("GOODBYE"))))
        )
            .isTrue();
    }

    @Test
    void convertsListSingletons() {
        assertThat(astFromValue("FOO", list(GraphQLString)).isEqualTo(new StringValue("FOO"))).isTrue();
    }

    @Test
    void convertsInputObjects() {
        var inputObj = GraphQLInputObjectType
            .newInputObject()
            .name("MyInputObj")
            .field(newInputObjectField().name("foo").type(GraphQLFloat))
            .field(newInputObjectField().name("bar").type(myEnum))
            .build();

        assertThat(
            astFromValue(List.of("foo", 3, "bar", "HELLO"), inputObj)
                .isEqualTo(
                    new ObjectValue(
                        List.of(
                            new ObjectField("foo", new IntValue(bigInt(3))),
                            new ObjectField("bar", new EnumValue("HELLO"))
                        )
                    )
                )
        )
            .isTrue();
    }

    @Test
    void convertsInputObjectsWithExplicitNulls() {
        var inputObj = GraphQLInputObjectType
            .newInputObject()
            .name("MyInputObj")
            .field(newInputObjectField().name("foo").type(GraphQLFloat))
            .field(newInputObjectField().name("bar").type(myEnum))
            .build();

        assertThat(
            astFromValue(Maps.newHashMap("foo", null), inputObj)
                .isEqualTo(new ObjectValue(List.of(new ObjectField("foo", NullValue.of()))))
        )
            .isTrue();
    }

    @Test
    void parseAstLiterals() {
        assertThat(AstValueHelper.valueFromAst("\"s\"")).isInstanceOf(StringValue.class);
        assertThat(AstValueHelper.valueFromAst("true")).isInstanceOf(BooleanValue.class);
        assertThat(AstValueHelper.valueFromAst("666")).isInstanceOf(IntValue.class);
        assertThat(AstValueHelper.valueFromAst("666.6")).isInstanceOf(FloatValue.class);
        assertThat(AstValueHelper.valueFromAst("[\"A\", \"B\", \"C\"]")).isInstanceOf(ArrayValue.class);
        assertThat(AstValueHelper.valueFromAst("{string : \"s\", integer : 1, boolean : true}"))
            .isInstanceOf(ObjectValue.class);
    }

    @Test
    void encodingOfJsonStrings() {
        assertThat(AstValueHelper.jsonStringify("")).isEqualTo("");
        assertThat(AstValueHelper.jsonStringify("json")).isEqualTo("json");
        assertThat(AstValueHelper.jsonStringify("quotation-\"")).isEqualTo("quotation-\\\"");
        assertThat(AstValueHelper.jsonStringify("reverse-solidus-\\")).isEqualTo("reverse-solidus-\\\\");
        assertThat(AstValueHelper.jsonStringify("solidus-/")).isEqualTo("solidus-\\/");
        assertThat(AstValueHelper.jsonStringify("backspace-\b")).isEqualTo("backspace-\\b");
        assertThat(AstValueHelper.jsonStringify("formfeed-\f")).isEqualTo("formfeed-\\f");
        assertThat(AstValueHelper.jsonStringify("newline-\n")).isEqualTo("newline-\\n");
        assertThat(AstValueHelper.jsonStringify("carriage-return-\r")).isEqualTo("carriage-return-\\r");
        assertThat(AstValueHelper.jsonStringify("horizontal-tab-\t")).isEqualTo("horizontal-tab-\\t");

        // this is some AST from issue 1105
        assertThat(AstValueHelper.jsonStringify("{\"operator\":\"eq\", \"operands\": []}"))
            .isEqualTo("{\\\"operator\\\":\\\"eq\\\", \\\"operands\\\": []}");
    }
}
