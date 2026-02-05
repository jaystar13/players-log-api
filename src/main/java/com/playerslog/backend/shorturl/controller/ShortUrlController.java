package com.playerslog.backend.shorturl.controller;

import com.playerslog.backend.shorturl.dto.ShortUrlGenerateRequest;
import com.playerslog.backend.shorturl.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/") // Root path for short URL redirection
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    @PostMapping("/short-urls")
    public ResponseEntity<String> generateShortUrl(@RequestBody ShortUrlGenerateRequest request) {
        String shortCode = shortUrlService.generateShortUrl(request.getGollId());
        // In a real application, you might return the full short URL including domain.
        // For now, just the short code.
        return ResponseEntity.ok(shortCode);
    }

    @GetMapping("/s/{shortCode}")
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode) {
        Long gollId = shortUrlService.getGollIdByShortCode(shortCode);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/goll/" + gollId); // Assuming frontend has /goll/{gollId} route
        return redirectView;
    }
}
