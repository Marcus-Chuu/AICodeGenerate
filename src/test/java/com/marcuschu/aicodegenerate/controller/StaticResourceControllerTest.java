package com.marcuschu.aicodegenerate.controller;

import com.marcuschu.aicodegenerate.constant.AppConstant;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StaticResourceControllerTest {

    private final StaticResourceController controller = new StaticResourceController();

    @Test
    void shouldServeDeployedIndexFile() throws Exception {
        String deployKey = "test_" + UUID.randomUUID().toString().replace("-", "");
        File deployDirectory = new File(AppConstant.CODE_DEPLOY_ROOT_DIR, deployKey);
        File indexFile = new File(deployDirectory, "index.html");
        String html = "<html><body>deployed app</body></html>";

        try {
            Files.createDirectories(deployDirectory.toPath());
            Files.writeString(indexFile.toPath(), html, StandardCharsets.UTF_8);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/static/" + deployKey + "/");
            request.setAttribute(
                    HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
                    "/static/" + deployKey + "/"
            );

            ResponseEntity<Resource> response = controller.serveStaticResource(deployKey, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("text/html; charset=UTF-8", response.getHeaders().getFirst("Content-Type"));
            assertNotNull(response.getBody());
            assertEquals(html, response.getBody().getContentAsString(StandardCharsets.UTF_8));
        } finally {
            if (indexFile.exists()) {
                Files.delete(indexFile.toPath());
            }
            if (deployDirectory.exists()) {
                Files.delete(deployDirectory.toPath());
            }
        }
    }
}
