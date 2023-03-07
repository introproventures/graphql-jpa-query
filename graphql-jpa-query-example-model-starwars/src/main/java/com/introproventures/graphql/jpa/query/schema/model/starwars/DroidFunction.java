package com.introproventures.graphql.jpa.query.schema.model.starwars;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Entity
@Table(name = "droid_function")
@GraphQLDescription("Represents the functions a droid can have")
@Data
@EqualsAndHashCode()
public class DroidFunction {

    @Id
    @GraphQLDescription("Primary Key for the DroidFunction Class")
    String id;
    
    String function;
    
    

}
