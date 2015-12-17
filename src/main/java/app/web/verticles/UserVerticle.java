package app.web.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserVerticle.class);

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer("user.all", res -> {
            LOGGER.debug("consume user.all with body {}", res.body());
        });
    }
}
