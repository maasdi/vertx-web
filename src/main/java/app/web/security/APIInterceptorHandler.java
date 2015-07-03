package app.web.security;

import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;

public class APIInterceptorHandler extends AuthHandlerImpl {

    public APIInterceptorHandler(AuthProvider authProvider) {
        super(authProvider);
    }

    @Override
    public void handle(RoutingContext context) {
        Session session = context.session();
        if (session != null) {
            User user = context.user();
            if (user != null) {
                authorise(user, context);
            } else {
                context.response().setStatusCode(401).end(); // Unauthorized
            }
        } else {
            context.fail(new NullPointerException("No session.."));
        }
    }
}
