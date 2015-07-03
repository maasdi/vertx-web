package app.web.handlers;

import app.web.annotations.RouteHandler;
import app.web.annotations.RouteMapping;
import app.web.annotations.RouteMethod;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

@RouteHandler("/api/users")
public class UserHandler {

    // sample
    private final JsonArray users = new JsonArray();

    private int counter = 1;

    public UserHandler() {
        init();
    }

    @RouteMapping(method = RouteMethod.GET)
    public Handler<RoutingContext> list() {
        return ctx -> {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(users.encode());
        };
    }

    @RouteMapping(method = RouteMethod.POST)
    public Handler<RoutingContext> add() {
        return ctx -> {
            JsonObject newUser = ctx.getBodyAsJson();
            newUser.put("id", counter++);
            users.add(newUser);

            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(newUser.encode());
        };
    }

    @RouteMapping(value = "/:id", method = RouteMethod.GET)
    public Handler<RoutingContext> edit() {
        return ctx -> {
                String param = ctx.request().getParam("id");
                if (param == null)
                    ctx.fail(404);

                Integer id = Integer.valueOf(param);
                for (JsonObject user : (List<JsonObject>) users.getList()) {
                    if (user.getInteger("id").equals(id)) {
                        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(user.encode());
                    }
                }
        };
    }

    @RouteMapping(value = "/:id", method = RouteMethod.PUT)
    public Handler<RoutingContext> update() {
        return ctx -> {
            String param = ctx.request().getParam("id");
            if (param == null)
                ctx.fail(404);

            JsonObject updateUser = ctx.getBodyAsJson();
            Integer id = Integer.valueOf(param);
            for (JsonObject user : (List<JsonObject>) users.getList()) {
                if (user.getInteger("id").equals(id)) {
                    users.remove(user);
                    users.add(updateUser);
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(updateUser.encode());
                }
            }
        };
    }

    @RouteMapping(value = "/:id", method = RouteMethod.DELETE)
    public Handler<RoutingContext> delete() {
        return ctx -> {
            String param = ctx.request().getParam("id");
            if (param == null)
                ctx.fail(404);

            Integer id = Integer.valueOf(param);
            JsonObject deleteUser = null;
            for (JsonObject user : (List<JsonObject>) users.getList()) {
                if (user.getInteger("id").equals(id)) {
                    deleteUser = user;
                    break;
                }
            }

            if (deleteUser == null)
                ctx.fail(505);

            users.remove(deleteUser);
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end();
        };
    }

    private void init() {
        // sample data, will update later
        JsonObject user = new JsonObject();
        users.add(user);
        user.put("id", counter++);
        user.put("username", "maasdianto");
        user.put("firstName", "Maas");
        user.put("lastName", "Dianto");
        user.put("address", "15A-32-2 Mont Kiara Pines Condominium");

        JsonObject user2 = new JsonObject();
        users.add(user2);
        user2.put("id", counter++);
        user2.put("username", "jhon");
        user2.put("firstName", "Jhon");
        user2.put("lastName", "Doe");
        user2.put("address", "Kuala Lumpur");
    }
}
