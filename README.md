GraphQL Query for JPA Entity Model
===============

This library uses [graphql-java v3.0](https://github.com/andimarek/graphql-java) to derive and build the GraphQL schema from JPA Entity Schema provided by entity classes. 

It implements a schema builder to generate GraphQL Schema using JPA EntityManager with JPA Query Data Fetchers that transform GraphQL queries into JPA queries with flexible type safe criteria expressions and user-friendly SQL query syntax semantics i.e. query by page, , where criteria expressions, select, order by etc.

Modules
-------
The GraphQL-JPA-Query library consists of the following modules:

1. `graphql-jpa-query-annotations` - Provides annotations for instrumenting your entity models with GraphQL Schema Descriptions
2. `graphql-jpa-query-schema` - Provides interface specifications and implementation of the JPA Schema Builder and JPA Data Fetchers
3. `graphql-jpa-query-boot-starter`- Provides Spring Boot auto-configuration support to enable GraphQL JPA Query in your project
4. `graphql-jpa-query-example` - Provides example application for Books entity models

Building with jitpack.io [![Release](https://jitpack.io/v/introproventures/graphql-jpa-query.svg)](https://jitpack.io/#introproventures/graphql-jpa-query)
------------------------
You can simply use jitpack.io to include and build individual modules in your project. You will need to add jitpack.io repository in our project to resolve required artifacts using valid release tag.

      <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
      </repositories>

For GraphQL JPA Annotations use:

    <dependency>
	    <groupId>com.github.introproventures.graphql-jpa-query</groupId>
	    <artifactId>graphql-jpa-query-annotations</artifactId>
	    <version>v0.1.0</version>
    </dependency>

For GraphQL JPA Schema Builder use:

    <dependency>
	    <groupId>com.github.introproventures.graphql-jpa-query</groupId>
	    <artifactId>graphql-jpa-query-schema</artifactId>
	    <version>v0.1.0</version>
    </dependency>

For GraphQL JPA Query Boot Starter use:

    <dependency>
	    <groupId>com.github.introproventures.graphql-jpa-query</groupId>
	    <artifactId>graphql-jpa-query-boot-starter</artifactId>
	    <version>v0.1.0</version>
    </dependency>

Other Dependencies
-----------------
The core library module `graphql-jpa-query-schema` keeps dependencies to a minimum. The main dependecies for schema module are `graphql-java`, `evo-inflector`, `javax.transaction-api`, `hibernate-jpa-2.1-api`. The tests depend on Spring Boot 1.5 with Web and Hibernate for JPA starters.  

Schema Generation
-----------------
The models are introspected using a JPA Entity Manager to auto-generate a GraphQL Schema. After that, you can use GraphQL schema to execute GraphQL query against your data.

Schema Documentation
--------------------
GraphQL provides a well documented schema for your domain entity model.  The Schema Builder produces
descriptions using `@GraphQLDescription` annotation on Java types and fields. These descriptions will show up in the GraphiQL schema browser to help you provide documented API to end-users.  See the GraphiQL section below for more details. You can use  `@GraphQLIgnore` annotation to exclude entity type or field from schema.

Queries
--------------
This library will wrap each entity into two query fields for each entity model (say Human or Droid - see tests) will have two representations in the generated schema:

- One that models the Entity directly using singular form, i.e. Human or Droid to get single instance by id.
- One that wraps the Entity in a pagable query request with where criteria expression using Entity pluralized form, i.e. Humans or Droids

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

Examples
--------

GraphiQL (https://github.com/graphql/graphiql) can be used for simple testing. You can build and launch provided example as a Spring Boot Application, then navigate to http://localhost:8080/ to load GraphiQL browser. The collapsed Docs panel can opened by clicking on the button in the upper right corner to expose current test schema models.

You can run GraphQL queries in the left pannel. Type the query and hit the run button. The results should come up in the middle
panel. If your query has variables, there is a minimized panel at the bottom left.  Simply click on this to expand, and
type in your variables as a JSON string with quoted keys.

License
-------
Apache License v2.0
