package com.warehouse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web配置类
 * 配置静态资源访问
 * 
 * @author Warehouse Team
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的静态资源访问
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

        // 确保路径以正斜杠结尾
        String normalizedPath = uploadPath.replace("\\", "/");
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }

        System.out.println("配置静态资源路径: " + normalizedPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + normalizedPath)
                .setCachePeriod(3600); // 缓存1小时
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 为静态资源添加CORS支持
        registry.addMapping("/uploads/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
