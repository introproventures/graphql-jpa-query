package com.introproventures.graphql.jpa.query;

import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource({"classpath:hibernate.properties"})
public abstract class AbstractSpringBootTestSupport {

}
