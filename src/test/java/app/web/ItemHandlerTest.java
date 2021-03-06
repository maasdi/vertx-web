package app.web;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class ItemHandlerTest extends BaseTest {

    @Override
    protected void onBefore(TestContext context) {
        FakeServer.loggedIn();
    }

    @Override
    protected void onAfter(TestContext context) {
        FakeServer.loggedOut();
    }

    @Test
    public void testAll(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.getNow(port, host, "/api/items", resp -> {
           try {
               context.assertEquals(200, resp.statusCode());
           } finally {
               client.close();
               async.complete();
           }
        });
    }

    @Test
    public void testCreate(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        client.post(port, host, "/api/items", resp -> {
            resp.bodyHandler(body -> {
                try {
                    context.assertEquals(200, resp.statusCode());
                } finally {
                    client.close();
                    async.complete();
                }
                });
        }).end("{\"title\" : \"Test Note\", \"description\": \"Note description here..\"}");
        }

    @Test
    public void testFetch(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        findItem(client, async, rs -> {
            try {
                context.assertTrue(rs.getRows().size() > 0);
                JsonObject row = rs.getRows().get(0);
                context.assertNotNull(row.getInteger("ID"));

                client.getNow(port, host, "/api/items/" + row.getInteger("ID"), resp -> {
                    resp.bodyHandler(body -> {
                        try {
                            context.assertEquals(200, resp.statusCode());
                            context.assertTrue(body.toString().contains(row.getString("TITLE")));
                        } finally {
                            client.close();
                            async.complete();
                        }
                    });
                });
            } catch (Exception e) {
                client.close();
                async.complete();
                context.fail(e);
            }
        });
    }

    @Test
    public void testUpdate(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        findItem(client, async, rs -> {
            try {
                context.assertTrue(rs.getRows().size() > 0);
                JsonObject row = rs.getRows().get(0);
                context.assertNotNull(row.getInteger("ID"));

                client.put(port, host, "/api/items/" + row.getInteger("ID"), resp -> {
                    resp.bodyHandler(body -> {
                        try {
                            context.assertEquals(200, resp.statusCode());
                            context.assertTrue(body.toString().contains("Description updated"));
                        } finally {
                            client.close();
                            async.complete();
                        }
                    });
                }).end("{\"id\":\""+row.getInteger("ID")+"\", \"title\" : \"note title update\", \"description\" : \"Description updated\"}");
            } catch (Exception e){
                client.close();
                async.complete();
                context.fail(e);
            }
        });
        }

    @Test
    public void testDestroy(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        findItem(client, async, rs -> {
            try {
                context.assertTrue(rs.getRows().size() > 0);
                JsonObject row = rs.getRows().get(0);
                context.assertNotNull(row.getInteger("ID"));

                client.delete(port, host, "/api/items/" + row.getInteger("ID"), resp -> {
                    try {
                        context.assertEquals(200, resp.statusCode());
                    } finally {
                        client.close();
                        async.complete();
                    }
                }).end();
            } catch (Exception e) {
                client.close();
                async.complete();
                context.fail(e);
            }
        });
        }

    private void findItem(HttpClient client, Async async, Handler<ResultSet> done) {
        JDBCClient jdbcClient = AppUtil.getJdbcClient(vertx);
        jdbcClient.getConnection(conn -> {
            if (conn.failed()) {
                client.close();
                async.complete();
            }

            SQLUtil.query(conn.result(), "select * from item", done);
        });
    }
    }
