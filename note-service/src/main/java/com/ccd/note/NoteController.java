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
        this.ai = RestClient.builder().baseUrl(aiUrl).build();
    }

    @GetMapping
    public List<Note> all(@RequestParam(required = false) String tag) {
        return tag == null ? repo.findAll() : repo.findByTagsContaining(tag);
    }

    @GetMapping("/{id}")
    public Note one(@PathVariable String id) {
        return repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note create(@RequestBody Note n) {
        n.setId(null);
        Note saved = repo.save(n);
        log.info("Created note {}", saved.getId());
        return saved;
    }

    @PutMapping("/{id}")
    public Note update(@PathVariable String id, @RequestBody Note in) {
        Note n = one(id);
        n.setTitle(in.getTitle());
        n.setContent(in.getContent());
        n.setTags(in.getTags());
        return repo.save(n);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        repo.deleteById(id);
        log.info("Deleted note {}", id);
    }

    @PostMapping("/{id}/summarize")
    public Note summarize(@PathVariable String id) {
        Note n = one(id);
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
