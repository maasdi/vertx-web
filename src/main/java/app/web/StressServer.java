package app.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StressServer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(StressServer.class);

    private JDBCClient client;

    @Override
    public void start() throws Exception {

        LOGGER.debug("Start server");

        client = AppUtil.getJdbcClient(vertx);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.route("/api/users*").handler(routingContext -> client.getConnection(res -> {
            if (res.failed()) {
                routingContext.fail(res.cause());
            } else {
                SQLConnection conn = res.result();

                // save the connection on the context
                routingContext.put("conn", conn);

                // we need to return the connection back to the jdbc pool. In order to do that we need to close it, to keep
                // the remaining code readable one can add a headers end handler to close the connection.
                routingContext.addHeadersEndHandler(done -> conn.close(v -> {
                    if (v.failed()) {
                        done.fail(v.cause());
                    } else {
                        done.complete();
                    }
                }));

                routingContext.next();
            }
        })).failureHandler(routingContext -> {
            SQLConnection conn = routingContext.get("conn");
            if (conn != null) {
                conn.close(v -> {
                });
            }
        });

        router.get("/api/users").handler(this::list);
        router.post("/api/users").handler(this::add);
        router.get("/api/users/:username").handler(this::edit);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    public void list(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        SQLConnection conn = ctx.get("conn");

        conn.query("select username, first_name, last_name, address from user", query -> {
            if (query.failed()) {
                LOGGER.debug("Failed");
                ctx.response().setStatusCode(500).end();
            } else {
                JsonArray users = new JsonArray();
                query.result().getRows().forEach(users::add);
                ctx.response().end(users.encode());
            }
        });
    }

    public void add(RoutingContext ctx) {
        JsonObject user = ctx.getBodyAsJson();
        String username = user.getString("username");
        String password = user.getString("password");
        String firstName = user.getString("first_name");
        String lastName = user.getString("last_name");
        String address = user.getString("address");

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            LOGGER.error("Username and Password cannot be null");
            JsonObject error = new JsonObject();
            error.put("error", "Username and Password cannot be null");
            ctx.response().setStatusCode(205).end(error.encode());
        }

        String salt = AppUtil.computeHash(username, null, "SHA-512");
        String passwordHash = AppUtil.computeHash(password, salt, "SHA-512");

        SQLConnection conn = ctx.get("conn");

        conn.queryWithParams("select * from user where username = ?", new JsonArray().add(username), query -> {
            if (query.failed()) {
                ctx.response().setStatusCode(500).end();
            } else {
                if (query.result().getResults().size() >= 1) {
                    LOGGER.error("User with username {} already exists..", username);
                    JsonObject error = new JsonObject();
                    error.put("error", "User with username " + username+ " already exists");
                    ctx.response().setStatusCode(205).end(error.encode());
                }

                JsonArray params = new JsonArray();
                params.add(username).add(passwordHash).add(salt).add(firstName).add(lastName).add(address);
                conn.updateWithParams("insert into user (username, password, password_salt, first_name, last_name, address) values (?, ?, ?, ?, ?, ?)", params, update -> {
                    if (update.failed()) {
                        ctx.response().setStatusCode(500).end();
                    } else {
                        conn.queryWithParams("select username, first_name, last_name, address from user where username = ?", new JsonArray().add(username), query2 -> {
                            if (query2.failed()) {
                                ctx.response().setStatusCode(500).end();
                            } else {
                                ctx.response().end(query2.result().getRows().get(0).encode());
                            }
                        });
                    }
                });
            }
        });
    };

    public void edit(RoutingContext ctx) {
        String username = ctx.request().getParam("username");
        if (StringUtils.isBlank(username)) {
            LOGGER.error("Username is blank");
            ctx.fail(404);
        }

        SQLConnection conn = ctx.get("conn");

        conn.queryWithParams("select username, first_name, last_name, address from user where username = ?", new JsonArray().add(username), query -> {
            if (query.failed()) {
                ctx.response().setStatusCode(500).end();
            } else {
                if (query.result().getRows().size() == 1) {
                    ctx.response().end(query.result().getRows().get(0).encode());
                } else {
                    JsonObject error = new JsonObject();
                    error.put("error", "Record not found");
                    ctx.response().setStatusCode(205).end(error.encode());
                }
            }
        });
    };
}
