package com.introproventures.graphql.jpa.query.graphiql;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

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
