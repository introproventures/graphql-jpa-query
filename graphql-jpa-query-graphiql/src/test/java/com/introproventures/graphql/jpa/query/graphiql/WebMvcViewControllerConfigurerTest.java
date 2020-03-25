package com.introproventures.graphql.jpa.query.graphiql;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WebMvcViewControllerConfigurerTest {
	
	@SpringBootApplication
	static class Application {
		
	}
	
	@Test
	public void contextLoads() {
		// success
	}

}
