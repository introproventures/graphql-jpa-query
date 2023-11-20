package gatling;

import static gatling.PerfTestConfig.BASE_URL;
import static gatling.PerfTestConfig.DURATION_SEC;
import static gatling.PerfTestConfig.P95_RESPONSE_TIME_MS;
import static gatling.PerfTestConfig.REQUEST_PER_SECOND;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
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

public class TestTaskVariablesSimulation extends Simulation {

    private Integer pages = 9;
    private Integer limit = 100;

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .header("Content-Type", "application/json")
        .check(status().is(200));

    ChainBuilder taskVariables = repeat(pages, "i")
        .on(
            exec(session -> session.set("page", session.getInt("i") + 1).set("limit", limit))
                .exec(
                    http("query{Tasks(page:{start:${page},limit:${limit}})")
                        .post("/graphql")
                        .body(
                            StringBody(
                                """
                                  {
                                    "query": "query($page: Int!, $limit: Int!) {TaskVariables(page: {start: $page, limit: $limit}, where: {name: {EQ: \\"accountNumber\\"}}) {select {id,name,value(orderBy: ASC),taskId,task {assignee,name,description,completedTo,completedFrom,candidateUsers: taskCandidateUsers {userId},candidateGroups: taskCandidateGroups {groupId},variables {name,value,type},accountNumber: variables(where: {name: {EQ: \\"accountNumber\\"}}) {value},processVariables: processInstance {variables {name,value,type}}}}}}",
                                    "variables": {"page": ${page}, "limit": ${limit}}
                                  }
                                """
                            )
                        )
                        .header("Content-Type", "application/json")
                        .check(status().is(200))
                        .check(jsonPath("$..errors").notExists())
                )
        )
        .exitHereIfFailed()
        .pause(1);

    ScenarioBuilder scn = scenario("Query Task Variables").exec(taskVariables);

    {
        setUp(scn.injectOpen(constantUsersPerSec(REQUEST_PER_SECOND).during(DURATION_SEC)))
            .protocols(httpProtocol)
            .assertions(
                global().responseTime().percentile3().lt(P95_RESPONSE_TIME_MS),
                global().successfulRequests().percent().is(100.0)
            );
    }
}
