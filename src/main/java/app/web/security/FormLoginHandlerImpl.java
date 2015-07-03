package app.web.security;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.FormLoginHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormLoginHandlerImpl implements FormLoginHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormLoginHandlerImpl.class);

    private final AuthProvider authProvider;

    private String usernameParam;
    private String passwordParam;
    private String returnURLParam;
    private String directLoggedInOKURL;

    @Override
    public FormLoginHandler setUsernameParam(String usernameParam) {
        this.usernameParam = usernameParam;
        return this;
    }

    @Override
    public FormLoginHandler setPasswordParam(String passwordParam) {
        this.passwordParam = passwordParam;
        return this;
    }

    @Override
    public FormLoginHandler setReturnURLParam(String returnURLParam) {
        this.returnURLParam = returnURLParam;
        return this;
    }

    @Override
    public FormLoginHandler setDirectLoggedInOKURL(String directLoggedInOKURL) {
        this.directLoggedInOKURL = directLoggedInOKURL;
        return this;
    }

    public FormLoginHandlerImpl(AuthProvider authProvider) {
        this(authProvider, "username", "password", null, null);
    }

    public FormLoginHandlerImpl(AuthProvider authProvider, String usernameParam, String passwordParam,
                                String returnURLParam, String directLoggedInOKURL) {
        this.authProvider = authProvider;
        this.usernameParam = usernameParam;
        this.passwordParam = passwordParam;
        this.returnURLParam = returnURLParam;
        this.directLoggedInOKURL = directLoggedInOKURL;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest req = context.request();
        if (req.method() != HttpMethod.POST) {
            context.fail(405); // Must be a POST
        } else {
            JsonObject params = context.getBodyAsJson();
            String username = params.getString(usernameParam);
            String password = params.getString(passwordParam);
            if (username == null || password == null) {
                LOGGER.warn("No username or password provided in form - did you forget to include a BodyHandler?");
                context.fail(400);
            } else {
                Session session = context.session();
                JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
                authProvider.authenticate(authInfo, res -> {
                    if (res.succeeded()) {
                        User user = res.result();
                        context.setUser(user);
                        req.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(user.principal().encode());
                    } else {
                        context.fail(403);  // Failed login
                    }
                });
            }
        }
    }

    private void doRedirect(HttpServerResponse response, String url) {
        response.putHeader("location", url).setStatusCode(302).end();
    }

}
