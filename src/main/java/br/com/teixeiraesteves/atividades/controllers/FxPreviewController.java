package br.com.teixeiraesteves.atividades.controllers;

import br.com.teixeiraesteves.atividades.services.FxPreviewService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fxpreview")
public class FxPreviewController {

    private final FxPreviewService service;

    public FxPreviewController(FxPreviewService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FxPreviewService.PreviewResult> preview(@RequestParam String url) {
        var result = service.generateThumbnail(url);
        return ResponseEntity.ok(result);
    }

}
