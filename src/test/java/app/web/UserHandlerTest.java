package app.web;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHandlerTest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHandlerTest.class);

    @Override
    protected void onBefore(TestContext context) {
        FakeServer.loggedIn();
    }

    @Override
    protected void onAfter(TestContext context) {
        FakeServer.loggedOut();
    }

    @Test
    public void testList(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.getNow(port, host, "/api/users", resp -> {
            resp.bodyHandler(body -> {
                try {
                    context.assertEquals(200, resp.statusCode());
                    context.assertNotNull(body.toString());
                } finally {
                    client.close();
                    async.complete();
                }
            });
        });
    }

    @Test
    public void testGetById(TestContext context) {

        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        String username = "jhon"; // make sure there is jhon in db
        client.getNow(port, host, "/api/users/" + username, resp -> {
           resp.bodyHandler(body -> {
              try {
                  context.assertEquals(200, resp.statusCode());
                  context.assertTrue(body.toString().contains(username));
              } finally {
                  client.close();
                  async.complete();
              }
           });
        });
    }

    @Test
    public void testAddUser(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.post(port, host, "/api/users", resp -> {
            resp.bodyHandler(body -> {
                try {
                    context.assertEquals(200, resp.statusCode());
                    context.assertTrue(body.toString().contains("test_user"));
                } finally {
                    client.close();
                    async.complete();
                }
            });
        })
         .end("{\"username\": \"test_user\",\"password\":\"test_pass\", \"first_name\":\"Test\", \"last_name\":\"User\", \"address\" : \"Test address\"}");
    }

    @Test
    public void testUpdate(TestContext context) {

        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        String username = "jhon";
        client.put(port, host, "/api/users/" + username, resp -> {
            resp.bodyHandler(body -> {
                try {
                    context.assertEquals(200, resp.statusCode());
                    context.assertTrue(body.toString().contains("Doe Update"));
                } finally {
                    client.close();
                    async.complete();
                }
            });
        }).end("{\"username\": \"jhon\", \"first_name\":\"Jhon\", \"last_name\":\"Doe Update\", \"address\" : \"Update address\"}");
        }

    @Test
    public void testDelete(TestContext context) {

        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        String username = "jhon";
        client.delete(port, host, "/api/users/" + username, resp -> {
            try {
                context.assertEquals(200, resp.statusCode());
            } finally {
                client.close();
                async.complete();
            }
            }).end();
        }

    }
