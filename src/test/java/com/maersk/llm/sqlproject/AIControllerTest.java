package com.maersk.llm.sqlproject;

import com.maersk.llm.sqlproject.springai.Model.AIQuestionRequest;
import com.maersk.llm.sqlproject.springai.controller.AIController;
import com.maersk.llm.sqlproject.springai.service.AIService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AIControllerTest {

    @Mock
    private AIService aiService;

    @InjectMocks
    private AIController aiController;

    @Test
    void generateSqlQuery_validHumanQuery_returnsOkResponse() {
        AIQuestionRequest request = new AIQuestionRequest();
        request.setQuestion("Which seller delivered the most orders to customers in Rio de Janeiro?");
        String mockResponse = "SELECT s.seller_id, COUNT(o.order_id) AS total_orders FROM orders o ... LIMIT 1;";
        when(aiService.generateSqlQuery(request.getQuestion())).thenReturn(mockResponse);
        ResponseEntity<String> response = aiController.generateSqlQuery(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void generateSqlQuery_emptyHumanQuery_returnsInternalServerError() {
        AIQuestionRequest request = new AIQuestionRequest();
        request.setQuestion("");
        when(aiService.generateSqlQuery(request.getQuestion())).thenThrow(new RuntimeException("Invalid question"));
        ResponseEntity<String> response = aiController.generateSqlQuery(request);
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid question", response.getBody());
    }


    @Test
    void generateSqlQuery_validHumanQuery_handlesException() {
        AIQuestionRequest request = new AIQuestionRequest();
        request.setQuestion("Which seller delivered the most orders to customers in Rio de Janeiro?");
        when(aiService.generateSqlQuery(request.getQuestion())).thenThrow(new RuntimeException("API error"));
        ResponseEntity<String> response = aiController.generateSqlQuery(request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("API error", response.getBody());
    }
}
