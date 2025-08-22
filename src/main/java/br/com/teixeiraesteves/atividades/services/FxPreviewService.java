package br.com.teixeiraesteves.atividades.services;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.util.concurrent.*;
import java.awt.*;

@Service
public class FxPreviewService {

    private ExecutorService fxExecutor;

    @PostConstruct
    public void init() {
        // Executor para rodar JavaFX numa thread dedicada
        fxExecutor = Executors.newSingleThreadExecutor( r -> {
           Thread t = new Thread(r);
           t.setDaemon(true);
           return t;
        });

        // Inicializa JavaFX toolkit
        Platform.startup(() -> {});
    }

    public record PreviewResult(String id, String dataUrl) {}

    public PreviewResult generateThumbnail(String url) {
        CompletableFuture<PreviewResult> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                    WebView webview = new WebView();
                    WebEngine engine = webview.getEngine();
                    // define tamanho 4:3 (1200x900)
                    int width = 1200, height = 900;
                    webview.setPrefSize(width, height);
                    engine.load(url);
                    engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                        switch (newState) {
                            case SUCCEEDED -> {
                                try {
                                    // Snapshot do webview
                                    WritableImage snapshot = new WritableImage(width, height);
                                    webview.snapshot(null, snapshot);
                                    BufferedImage bimg = SwingFXUtils.fromFXImage(snapshot, null);
                                    // Reduz par thumbnail menor mantendo 4:3
                                    BufferedImage thumb = resize(bimg, 800, 600);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ImageIO.write(thumb, "png", baos);
                                    byte[] bytes = baos.toByteArray();
                                    String base64 = Base64.getEncoder().encodeToString(bytes);
                                    PreviewResult result = new PreviewResult(
                                            UUID.randomUUID().toString(),
                                            "data:image/png;base64," + base64
                                    );
                                    future.complete(result);
                                } catch (Exception e) {
                                    future.completeExceptionally(e);
                                }
                            }
                            case FAILED, CANCELLED -> {
                                future.completeExceptionally(
                                        new RuntimeException("Falha ao carregar" + url));
                            }
                        }
                    });
            } catch (Exception e){
                future.completeExceptionally(e);
            }
        });

        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Timeout ou erro ao gerar thumbnail", e);
        }
    }

    private BufferedImage resize(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return dst;
    }
}
