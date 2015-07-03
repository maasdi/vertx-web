package app.web;

import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHandlerTest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHandlerTest.class);

    @Test
    public void testList(TestContext context) {
        FakeServer.loggedIn();

        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.getNow(port, "localhost", "/api/users", resp -> {
            resp.bodyHandler(body -> {
                try {
                    context.assertEquals(200, resp.statusCode());
                } finally {
                    client.close();
                    async.complete();
                    FakeServer.loggedOut();
                }
            });
        });
    }

}
