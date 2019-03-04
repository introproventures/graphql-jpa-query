package com.introproventures.graphql.jpa.query.schema.model.calc;

import com.introproventures.graphql.jpa.query.annotation.GraphQLCalcField;
import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Data
@Entity
public class CalcEntity {
    @Id
    Long id;

    String title;

    String info;

    @Transient
    @GraphQLCalcField
    boolean logic = true;

    @Transient
    @GraphQLCalcField
    @GraphQLDescription("i desc member")
    String fieldMem = "member";


    @GraphQLCalcField
    @GraphQLDescription("i desc function")
    public String getFieldFun() {
        return title + " function";
    }

    @Transient
    @GraphQLCalcField
    public boolean isCustomLogic() {
        return false;
    }
}
