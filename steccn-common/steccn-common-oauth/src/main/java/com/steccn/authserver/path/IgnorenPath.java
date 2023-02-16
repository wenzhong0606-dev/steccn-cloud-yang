package com.steccn.authserver.path;

import com.google.common.collect.Sets;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

/**
 * @Description: 白名单url
 */
@Configuration
@ConfigurationProperties(prefix = "ignore.resource")
public class IgnorenPath {

    static Set<String> igpath = Sets.newHashSet();

    public IgnorenPath() {
        igpath.add("/feign/**");
        igpath.add("/doc.html");
        igpath.add("/webjars/**");
        igpath.add("/api-docs-ext");
        igpath.add("/api-docs");
        igpath.add("/v3/api-docs");
        igpath.add("/swagger-ui.html");
        igpath.add("/swagger-resources/**");
        igpath.add("/actuator/**");
        igpath.add("/v2/**");
        igpath.add("/css/**");
        igpath.add("/js/**");
        igpath.add("/images/**");
        igpath.add("/favicon.ico");
        igpath.add("/service-worker.js");
    }

    private String id;
    private List<String> ignoredPath;

    private String[] allIgnoredPath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getIgnoredPath() {
        return ignoredPath;
    }

    public void setIgnoredPath(List<String> ignoredPath) {
        this.ignoredPath = ignoredPath;
    }

    public String[] getAllIgnoredPath() {
        if (this.ignoredPath != null && !this.ignoredPath.isEmpty()) {
            this.ignoredPath.forEach(p->{
                igpath.add(p);
            });
        }
        return igpath.toArray(new String[igpath.size()]);
    }

    public void setAllIgnoredPath(String[] allIgnoredPath) {

    }

    public static Set<String> getIgpath() {
        return igpath;
    }
}