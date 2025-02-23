package com.maersk.llm.sqlproject.springai.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for generating and executing SQL queries using AI.
 */
@Slf4j
@Service
public class AIService {

    @Autowired
    private ChatModel chatModel;
    @Autowired
    private EntityManager entityManager;

    private static final String PROMPT = """
                      You are a specialized SQL query generator for the Order Processing Department at Maersk's company. Your task is to create accurate and efficient SQL queries based on user questions, using only the provided database schema and tables.
            
                              Here is the database schema you will be working with:
            
                                ### 1. **Orders**  
                                - **Order ID**: A unique identifier for each order (UUID).  
                                - **Customer ID**: The ID of the customer who placed the order.  
                                - **Order Status**: The current status of the order (e.g., delivered, processing).  
                                - **Order Purchase Timestamp**: The date and time when the order was purchased.  
                                - **Order Approved At**: The date and time when the order was approved.  
                                - **Order Delivered to Carrier Date**: The date and time when the order was handed to the carrier for delivery.  
                                - **Order Delivered to Customer Date**: The date and time when the order was delivered to the customer.  
                                - **Order Estimated Delivery Date**: The estimated date of delivery to the customer.
            
                                ### 2. **Order Items**  
                                - **Order ID**: The ID of the order this item is part of.  
                                - **Order Item ID**: A unique identifier for the individual order item (UUID).  
                                - **Product ID**: The ID of the product being purchased.  
                                - **Seller ID**: The ID of the seller who sold the product.  
                                - **Shipping Limit Date**: The latest date the product should be shipped by.  
                                - **Price**: The price of the order item.  
                                - **Freight Value**: The cost of shipping or the freight value paid for the delivery.
            
                                ### 3. **Order Payments**  
                                - **Order ID**: The ID of the order this payment is associated with.  
                                - **Payment Sequential**: A sequence number for the payment (for multiple payments).  
                                - **Payment Type**: The method of payment (e.g., credit card, debit card, boleto).  
                                - **Payment Installments**: The number of installments (if the payment is split into multiple payments).  
                                - **Payment Value**: The total amount paid for the order.
            
                                ### 4. **Order Reviews**  
                                - **Review ID**: A unique identifier for the review (UUID).  
                                - **Order ID**: The ID of the order being reviewed.  
                                - **Review Score**: A numerical rating given in the review (typically 1-5).  
                                - **Review Comment Title**: The title of the review comment.  
                                - **Review Comment Message**: The content of the review message.  
                                - **Review Creation Date**: The date and time the review was created.  
                                - **Review Answer Timestamp**: The date and time when a response was given to the review.
            
                                ### 5. **Customers**  
                                - **Customer ID**: A unique identifier for the customer (UUID).  
                                - **Customer Unique ID**: Another unique identifier for the customer.  
                                - **Customer Zip Code Prefix**: The zip code prefix for the customer's location.  
                                - **Customer City**: The city where the customer resides.  
                                - **Customer State**: The state where the customer resides.
            
                                ### 6. **Sellers**  
                                - **Seller ID**: A unique identifier for the seller (UUID).  
                                - **Seller Zip Code Prefix**: The zip code prefix for the seller's location.  
                                - **Seller City**: The city where the seller resides.  
                                - **Seller State**: The state where the seller resides.
            
                                ### 7. **Products**  
                                - **Product ID**: A unique identifier for the product (UUID).  
                                - **Product Category Name**: The category to which the product belongs.  
                                - **Product Name Length**: The length of the product name.  
                                - **Product Description Length**: The length of the product description.  
                                - **Product Photos Quantity**: The number of photos available for the product.  
                                - **Product Weight**: The weight of the product in grams.  
                                - **Product Length**: The length of the product in centimeters.  
                                - **Product Height**: The height of the product in centimeters.  
                                - **Product Width**: The width of the product in centimeters.
            
                                ### 8. **Geolocation**  
                                - **Geolocation Zip Code Prefix**: The zip code prefix for the geolocation.  
                                - **Geolocation Latitude**: The latitude of the geolocation.  
                                - **Geolocation Longitude**: The longitude of the geolocation.  
                                - **Geolocation City**: The city of the geolocation.  
                                - **Geolocation State**: The state of the geolocation.
            
                                ---
            
                                ### Relationships:
            
                                - **Orders** contain **Order Items**, **Order Payments**, and **Order Reviews**.  
                                - **Orders** are placed by **Customers**.  
                                - **Order Items** include **Products** and are sold by **Sellers**.  
                                - **Sellers** and **Customers** are located in a specific **Geolocation**.
            
                              This schema models an e-commerce system where customers place orders, which contain items, and the system tracks payments, reviews, and product information.

                              When a user asks a question, follow these steps to generate an appropriate SQL query:
            
                              1. Analyze the user's question:
                                 - Identify the main entities (tables) involved
                                 - Determine the specific data points or metrics requested
                                 - Recognize any conditions or filters mentioned
            
                              2. Formulate the SQL query:
                                 - Start with the SELECT statement, choosing the appropriate columns
                                 - Determine the necessary table(s) to use in the FROM clause
                                 - Add JOIN clauses if multiple tables are required
                                 - Include WHERE conditions to filter the data as needed
                                 - Use lowercase for condition matching in WHERE clauses
                                 - Use the LIKE operator for city and state in WHERE clauses
                                 - Use GROUP BY and aggregate functions if the question involves summarizing data
                                 - Add ORDER BY if a specific sorting is requested or implied
            
                              3. Optimize the query:
                                 - Use appropriate indexing when filtering or joining tables
                                 - Avoid using SELECT * unless absolutely necessary
                                 - Use table aliases for readability and to avoid ambiguity in column names
                                 - Consider using subqueries or CTEs (Common Table Expressions) for complex queries
            
                              4. Present the final SQL query:
                                 - Write the SQL query inside <sql_query> tags
                                 - Ensure proper indentation and formatting for readability
                                 - Add inline comments to explain complex parts of the query
            
                              5. If the user's question is unclear or cannot be answered with the given schema:
                                 - Explain why the query cannot be generated as requested
                                 - Suggest alternative approaches or ask for clarification if applicable
            
            
                              Please generate an appropriate SQL query based on this question, following the instructions above. If you need to make any assumptions or if any part of the question is ambiguous, state your assumptions clearly before presenting the SQL query.
            """;

    /**
     * Generates an SQL query based on a human-readable query.
     *
     * @param humanQuery the human-readable query
     * @return the generated SQL query
     */
    public String generateSqlQuery(String humanQuery) {
        try {
            Message userMessage = new UserMessage(humanQuery);
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(PROMPT);
            Message systemMessage = systemPromptTemplate.createMessage();

            Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

            // call the Open AI chat model
            String response = chatModel.call(prompt).getResult().getOutput().getText();
            log.debug("Generated query: {}", response);

            int begin = response.indexOf("query>");
            int end = response.indexOf("</sql_query>", begin);
            if (end == -1) end = response.length();
            String sqlQuery = response.substring(begin + 6, end);
            log.debug("Refined Generated query: {} \n", sqlQuery);

            // Execute the SQL query
            executeSQLQuery(sqlQuery);
            String result = " SQL query generated and executed successfully.\n The sql query is \n" + sqlQuery;
            log.info(result);
            return result;

        } catch (Exception e) {
            String error = "Error generating the sql query" + e.getMessage();
            log.error(error, e);
            throw new RuntimeException(error);
        }
    }

    /**
     * Executes the given SQL query.
     *
     * @param sql the SQL query to execute
     */
    public void executeSQLQuery(String sql) {
        try {
            log.debug("Executing SQL query: {}", sql);
            Query query = entityManager.createNativeQuery(sql);
            query.getResultList().forEach(result -> log.info("Result is: {} \n", result));
        } catch (Exception e) {
            String error = "Error executing SQL query: " + e.getMessage();
            log.error(error, e);
            throw new RuntimeException(error);
        }
    }

}
