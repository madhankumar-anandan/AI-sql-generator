package com.maersk.llm.sqlproject;


import com.maersk.llm.sqlproject.springai.Model.AIQuestionResponse;
import com.maersk.llm.sqlproject.springai.service.AIService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AIServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AIService aiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateSqlQuery_validQuestion_returnsSqlQuery() {
        String question = "Which seller delivered the most orders to customers in Rio de Janeiro?";
        String mockResponse = """
                To determine which seller has delivered the most orders to customers in Rio de Janeiro, you can use the following SQL query:

                ```sql
                SELECT 
                    s.seller_id,
                    COUNT(o.order_id) AS total_orders
                FROM 
                    orders o
                JOIN 
                    order_items oi ON o.order_id = oi.order_id
                JOIN 
                    sellers s ON oi.seller_id = s.seller_id
                JOIN 
                    customers c ON o.customer_id = c.customer_id
                WHERE 
                    c.customer_city = 'rio de janeiro' AND
                    o.order_status = 'delivered'
                GROUP BY 
                    s.seller_id
                ORDER BY 
                    total_orders DESC
                LIMIT 1;
                ```
                This query joins the `orders`, `order_items`, `sellers`, and `customers` tables, filtering for orders delivered to customers in Rio de Janeiro.
                """;

        // Mock behavior of the chatModel to return the mock response
        AssistantMessage assistantMessage= new AssistantMessage(mockResponse);
        Generation generation = new Generation(assistantMessage);
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // Mock behavior of the entityManager to return a Query object
        Query query = mock(Query.class);
        when(query.getResultList()).thenReturn(List.of("10"));
        when(entityManager.createNativeQuery(any(String.class))).thenReturn(query);

        AIQuestionResponse result = aiService.generateSqlQuery(question);

        assertTrue(result.getMessage().contains("SQL query generated and executed successfully"));
        assertTrue(result.getDbResult().matches("10"));
    }

    @Test
    void generateSqlQuery_invalidQuestion_returnsNull() {
        String question = "";
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("Invalid question"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> aiService.generateSqlQuery(question));
        assertTrue(exception.getMessage().contains("Error generating the sql query"));
    }

    @Test
    void executeSQLQuery_validSql_executesSuccessfully() {
        // Mock behavior of the entityManager to return a Query object
        String sql = "SELECT * FROM orders";
        Query query = mock(Query.class);
        when(query.getResultList()).thenReturn(List.of("10"));
        when(entityManager.createNativeQuery(sql)).thenReturn(query);
        String result = aiService.executeSQLQuery(sql);
        assertTrue(result.matches("10"));
    }

    @Test
    void executeSQLQuery_invalidSql_throwsException() {
        String sql = "INVALID SQL";
        when(entityManager.createNativeQuery(sql)).thenThrow(new RuntimeException("SQL error"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> aiService.executeSQLQuery(sql));
        assertTrue(exception.getMessage().contains("SQL error"));
    }
}