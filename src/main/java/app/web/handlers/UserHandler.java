package app.web.handlers;

import app.web.AppUtil;
import app.web.SQLUtil;
import app.web.annotations.RouteHandler;
import app.web.annotations.RouteMapping;
import app.web.annotations.RouteMethod;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RouteHandler("/api/users")
public class UserHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);

    @RouteMapping(method = RouteMethod.GET)
    public Handler<RoutingContext> list() {
        return ctx -> {
            LOGGER.debug("Start get list");
            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());

            client.getConnection(conn -> {
                LOGGER.debug("Succeed {}", conn.succeeded());
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(400);
                }

                ctx.put("conn", conn.result());
                ctx.addHeadersEndHandler(done -> conn.result().close(close -> {
                    if (close.failed()) {
                        done.fail(close.cause());
                    } else {
                        done.complete();
                    }
                }));

                SQLUtil.query(conn.result(), "select username, first_name, last_name, address from user", rs -> {
                    JsonArray users = new JsonArray();
                    for (JsonObject result : rs.getRows()) {
                        users.add(result);
                    }

                    ctx.response().end(users.encode());
                });
            });
        };
    }

    @RouteMapping(method = RouteMethod.POST)
    public Handler<RoutingContext> add() {
        return ctx -> {
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

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());

            client.getConnection(conn -> {

                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(400);
                }

                ctx.put("conn", conn.result());
                ctx.addHeadersEndHandler(done -> conn.result().close(close -> {
                    if (close.failed()) {
                        done.fail(close.cause());
                    } else {
                        done.complete();
                    }
                }));

                SQLUtil.query(conn.result(), "select * from user where username = ?", new JsonArray().add(username), rs -> {
                    if (rs.getResults().size() >= 1) {
                        LOGGER.error("User with username {} already exists..", username);
                        JsonObject error = new JsonObject();
                        error.put("error", "User with username " + username+ " already exists");
                        ctx.response().setStatusCode(205).end(error.encode());
                    }

                    JsonArray params = new JsonArray();
                    params.add(username).add(passwordHash).add(salt).add(firstName).add(lastName).add(address);
                    SQLUtil.update(conn.result(), "insert into user (username, password, password_salt, first_name, last_name, address) values (?, ?, ?, ?, ?, ?)", params, insert -> {
                        SQLUtil.query(conn.result(), "select username, first_name, last_name, address from user where username = ?", new JsonArray().add(username), rs2 -> {
                            ctx.response().end(rs2.getRows().get(0).encode());
                        });
                    });
                });
            });
        };
    }

    @RouteMapping(value = "/:username", method = RouteMethod.GET)
    public Handler<RoutingContext> edit() {
        return ctx -> {
            String username = ctx.request().getParam("username");
            if (StringUtils.isBlank(username)) {
                LOGGER.error("Username is blank");
                ctx.fail(404);
            }

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {

                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(400);
                }

                ctx.put("conn", conn.result());
                ctx.addHeadersEndHandler(done -> conn.result().close(close -> {
                    if (close.failed()) {
                        done.fail(close.cause());
                    } else {
                        done.complete();
                    }
                }));

                SQLUtil.query(conn.result(), "select username, first_name, last_name, address from user where username = ?", new JsonArray().add(username), res -> {
                    if (res.getRows().size() == 1) {
                        ctx.response().end(res.getRows().get(0).encode());
                    } else {
                        JsonObject error = new JsonObject();
                        error.put("error", "Record not found");
                        ctx.response().setStatusCode(205).end(error.encode());
                    }
                });
            });
        };
    }

    @RouteMapping(value = "/:username", method = RouteMethod.PUT)
    public Handler<RoutingContext> update() {
        return ctx -> {
            JsonObject user = ctx.getBodyAsJson();
            String username = user.getString("username");
            if (StringUtils.isBlank(username)) {
                LOGGER.error("Username is blank");
                ctx.fail(404);
            }

            String firstName = user.getString("first_name");
            String lastName = user.getString("last_name");
            String address = user.getString("address");

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(404);
                }

                ctx.put("conn", conn.result());
                ctx.addHeadersEndHandler(done -> conn.result().close(close -> {
                    if (close.failed()) {
                        done.fail(close.cause());
                    } else {
                        done.complete();
                    }
                }));

                JsonArray params = new JsonArray();
                params.add(firstName).add(lastName).add(address).add(username);
                SQLUtil.update(conn.result(), "update user set first_name = ?, last_name = ?, address = ? where username = ?", params, res -> {
                    SQLUtil.query(conn.result(), "select username, first_name, last_name, address from user where username = ?", new JsonArray().add(username), rs -> {
                        ctx.response().end(rs.getRows().get(0).encode());
                    });
                });
            });
        };
    }

    @RouteMapping(value = "/:username", method = RouteMethod.DELETE)
    public Handler<RoutingContext> delete() {
        return ctx -> {
            String username = ctx.request().getParam("username");
            if (StringUtils.isBlank(username)) {
                LOGGER.error("Username is blank");
                ctx.fail(404);
            }

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(404);
                }

                ctx.put("conn", conn.result());
                ctx.addHeadersEndHandler(done -> conn.result().close(close -> {
                    if (close.failed()) {
                        done.fail(close.cause());
                    } else {
                        done.complete();
                    }
                }));

                SQLUtil.update(conn.result(), "delete from user where username = ?", new JsonArray().add(username), res -> {
                    ctx.response().end();
                });

            });
        };
    }

}
