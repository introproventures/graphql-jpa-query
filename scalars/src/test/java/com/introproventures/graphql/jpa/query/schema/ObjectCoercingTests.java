package com.introproventures.graphql.jpa.query.schema;

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
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.schema.Coercing;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ObjectCoercingTests {

    private Map<String, Object> variables = Collections.singletonMap("varRef1", "value1");

    private Coercing<?, ?> coercing = new JavaScalars.GraphQLObjectCoercing();

    @SuppressWarnings("serial")
    @Test
    public void testASTParsing() {
        // when
        assertThat(coercing.parseLiteral(mkStringValue("s"), variables)).isEqualTo("s");
        assertThat(coercing.parseLiteral(mkFloatValue("99.9"), variables)).isEqualTo(new BigDecimal("99.9"));
        assertThat(coercing.parseLiteral(mkIntValue(BigInteger.valueOf(666)), variables))
            .isEqualTo(BigInteger.valueOf(666));
        assertThat(coercing.parseLiteral(mkBooleanValue(true), variables)).isEqualTo(true);
        assertThat(coercing.parseLiteral(mkNullValue(), variables)).isEqualTo(null);
        assertThat(coercing.parseLiteral(mkVarRef("varRef1"), variables)).isEqualTo("value1");
        assertThat(
            coercing.parseLiteral(
                mkArrayValue(
                    new ArrayList<Value>() {
                        {
                            add(mkStringValue("s"));
                            add(mkIntValue(BigInteger.valueOf(666)));
                        }
                    }
                ),
                variables
            )
        )
            .asList()
            .containsExactly("s", BigInteger.valueOf(666));
    }

    @SuppressWarnings({ "serial", "rawtypes" })
    @Test
    public void testASTObjectParsing() {
        Map<String, Value> input = new LinkedHashMap<String, Value>();

        input.put("fld1", mkStringValue("s"));
        input.put("fld2", mkIntValue(BigInteger.valueOf(666)));
        input.put(
            "fld3",
            mkObjectValue(
                new LinkedHashMap<String, Value>() {
                    {
                        put("childFld1", mkStringValue("child1"));
                        put("childFl2", mkVarRef("varRef1"));
                    }
                }
            )
        );

        Map<String, Object> expected = new LinkedHashMap<String, Object>();

        expected.put("fld1", "s");
        expected.put("fld2", BigInteger.valueOf(666));
        expected.put(
            "fld3",
            new LinkedHashMap<String, Object>() {
                {
                    put("childFld1", "child1");
                    put("childFl2", "value1");
                }
            }
        );

        assertThat(coercing.parseLiteral(mkObjectValue(input), variables)).isEqualTo(expected);
    }

    @Test
    public void testSerializeIsAlwaysInAndOut() {
        assertThat(coercing.serialize(666)).isEqualTo(666);
        assertThat(coercing.serialize("same")).isEqualTo("same");
    }

    @Test
    public void testParseValueIsAlwaysInAndOut() {
        assertThat(coercing.parseValue(666)).isEqualTo(666);
        assertThat(coercing.parseValue("same")).isEqualTo("same");
    }

    ObjectValue mkObjectValue(Map<String, Value> fields) {
        List<ObjectField> list = new ArrayList<>();

        for (String key : fields.keySet()) {
            list.add(new ObjectField(key, fields.get(key)));
        }
        return new ObjectValue(list);
    }

    VariableReference mkVarRef(String name) {
        return new VariableReference(name);
    }

    ArrayValue mkArrayValue(List<Value> values) {
        return new ArrayValue(values);
    }

    NullValue mkNullValue() {
        return NullValue.newNullValue().build();
    }

    EnumValue mkEnumValue(String val) {
        return new EnumValue(val);
    }

    BooleanValue mkBooleanValue(boolean val) {
        return new BooleanValue(val);
    }

    IntValue mkIntValue(BigInteger val) {
        return new IntValue(val);
    }

    FloatValue mkFloatValue(String val) {
        return new FloatValue(new BigDecimal(val));
    }

    StringValue mkStringValue(String val) {
        return new StringValue(val);
    }
}
