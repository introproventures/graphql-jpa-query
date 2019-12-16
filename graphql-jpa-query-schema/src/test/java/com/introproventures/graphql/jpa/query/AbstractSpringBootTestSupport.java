package com.introproventures.graphql.jpa.query;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:hibernate.properties"})
@AutoConfigureTestDatabase(replace = Replace.ANY)
public abstract class AbstractSpringBootTestSupport {

}
