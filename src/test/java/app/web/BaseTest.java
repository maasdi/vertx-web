package app.web;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RunWith(VertxUnitRunner.class)
public abstract class BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

    protected Integer port = AppUtil.configInt("port") == null ? 8080 : AppUtil.configInt("port");

    protected Vertx vertx;

    @Before
    public void before(TestContext context) {
        try {
            // Quick initialized test data
            setUpInitialData(AppUtil.configStr("db.url"));

            vertx = Vertx.vertx();
            vertx.deployVerticle(FakeServer.class.getName(), context.asyncAssertSuccess());

            onBefore(context);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * To be override by subclass
     * @param context
     */
    protected void onBefore(TestContext context) {}

    @After
    public void after(TestContext context) {
        onAfter(context);

        vertx.close(context.asyncAssertSuccess());
    }

    /**
     * To be override by subclass
     * @param context
     */
    protected void onAfter(TestContext context) {}

    private Connection conn;

    private void setUpInitialData(String url) throws SQLException {
        conn = DriverManager.getConnection(url);
        executeStatement("drop table if exists user;");
        executeStatement("drop table if exists user_roles;");
        executeStatement("drop table if exists roles_perms;");
        executeStatement("create table user (username varchar(255), password varchar(255), password_salt varchar(255) );");
        executeStatement("create table user_roles (username varchar(255), role varchar(255));");
        executeStatement("create table roles_perms (role varchar(255), perm varchar(255));");

        executeStatement("insert into user values ('tim', 'EC0D6302E35B7E792DF9DA4A5FE0DB3B90FCAB65A6215215771BF96D498A01DA8234769E1CE8269A105E9112F374FDAB2158E7DA58CDC1348A732351C38E12A0', 'C59EB438D1E24CACA2B1A48BC129348589D49303858E493FBE906A9158B7D5DC');");
        executeStatement("insert into user_roles values ('tim', 'dev');");
        executeStatement("insert into user_roles values ('tim', 'admin');");
        executeStatement("insert into roles_perms values ('dev', 'commit_code');");
        executeStatement("insert into roles_perms values ('dev', 'eat_pizza');");
        executeStatement("insert into roles_perms values ('admin', 'merge_pr');");
    }

    private void executeStatement(String sql) throws SQLException {
        conn.createStatement().execute(sql);
    }

}
