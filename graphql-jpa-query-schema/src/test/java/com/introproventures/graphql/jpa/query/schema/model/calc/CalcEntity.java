package com.introproventures.graphql.jpa.query.schema.model.calc;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;
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
    boolean logic = true;

    @Transient
    @GraphQLDescription("i desc member")
    String fieldMem = "member";

    @Transient
    @GraphQLIgnore
    String hideField = "hideField";

    @Transient
    @GraphQLDescription("i desc function")
    public String getFieldFun() {
        return title + " function";
    }

    @Transient
    public boolean isCustomLogic() {
        return false;
    }

    public String getHideFieldFunction() {
        return "getHideFieldFunction";
    }
}
