package gatling;

import static gatling.PerfTestConfig.BASE_URL;
import static gatling.PerfTestConfig.DURATION_SEC;
import static gatling.PerfTestConfig.P95_RESPONSE_TIME_MS;
import static gatling.PerfTestConfig.REQUEST_PER_SECOND;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class TestProcessVariablesSimulation extends Simulation {

    private Integer pages = 9;
    private Integer limit = 100;

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .header("Content-Type", "application/json")
        .check(status().is(200));

    ChainBuilder processVariables = repeat(pages, "i")
        .on(
            exec(
                http("query(ProcessInstances.variables(${i}))")
                    .post("/graphql")
                    .body(
                        StringBody(
                            """
                          {
                            "query": "{ ProcessInstances(where: {businessKey: {EQ: \\"232951752337576\\"}}) { select { businessKey variables(where: {name: {EQ: \\"applicationDate\\"}}) { name value }}}}"
                          }
                        """
                        )
                    )
                    .check(jsonPath("$..errors").notExists())
                    .check(jsonPath("$..data.ProcessInstances.select[0].variables[0].name").exists())
                    .check(bodyString().saveAs("response"))
            )
        );

    ScenarioBuilder scn = scenario("Query Process Variables").exec(processVariables);

    {
        setUp(scn.injectOpen(constantUsersPerSec(REQUEST_PER_SECOND).during(DURATION_SEC)))
            .protocols(httpProtocol)
            .assertions(
                global().responseTime().percentile3().lt(P95_RESPONSE_TIME_MS),
                global().successfulRequests().percent().is(100.0)
            );
    }
}
