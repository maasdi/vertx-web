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

@RouteHandler("/api/items")
public class ItemsHandler {

    // sample
    private static final JsonArray items = new JsonArray();

    static {
        JsonObject item = new JsonObject();
        item.put("id", 1);
        item.put("name", "Item One");
        item.put("description", "Item One Desc");
        items.add(item);
    }

    @RouteMapping(method = RouteMethod.GET)
    public Handler<RoutingContext> getItems() {
        return ctx -> {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(items.encode());
        };
    }

}
