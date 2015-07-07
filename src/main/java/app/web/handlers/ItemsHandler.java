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

import java.util.List;

@RouteHandler("/api/items")
public class ItemsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemsHandler.class);

    @RouteMapping(method = RouteMethod.GET)
    public Handler<RoutingContext> getAll() {
        return ctx -> {
            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());

            client.getConnection(conn -> {
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(400);
                }

                SQLUtil.query(conn.result(), "select id, title, description from item", rs -> {
                    JsonArray items = new JsonArray();
                    for (JsonObject row : rs.getRows()) {
                        items.add(row);
                    }

                    SQLUtil.close(conn.result());
                    ctx.response().end(items.encode());
                });
            });
        };
    }

    @RouteMapping(value = "/:id", method = RouteMethod.GET)
    public Handler<RoutingContext> fetch() {
        return ctx -> {
            String id = ctx.request().getParam("id");
            if (StringUtils.isBlank(id)) {
                LOGGER.error("ID is blank");
                JsonObject error = new JsonObject();
                error.put("error", "ID should not be blank");
                ctx.response().setStatusCode(205).end(error.encode());
            }

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(400);
                }

                SQLUtil.query(conn.result(), "select id, title, description from item where id = ?", new JsonArray().add(Integer.valueOf(id)), rs -> {
                    SQLUtil.close(conn.result());
                    if (rs.getRows().size() == 1) {
                        ctx.response().end(rs.getRows().get(0).encode());
                    } else {
                        JsonObject error = new JsonObject();
                        error.put("error", "Record not found");
                        ctx.response().setStatusCode(205).end(error.encode());
                    }
                });
            });
        };
    }

    @RouteMapping(method = RouteMethod.POST)
    public Handler<RoutingContext> create() {
        return ctx -> {
            JsonObject item = ctx.getBodyAsJson();
            String title = item.getString("title");
            String desc = item.getString("description");
            if (StringUtils.isBlank(title) || StringUtils.isBlank(desc)) {
                JsonObject error = new JsonObject();
                error.put("error", "Title and description should not be balnk");
                ctx.response().setStatusCode(205).end(error.encode());
            }

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {
               if (conn.failed()) {
                   LOGGER.error(conn.cause().getMessage(), conn.cause());
                   ctx.fail(400);
               }

                JsonArray params = new JsonArray();
                params.add(title).add(desc);
                SQLUtil.update(conn.result(), "insert into item (title, description) values (?, ?)", params, res -> {
                    SQLUtil.close(conn.result());
                    ctx.response().end();
                });
            });
        };
    };

    @RouteMapping(value = "/:id", method = RouteMethod.PUT)
    public Handler<RoutingContext> update() {
        return ctx -> {
            JsonObject item = ctx.getBodyAsJson();
            Integer id = item.getInteger("id");
            if (id == null) {
                LOGGER.error("ID is blank");
                JsonObject error = new JsonObject();
                error.put("error", "ID cannot be blank");
                ctx.response().setStatusCode(205).end(error.encode());
            }

            String title = item.getString("title");
            String desc = item.getString("description");
            if (StringUtils.isBlank(title) || StringUtils.isBlank(desc)) {
                JsonObject error = new JsonObject();
                error.put("error", "Title and description should not be balnk");
                ctx.response().setStatusCode(205).end(error.encode());
            }

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(400);
                }

                JsonArray params = new JsonArray();
                params.add(title).add(desc).add(id);
                SQLUtil.update(conn.result(), "update item set title = ?, description = ? where id = ?", params, res -> {
                    SQLUtil.query(conn.result(), "select id, title, description from item where id = ?", new JsonArray().add(Integer.valueOf(id)), rs -> {
                        SQLUtil.close(conn.result());
                        ctx.response().end(rs.getRows().get(0).encode());
                    });
                });
            });
        };
    }

    @RouteMapping(value = "/:id", method = RouteMethod.DELETE)
    public Handler<RoutingContext> destroy() {
        return ctx -> {
            String id = ctx.request().getParam("id");
            if (StringUtils.isBlank(id)) {
                LOGGER.error("ID is blank");
                JsonObject error = new JsonObject();
                error.put("error", "ID cannot be blank");
                ctx.response().setStatusCode(205).end(error.encode());
            }

            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(400);
                }

                JsonArray params = new JsonArray();
                params.add(Integer.valueOf(id));
                SQLUtil.update(conn.result(), "delete from item where id = ?", params, res -> {
                    SQLUtil.close(conn.result());
                    ctx.response().end();
                });
            });
        };
    }

}
