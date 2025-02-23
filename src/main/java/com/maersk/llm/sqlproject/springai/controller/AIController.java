package com.maersk.llm.sqlproject.springai.controller;

import com.maersk.llm.sqlproject.springai.Model.AIQuestionRequest;
import com.maersk.llm.sqlproject.springai.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling AI-related requests.
 */
@RestController
@RequestMapping("/api/v1")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * Endpoint to generate an SQL query based on a given question in a POST request.
     *
     * @param request the request body containing the question to generate the SQL query for
     * @return a ResponseEntity containing the generated SQL query or an error message
     */
    @PostMapping("/query")
    public ResponseEntity<String> generateSqlQuery(@RequestBody AIQuestionRequest request) {
        try {
            String question = request.getQuestion().trim();
            if(question == null || question.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid question");
            }else {
                return ResponseEntity.ok(aiService.generateSqlQuery(question));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}