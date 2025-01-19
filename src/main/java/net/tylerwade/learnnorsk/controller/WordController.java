package net.tylerwade.learnnorsk.controller;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.tylerwade.learnnorsk.model.Word;
import net.tylerwade.learnnorsk.repository.WordRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/words")
public class WordController {

    @Autowired
    private WordRepository wordRepo;

    // Get all words
    @GetMapping({"/", ""})
    public ResponseEntity<?> getAllWords() {
        return new ResponseEntity<>(wordRepo.findAll(), HttpStatus.OK);
    }

    // Add an array of words
    @PostMapping({"/", ""})
    public ResponseEntity<?> addWords(@RequestBody Word[] words) {

        if (words.length == 0) {
            return new ResponseEntity<>("No words provided", HttpStatus.BAD_REQUEST);
        }


        List<Word> existingWords = new ArrayList<>();
        // TODO: remove debug message
        // Debug:
        System.out.println("Attempting to add the words:");
        for (Word word : words) {
            System.out.println(word.toString());
        }

        // Check if words already exist in eng
        for (Word word : words) {

            // Check for empty word
            if (word.getNorsk() == null || word.getEng() == null || word.getNorsk().isEmpty() || word.getEng().isEmpty()) {
                return new ResponseEntity<>(new WordsExist("Some or all words are empty.", existingWords), HttpStatus.BAD_REQUEST);
            }

            // Check for norsk word
            Optional<Word> existingNorskWord = wordRepo.findByNorskIgnoreCase(word.getNorsk());
            if (existingNorskWord.isPresent()) {
                existingWords.add(existingNorskWord.get());
            }
            // Check for eng word
            Optional<Word> existingEngWord = wordRepo.findByEngIgnoreCase(word.getEng());
            if (existingEngWord.isPresent()) {
                if (!existingEngWord.get().equals(existingNorskWord.get())) {
                    existingWords.add(existingEngWord.get());
                }
            }

        }

        // Return error if words already exist
        if (existingWords.size() > 0) {
            return new ResponseEntity<>(new WordsExist("Some or all words already exist.", existingWords), HttpStatus.BAD_REQUEST);
        }

        // Save words
        for (Word word : words) {
            wordRepo.save(word);
        }

        return new ResponseEntity<>(words, HttpStatus.OK);
    }

    // Delete a word
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWord(@PathVariable Long id) {
        Optional<Word> word = wordRepo.findById(id);

        if (word.isEmpty()) {
            return new ResponseEntity<>("Word not found", HttpStatus.NOT_FOUND);
        }

        wordRepo.delete(word.get());

        return new ResponseEntity<>("Word deleted", HttpStatus.OK);
    }

    // Update a word
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWord(@PathVariable Long id, @RequestBody Word newValues) {
        Optional<Word> existingWordOptional = wordRepo.findById(id);
        if (existingWordOptional.isEmpty()) {
            return new ResponseEntity<>("Word not found", HttpStatus.NOT_FOUND);
        }

        Word existingWord = existingWordOptional.get();

        // Update norsk
        if (newValues.getNorsk() != null)
            existingWord.setNorsk(newValues.getNorsk());

        // Update eng
        if (newValues.getEng() != null)
            existingWord.setEng(newValues.getEng());

        // Update image
        if (newValues.getImage() != null)
            existingWord.setImage(newValues.getImage());

        wordRepo.save(existingWord);

        return new ResponseEntity<>(existingWord, HttpStatus.OK);
    }
}

@Getter @Setter @NoArgsConstructor
class WordsExist {

    private String message;
    private List<Word> existingWords;

    public WordsExist(String message, List<Word> existingWords) {
        this.message = message;
        this.existingWords = existingWords;
    }
}
