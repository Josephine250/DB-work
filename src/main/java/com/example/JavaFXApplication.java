package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.File;

public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private static final String APP_URL = "http://localhost:8000";
    
    // Use a consistent cache location instead of a unique one per launch
    private static final String CACHE_DIR = System.getProperty("user.home") + "/.college-portal/cache";

    static {
        // --- Stability: Bypass failing Direct3D/GPU pipeline on Windows ---
        // This fixes the flood of "RTTexture NullPointerException" RenderJob errors
        // that cause the WebView to freeze and lag continuously.
        System.setProperty("prism.order", "sw");       // Use software renderer
        System.setProperty("prism.vsync", "false");    // Reduces render thread blocking
        System.setProperty("prism.dirtyopts", "true"); // Only repaint changed regions
        System.setProperty("prism.lcdtext", "false");  // Skip GPU LCD font rendering
        // Prevent WebKit from trying hardware-accelerated compositing
        System.setProperty("com.sun.webkit.acceleratedLayerRendering", "false");
    }

    @Override
    public void init() throws Exception {
        // Start Spring Boot in the background
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.springContext = new SpringApplicationBuilder()
                .sources(DemoApplication.class)
                .run(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("College Registration System - Desktop");
        
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        
        // Enable persistent caching to speed up resource loading (CSS, JS, Fonts)
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        webEngine.setUserDataDirectory(cacheDir);
        
        // Handle navigation and errors
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.FAILED) {
                System.err.println("Page failed to load: " + webEngine.getLocation());
            }
        });

        // Load the URL as soon as Spring is ready
        // We'll use a shorter polling mechanism instead of a flat sleep
        stage.setOnShown(event -> {
            new Thread(() -> {
                waitForSpringAndLoad(webEngine);
            }).start();
        });

        BorderPane root = new BorderPane(webView);
        Scene scene = new Scene(root, 1280, 850);
        
        stage.setScene(scene);
        stage.show();
    }

    private void waitForSpringAndLoad(WebEngine engine) {
        // Simple polling to check if Spring is up (context is initialized)
        int attempts = 0;
        while (springContext == null || !springContext.isRunning()) {
            try {
                Thread.sleep(100);
                if (attempts++ > 100) break; // Timeout after 10s
            } catch (InterruptedException e) {
                break;
            }
        }
        // Load the page
        Platform.runLater(() -> engine.load(APP_URL));
    }

    @Override
    public void stop() throws Exception {
        if (this.springContext != null) {
            this.springContext.close();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(JavaFXApplication.class, args);
    }
}
