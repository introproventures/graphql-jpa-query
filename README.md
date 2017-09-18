GraphQL Query for JPA Entity Model
===============
This library uses [graphql-java v3.0](https://github.com/andimarek/graphql-java) to derive and build the GraphQL schema from JPA Entity Schema provided by entity classes. 

It implements a schema builder to generate GraphQL Schema using JPA EntityManager and an JPA Data Fetchers to transform GraphQL queries into JPA queries with flexible type safe criteria expressions and user-friendly SQL query syntax semantics i.e. query by page, , where criteria expressions, select, order by etc.

Modules
-------
The GraphQL-JPA-Query library consists of the following modules:

1. GraphQL JPA Query Annotations - Provides annotations for instrumenting your entity models with GraphQL Schema Descriptions
2. GraphQL JPA Query Schema - Provides interface specifications and implementation of the JPA schema builder and JPA data fetchers
3. GraphQL JPA Query Web - Provides web interface endpoint for executing queries via HTTP
4. GraphQL JPA Query Spring Boot Starter - Provides Spring Boot auto-configuration support to enable GraphQL JPA Query in your project

Dependencies
-----------------
The library tries to keep the following dependencies: graphql-java, and some javax annotation packages. The tests depend
on Spring Boot with Hibernate for JPA.  

Schema Generation
-----------------
The models are introspected using a JPA Entity Manager to auto-generate a GraphQL Schema. After that, you can use GraphQL schema to query your data.

Schema Documentation
--------------------
GraphQL provides a well documented schema for your domain entity model.  The Schema Builder produces
descriptions using `@GraphQLDescription` annotation on Java types and fields. These descriptions will show up in the GraphiQL schema browser to help you provide documented API to end-users.  See the GraphiQL section below for more details. You can use  `@GraphQLIgnore` annotation to exclude entity type or field from schema.

Queries
--------------
This library will wrap each entity into two query fields:  
Each model (say Human or Droid - see tests) will have two representations in the generated schema:

- One that models the Entities directly using singular form, i.e. Human or Droid to get single instance by id.
- One that wraps the Entity in a pagable request with where criteria expression using Entity pluralized form, i.e. Humans or Droids

Singular Query Wrapper
--------------
You can use simple query, if you need a single object as root of your query. 

For Example:

    query {
      Human(id: 1) { name }
    }

Will return:

    Human: {
      name: "Luke Skywalker"
    }
    
Query Wrapper with Where Criteria Expressions
-------------------------------------
This library supports flexible type safe criteria expressions with user-friendly SQL query syntax semantics using `where` arguments and `select` field to specify the entity graph query with entiy attribute names as a combination of logical expressions like OR, AND, EQ, NE, GT, GE, LT, LR, IN, NIN, IS_NULL, NOT_NULL.

For Example: 

    query {
      Humans(where: { 
        OR: {
          name: { LIKE: "Luke" }
          OR: {
            name: { LIKE: "Darth"}
          }
        }
      }) {
        select { name }
      }
    }

Will return:

    {
        Humans: {
            select: [
                { name: 'Luke Skywalker' },
                { name: 'Darth Vader' }
            ]
        }
    }

You can use familiar SQL criteria expressions to specify complex criterias to fetch your data from SQL database. If you omit, where argument, all entities will be returned.

Collection Filtering
--------------------
You can specify criteria expressions for one-to-many associations in order to further filter entity by collection attributes:

For Example:

    query {
      Humans {
        select { 
          name
          friends(where: {
            appearsIn: {
              IN: [A_NEW_HOPE]
            }
            name: {
              LIKE: "Han"
            }

          }) {
            id
            name
          }
        }
      }
    }
  
Will Return: 

    "Humans": {
        "select": [
          {
            "name": "Luke Skywalker",
            "friends": [
              {
                "id": "1002",
                "name": "Han Solo"
              }
            ]
          },
          {
            "name": "Darth Vader",
            "friends": []
          },
          {
            "name": "Han Solo",
            "friends": []
          },
          {
            "name": "Leia Organa",
            "friends": [
              {
                "id": "1002",
                "name": "Han Solo"
              }
            ]
          },
          {
            "name": "Wilhuff Tarkin",
            "friends": []
          }
        ]
      }
    
Reverse Query
-------------
You can execute an inverse query to fitler results with a join in many-to-one association with some limitations. If you do this, be aware that only static parameter binding are supported in `where` criteria expressions.

For Example: 

    query {
        Humans {
            select {
                name
                favoriteDroid(where: {appearsIn: {IN:[A_NEW_HOPE]}}) {
                	name
              	}
            }
        }
    }
    
Will Return:

    {
      "Humans": {
        "select": [
          {
            "name": "Luke Skywalker",
            "favoriteDroid": {
              "name": "C-3PO"
            }
          },
          {
            "name": "Darth Vader",
            "favoriteDroid": {
              "name": "R2-D2"
            }
          }
        ]
      }

Type Safe Arguments
-------------------
The JPA Schema builder will derive QraphQL scalar types from JPA model attributes. At runtime, it will validate provided values against the schema. Enum Java types are also translated to QraphQL Enum scalar type.

Pagination
----------
GraphQL does not specify any language or idioms for performing Pagination. This library provides support for pageable queries with `page` argument on pluralized query wrapper.

This allows you to query for the "Page" version of any Entity, and return page metadata i.e. pages and total records count with the select data.  

For example:

    query {
        Humans(page:{start:0, limit: 3}) {
            pages
            total
            select {
                name
            }
        }
    }

Will return:

    {
      "Humans": {
        "pages": 2,
        "total": 5,
        "select": [
          {
            "name": "Luke Skywalker"
          },
          {
            "name": "Darth Vader"
          },
          {
            "name": "Han Solo"
          }
        ]
      }
    
The JPA DataFetcher implementation will execute an extra query to get the total elements only if you have requested 'pages' or 'total' fields. 

Sorting
-------

Sorting is supported on any field.  Simply pass in an 'orderBy' argument with the value of ASC or DESC.  Here's an example
of sorting by name for Human objects. The default sort order can be specified using `GraphQLDefaultSort` annotation on entity field. If sort order is not specified and there is no field with default sort order provided, we will use field annotated with @Id to avoid paging confusions.

    query {
        Human {
            name(orderBy: DESC)
            homePlanet
        }
    }

Performance
-----------
The JPA DataFetcher implementation will attempt to build dynamic fetch graph in order to optimize query performance and avoid N+1 lazy loading.

GraphiQL Browser
--------

GraphiQL (https://github.com/graphql/graphiql) can be used for simple testing. You can launch provided example as Spring Boot Application, and navigate to http://localhost:8080/ to load GraphiQL browser.  The collapsed Docs panel can opened by clicking on the button in the upper right corner, that when expanded will show you the running test schema.

You can run GraphQL queries in the left pannel, and hit the run button, and the results should come back in the right
panel.  If your query has variables, there is a minimized panel at the bottom left.  Simply click on this to expand, and
type in your variables as a JSON string (don't forget to quote the keys!).

License
-------
Apache License v2.0
