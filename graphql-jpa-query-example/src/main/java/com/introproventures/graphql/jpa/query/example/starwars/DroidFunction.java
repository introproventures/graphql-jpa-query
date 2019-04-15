package com.introproventures.graphql.jpa.query.example.starwars;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Entity(name = "droidFunction")
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
