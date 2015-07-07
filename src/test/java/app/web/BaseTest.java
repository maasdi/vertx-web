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

    protected String host = AppUtil.configStr("host") == null ? "localhost" : AppUtil.configStr("host");

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
        executeStatement("create table user (username varchar(255), password varchar(255), password_salt varchar(255), first_name varchar(50), last_name varchar(50), address varchar(255) );");
        executeStatement("create table user_roles (username varchar(255), role varchar(255));");
        executeStatement("create table roles_perms (role varchar(255), perm varchar(255));");

        executeStatement("insert into user values ('admin', '5B844DDCB549E9DF29A7116C38B585FE452A5E7A0352102E984E1B793F44419B6BAC8246EA19F2F3618AE29AF6F015889A18D41BD038707C1017AFCCF70CE263', 'C7AD44CBAD762A5DA0A452F9E854FDC1E0E7A52A38015F23F3EAB1D80B931DD472634DFAC71CD34EBC35D16AB7FB8A90C81F975113D6C7538DC69DD8DE9077EC', 'Admin', '', 'At main office');");
        executeStatement("insert into user values ('jhon', '42F2E57DB605380D4ED651E390B08FCAC9CF29F42E523A07FC3FB0B73DCF3D813C164F6F75B5508DCF121895692DC7BE438CA5860C354073C95EC55B93CFB35D', '74591AF8230F8D40BCC8143DC743B5AB0A76FADD63D2E7BB21D570A265C49DDD6E3FCFF0E7BEC5BF0C676F3C38A76283D437656CA25F29173721D0219CF7A5A8', 'Jhon', 'Doe', '21st Street');");
        executeStatement("insert into user_roles values ('admin', 'admin');");
        executeStatement("insert into user_roles values ('jhon', 'user');");
        executeStatement("insert into roles_perms values ('user', 'manage_profile');");
        executeStatement("insert into roles_perms values ('admin', 'manage_user');");

        executeStatement("drop table if exists item;");
        executeStatement("create table item (id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, title varchar(100) default null, description varchar(255));");
        executeStatement("insert into item (title, description) values('note title', 'This is note for you');");
    }

    private void executeStatement(String sql) throws SQLException {
        conn.createStatement().execute(sql);
    }

}
