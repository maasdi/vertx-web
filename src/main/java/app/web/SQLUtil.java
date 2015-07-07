package app.web;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQLUtil.java
 *
 * An utility class for SQL related operations.
 */
public class SQLUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLUtil.class);

    /**
     * An SQL utility class
     */
    private SQLUtil() {}

    public static void query(SQLConnection conn, String sql, Handler<ResultSet> done) {
        LOGGER.debug("query - {}", sql);
        conn.query(sql, res -> {
            if (res.failed()) {
                LOGGER.error(res.cause().getMessage(), res.cause());
                throw new RuntimeException(res.cause());
            }

            done.handle(res.result());
        });
    }

    public static void query(SQLConnection conn, String sql, JsonArray params, Handler<ResultSet> done) {
        LOGGER.debug("query - {}", sql);
        conn.queryWithParams(sql, params, res -> {
            if (res.failed()) {
                LOGGER.error(res.cause().getMessage(), res.cause());
                throw new RuntimeException(res.cause());
            }

            done.handle(res.result());
        });
    }

    public static void update(SQLConnection conn, String sql, Handler<UpdateResult> done) {
        LOGGER.debug("query - {}", sql);
        conn.update(sql, res -> {
           if (res.failed()) {
               LOGGER.error(res.cause().getMessage(), res.cause());
               throw new RuntimeException(res.cause());
           }

           done.handle(res.result());
        });
    }

    public static void update(SQLConnection conn, String sql, JsonArray params, Handler<UpdateResult> done) {
        LOGGER.debug("query - {}", sql);
        conn.updateWithParams(sql, params, res -> {
            if (res.failed()) {
                LOGGER.error(res.cause().getMessage(), res.cause());
                throw new RuntimeException(res.cause());
            }

            done.handle(res.result());
        });
    }

    public static void execute(SQLConnection conn, String sql, Handler<Void> done) {
        LOGGER.debug("query - {}", sql);
        conn.execute(sql, res -> {
            if (res.failed()) {
                LOGGER.error(res.cause().getMessage(), res.cause());
                throw new RuntimeException(res.cause());
            }

            done.handle(null);
        });
    }

    public static void close(SQLConnection conn) {
        conn.close(res -> {
            if (res.failed()) {
                LOGGER.error(res.cause().getMessage(), res.cause());
                throw new RuntimeException(res.cause());
            }
        });
    }

}
