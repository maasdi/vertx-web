package app.web;

import io.vertx.core.http.HttpClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHandlerTest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginHandlerTest.class);

    @Test
    public void testUnautorized(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.getNow(port, host, "/api/users", resp -> {
            try {
                context.assertEquals(401, resp.statusCode());
            } finally {
                client.close();
                async.complete();
            }
        });
    }

    @Test
    public void testLogin(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.post(port, host, "/login", resp -> {
            resp.bodyHandler(body -> {
                try {
                    context.assertEquals("{\"username\":\"admin\"}", body.toString());
                    context.assertEquals(200, resp.statusCode());
                } finally {
                    client.close();
                    async.complete();
                }
                });
            })
            .end("{\"username\":\"admin\",\"password\":\"password\"}");
        }

    }
