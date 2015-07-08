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
import io.vertx.ext.auth.User;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RouteHandler("/api")
public class SecurityHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityHandler.class);

    @RouteMapping(value = "/permission", method = RouteMethod.POST)
    public Handler<RoutingContext> permissions() {
        return ctx -> {
            JDBCClient client = AppUtil.getJdbcClient(Vertx.vertx());
            client.getConnection(conn -> {
                if (conn.failed()) {
                    LOGGER.error(conn.cause().getMessage(), conn.cause());
                    ctx.fail(400);
                }

                User user = ctx.user();
                if (user == null) {
                    LOGGER.error("Error, no user");
                    ctx.fail(401);
                }
                SQLUtil.query(conn.result(), "select a.perm from roles_perms a join user_roles b on a.role = b.role where b.username = ?", new JsonArray().add(user.principal().getString("username")), rs -> {
                    JsonArray permissions = new JsonArray();
                    for (JsonObject permission : rs.getRows()) {
                        permissions.add(permission);
                    }
                    ctx.response().end(permissions.encode());
                });
            });
        };
    }

}
