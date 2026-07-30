package com.marcuschu.aicodegenerate.deploy;

import com.marcuschu.aicodegenerate.constant.AppConstant;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class ServeDeployService {

    private static final int SERVE_PORT = 3000;
    private static Process serveProcess;

    /**
     * 启动 Serve 服务
     */
    public void startServeService() {
        try {
            if (serveProcess == null || !serveProcess.isAlive()) {
                Path deployRootPath = Path.of(AppConstant.CODE_DEPLOY_ROOT_DIR);
                Files.createDirectories(deployRootPath);

                boolean isWindows = System.getProperty("os.name")
                        .toLowerCase()
                        .contains("win");
                ProcessBuilder processBuilder;
                if (isWindows) {
                    processBuilder = new ProcessBuilder(
                            "cmd.exe", "/c", "npx", "--yes", "serve",
                            deployRootPath.toString(), "-l", String.valueOf(SERVE_PORT)
                    );
                } else {
                    processBuilder = new ProcessBuilder(
                            "npx", "--yes", "serve",
                            deployRootPath.toString(), "-l", String.valueOf(SERVE_PORT)
                    );
                }
                processBuilder.inheritIO();
                serveProcess = processBuilder.start();
                System.out.println("Serve service started on port " + SERVE_PORT);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start serve service", e);
        }
    }

    /**
     * 关闭 Serve 服务
     */
    public void stopServeService() {
        if (serveProcess != null && serveProcess.isAlive()) {
            serveProcess.destroy();
            try {
                serveProcess.waitFor(5, TimeUnit.SECONDS);
                System.out.println("Serve service stopped");
            } catch (InterruptedException e) {
                serveProcess.destroyForcibly();
                System.out.println("Serve service force stopped");
            }
        }
    }
}
