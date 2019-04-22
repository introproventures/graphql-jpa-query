package com.introproventures.graphql.jpa.query.schema.model.starwars;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity(name = "droid_function")
@GraphQLDescription("Represents the functions a droid can have")
@Data
@EqualsAndHashCode()
public class DroidFunction {

    @Id
    @GraphQLDescription("Primary Key for the DroidFunction Class")
    String id;
    
    String function;
    
    

}
