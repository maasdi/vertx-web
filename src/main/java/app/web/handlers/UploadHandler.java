package app.web.handlers;

import app.web.AppUtil;
import app.web.annotations.RouteHandler;
import app.web.annotations.RouteMapping;
import app.web.annotations.RouteMethod;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@RouteHandler("/secured/upload")
public class UploadHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadHandler.class);

    @RouteMapping(method = RouteMethod.GET)
    public Handler<RoutingContext> uploadForm() {
        return ctx -> {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
            StringBuilder sb = new StringBuilder();
            sb.append("<form name=\"upload\" action=\"/secured/upload\" method=\"post\" enctype=\"multipart/form-data\">");
            sb.append("   <div>");
            sb.append("        <label for=\"name\">Select a file:</label>\n");
            sb.append("        <input type=\"file\" name=\"file\" />\n");
            sb.append("    </div>\n");
            sb.append("    <div class=\"button\">\n");
            sb.append("        <button type=\"submit\">Send</button>\n");
            sb.append("    </div>");
            sb.append("</form>");
            ctx.response().end(sb.toString());
        };
    }

    @RouteMapping(method = RouteMethod.POST)
    public Handler<RoutingContext> upload() {
        return ctx -> {

            Set<FileUpload> files = ctx.fileUploads();
            String filename = "";
            for (FileUpload file : files) {
                String path = file.uploadedFileName();
                LOGGER.debug("upload path : {}", path);
                filename = path.substring(path.lastIndexOf("\\") + 1);
            }

            ctx.response().putHeader("location", "/secured/upload/" + filename).setStatusCode(302).end();
        };
    }

    @RouteMapping(value = "/:filename", method = RouteMethod.GET)
    public Handler<RoutingContext> showFile() {
        return ctx -> {
            String filename = ctx.request().getParam("filename");
            // prepend upload dir
            filename = AppUtil.getUploadDir() + "\\" + filename;

            ctx.response().sendFile(filename, res -> {
                if (res.failed()) {
                    LOGGER.error(res.cause().getMessage(), res.cause());
                    ctx.fail(res.cause());
                }
            });
        };
    }

}
