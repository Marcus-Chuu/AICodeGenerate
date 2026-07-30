package com.marcuschu.aicodegenerate.controller;

import com.marcuschu.aicodegenerate.constant.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/static")
public class StaticResourceController {


    /**
     * 提供生成预览和已部署应用的静态资源访问，支持目录重定向。
     * 预览目录格式：/api/static/{codeGenType}_{appId}/
     * 部署目录格式：/api/static/{deployKey}/
     */
    @GetMapping("/{deployKey}/**")
    public ResponseEntity<Resource> serveStaticResource(@PathVariable String deployKey, HttpServletRequest request) {
        try {
            // 获取资源路径
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/" + deployKey).length());
            // 如果是目录访问（不带斜杠），重定向到带斜杠的URL
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            // 默认返回 index.html
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }

            // 随机部署标识优先从部署目录读取；代码生成目录仍用于实时预览。
            File rootDirectory = resolveRootDirectory(deployKey);
            if (rootDirectory == null) {
                return ResponseEntity.notFound().build();
            }

            // 使用规范路径校验，避免通过 ../ 访问应用目录之外的文件。
            File file = new File(rootDirectory, resourcePath.substring(1)).getCanonicalFile();
            String rootPath = rootDirectory.getCanonicalPath() + File.separator;
            if (!file.getPath().startsWith(rootPath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // 检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }
            // 返回文件资源
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(file.getName()))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private File resolveRootDirectory(String directoryName) throws IOException {
        if (directoryName == null || !directoryName.matches("[A-Za-z0-9_-]+")) {
            return null;
        }

        File deployDirectory = new File(AppConstant.CODE_DEPLOY_ROOT_DIR, directoryName).getCanonicalFile();
        if (deployDirectory.isDirectory()) {
            return deployDirectory;
        }

        File previewDirectory = new File(AppConstant.CODE_OUTPUT_ROOT_DIR, directoryName).getCanonicalFile();
        if (previewDirectory.isDirectory()) {
            return previewDirectory;
        }
        return null;
    }

    /**
     * 根据文件扩展名返回带字符编码的 Content-Type
     */
    private String getContentTypeWithCharset(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".json")) return "application/json; charset=UTF-8";
        if (filePath.endsWith(".svg")) return "image/svg+xml";
        if (filePath.endsWith(".webp")) return "image/webp";
        if (filePath.endsWith(".ico")) return "image/x-icon";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}
