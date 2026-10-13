package com.ccd.note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
public class NoteController {
    private static final Logger log = LoggerFactory.getLogger(NoteController.class);
    private final NoteRepository repo;
    private final RestClient ai;

    public NoteController(NoteRepository repo, @Value("${ai.service.url}") String aiUrl) {
        this.repo = repo;
                org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(90000);
        this.ai = RestClient.builder().baseUrl(aiUrl).requestFactory(factory).build();
    }

    /** Finds a note only if it belongs to this user; otherwise behaves as if it does not exist. */
    private Note owned(String id, String userId) {
        return repo.findById(id)
                .filter(n -> userId.equals(n.getUserId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }

    @GetMapping
    public List<Note> all(@RequestAttribute("userId") String userId,
                          @RequestParam(required = false) String tag) {
        return tag == null ? repo.findByUserId(userId) : repo.findByUserIdAndTagsContaining(userId, tag);
    }

    @GetMapping("/{id}")
    public Note one(@RequestAttribute("userId") String userId, @PathVariable String id) {
        return owned(id, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note create(@RequestAttribute("userId") String userId, @RequestBody Note n) {
        n.setId(null);
        n.setUserId(userId);
        n.setSummary(null);
        n.setCreatedAt(Instant.now());
        Note saved = repo.save(n);
        log.info("User {} created note {}", userId, saved.getId());
        return saved;
    }

    @PutMapping("/{id}")
    public Note update(@RequestAttribute("userId") String userId, @PathVariable String id,
                       @RequestBody Note in) {
        Note n = owned(id, userId);
        n.setTitle(in.getTitle());
        n.setContent(in.getContent());
        n.setTags(in.getTags());
        return repo.save(n);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestAttribute("userId") String userId, @PathVariable String id) {
        owned(id, userId);
        repo.deleteById(id);
        log.info("User {} deleted note {}", userId, id);
    }

    @PostMapping("/{id}/summarize")
    public Note summarize(@RequestAttribute("userId") String userId, @PathVariable String id) {
        Note n = owned(id, userId);
        String text = n.getContent() == null ? "" : n.getContent();
        try {
            Map<?, ?> res = ai.post().uri("/summarize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", text))
                    .retrieve()
                    .body(Map.class);
            n.setSummary(res == null ? null : String.valueOf(res.get("summary")));
            log.info("Summarized note {}", id);
            return repo.save(n);
        } catch (RestClientException e) {
            log.error("AI service call failed for note {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI service unavailable");
        }
    }
}
