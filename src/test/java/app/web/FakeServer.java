package app.web;

import app.web.security.APIInterceptorHandler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;

/**
 * An extended of actual server, to override default behaviour
 * for testing purposes.
 */
public class FakeServer extends Server {

    private static Boolean auth = Boolean.TRUE;

    @Override
    protected AuthHandler getAPIInterceptorHandler() {
        return new TestAPIInterceptorHandler(getAuthProvider());
    }

    // Set to FALSE for testing
    public static void loggedIn() {
        auth = Boolean.FALSE;
    }

    public static void loggedOut() {
        auth = Boolean.TRUE;
    }

    private class TestAPIInterceptorHandler extends APIInterceptorHandler {

        public TestAPIInterceptorHandler(AuthProvider authProvider) {
            super(authProvider);
        }

        @Override
        public void handle(RoutingContext context) {
            if (auth) {
                super.handle(context);
            } else {
                // No auth required
                context.next();
            }
        }
    }
}
