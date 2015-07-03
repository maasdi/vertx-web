package app.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;

/**
 * AppUtil.java
 *
 * <p>An utility class for multipurposes
 */
public class AppUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppUtil.class);

    private static final String CONFIG_NAME = "config.json";
    private static final AppUtil me = new AppUtil();

    private static JsonObject config;

    private static JDBCClient jdbcClient;

    /**
     * ConfigUtil constructor, and should not allowed to create instance from
     * outside.
     */
    private AppUtil() {
        try {
            URL url = getClass().getClassLoader().getResource(CONFIG_NAME);
            LOGGER.debug("Config URL : {}", url);
            ObjectMapper mapper = new ObjectMapper();
            config = new JsonObject((Map<String, Object>) mapper.readValue(url, Map.class) );
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public static String configStr(String key) {
        return config.getString(key);
    }

    public static Integer configInt(String key) {
        return config.getInteger(key);
    }

    public static JDBCClient getJdbcClient(Vertx vertx) {
        if (jdbcClient == null) {
            JsonObject config = new JsonObject()
                    .put("url", AppUtil.configStr("db.url"))
                    .put("driver_class", AppUtil.configStr("db.driver_class"));

            String username = AppUtil.configStr("db.user");
            if (StringUtils.isNotBlank(username))
                config.put("user", username);

            String password = AppUtil.configStr("db.password");
            if (StringUtils.isNotBlank(password))
                config.put("password", password);

            jdbcClient = JDBCClient.createShared(vertx, config);
        }

        return jdbcClient;
    }

}
