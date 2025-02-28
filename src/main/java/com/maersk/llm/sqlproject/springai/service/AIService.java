package com.maersk.llm.sqlproject.springai.service;

import com.maersk.llm.sqlproject.springai.Model.AIQuestionResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
            You are a specialized SQL query generator for the Order Processing Department at Maersk. Your task is to create accurate and efficient SQL queries based on user questions, using only the provided database schema and tables.
            
            Here is the user's question:
                    <user_question>
                    {USER_QUESTION}
                    </user_question>
            
            Before we begin, let's review the database schema you'll be working with:
            
            <database_schema>
                Table name: orders
                Column names:     
                    string order_id - contains the unique order id in the format of UUID
                    string customer_id - references the customer id
                    string order_status - status of the order such as delivered, processing
                    datetime order_purchase_timestamp - purchase date and time
                    datetime order_approved_at - approved date and time
                    datetime order_delivered_carrier_date - order delivery date and time to carrier
                    datetime order_delivered_customer_date - order delivery date and time to customer
                    datetime order_estimated_delivery_date - estimated delivery date to customer
            
            
                Table name: order_items
                Column names:
                    string order_id - references the order id
                    int order_item_id - unique order item id in the format of UUID
                    string product_id - references the product id
                    string seller_id - references the seller id
                    datetime shipping_limit_date - shipping limit date and time
                    float price - price of the order item
                    float freight_value - total price paid for the cargo
            
            
                Table name: order_payments
                Column names:
                    string order_id - references the order id
                    int payment_sequential - unique payment sequential id in the format of UUID
                    string payment_type - type of payment such as credit_card, debit_card, boleto
                    int payment_installments - number of payment installments
                    float payment_value - total value of the payment has done
            
            
                Table name: order_reviews
                Column names: 
                    string review_id - unique review id in the format of UUID
                    string order_id -  references the order id
                    int review_score - score of the review
                    string review_comment_title - title of the review
                    string review_comment_message - message of the review
                    datetime review_creation_date - date and time of the review creation
                    datetime review_answer_timestamp - date and time of the review answer
            
                Table name: customers
                Column names:   
                    string customer_id - unique customer id in the format of UUID
                    string customer_unique_id - unique customer id
                    string customer_zip_code_prefix - zip code prefix of the customer
                    string customer_city - details of the city where customer lives
                    string customer_state - details of state where the customer lives
            
            
                Table name: sellers
                Column names:  
                    string seller_id - unique seller id in the format of UUID
                    string seller_zip_code_prefix - zip code prefix of the seller
                    string seller_city - details of the city where seller lives
                    string seller_state - details of state where the seller lives
            
            
                Table name: products
                Column names:
                    string product_id - unique product id in the format of UUID
                    string product_category_name - name of the product
                    int product_name_length - length of the product name
                    int product_description_length - length of the product description
                    int product_photos_qty - number of photos for the product
                    float product_weight_g - weight of the product in grams
                    float product_length_cm - length of the product in centimeters
                    float product_height_cm - height of the product in centimeters
                    float product_width_cm -  width of the product in centimeters
            
            
                Table name: geolocation
                Column names: 
                    string geolocation_zip_code_prefix - zip code prefix of the geolocation
                    float geolocation_lat - latitude of the geolocation
                    float geolocation_lng - longitude of the geolocation
                    string geolocation_city - details of the city
                    string geolocation_state - details of the state
            
            Relationships:
            - Orders contain Order Items, Order Payments, and Order Reviews.
            - Orders are placed by Customers.
            - Order Items include Products and are sold by Sellers.
            - Sellers and Customers are located in a specific Geolocation.
            </database_schema>
            
            To generate an appropriate SQL query, follow these steps:
            
            1. Analyze the user's question and Identify the column/field names expected in results
            2. Formulate the SQL query 
            3. Optimize the query
            4. Identify potential edge cases or limitations
            5. Consider performance implications
            6. Present the final SQL query
            
            For each step, wrap your thoughts in <thought_process> tags. This will help ensure a thorough and accurate query generation.
            
            Step 1: Analyze the user's question
            <thought_process>
            - Identify the main entities (tables) involved
            - Determine the specific data points or metrics requested
            - Recognize any conditions or filters mentioned
            - Count the number of tables that will be involved in the query
            </thought_process>
            
            Step 2: Formulate the SQL query
            <thought_process>
            - Start with the SELECT statement, choosing the appropriate columns
            - Determine the necessary table(s) to use in the FROM clause
            - Add JOIN clauses if multiple tables are required
            - Verify that all necessary joins are included
            - Include WHERE conditions to filter the data as needed
            - Use lowercase for condition matching in WHERE clauses
            - Use the LIKE operator for city and state in WHERE clauses
            - Use GROUP BY and aggregate functions if the question involves summarizing data
            - Add ORDER BY if a specific sorting is requested or implied
            </thought_process>
            
            Step 3: Optimize the query
            <thought_process>
            - Use appropriate indexing when filtering or joining tables
            - Avoid using SELECT * unless absolutely necessary
            - Ensure no extra or invalid characters are in your query, such as extra double colon (::)
            - Use table aliases for readability and to avoid ambiguity in column names
            - Consider using subqueries or CTEs (Common Table Expressions) for complex queries
            - Use subqueries or window functions for advanced calculations
            - Double-check that the query meets all requirements and is optimized for the specific question asked
            - Return only the request column in the bracket [] to reduce query complexity
            </thought_process>
            
            Step 4: Identify potential edge cases or limitations
            <thought_process>
            - Consider scenarios where the query might return unexpected results
            - Think about possible data inconsistencies or missing data that could affect the query
            - Identify any assumptions made in the query that might not always hold true
            </thought_process>
            
            Step 5: Consider performance implications
            <thought_process>
            - Evaluate the complexity of the query and its potential impact on performance
            - Consider the size of the tables involved and how it might affect query execution time
            - Think about ways to optimize the query further if performance might be an issue
            </thought_process>
            
            Step 6: Present the final SQL query
            Present your final SQL query within <sql_query> tags. 
            Ensure proper indentation and formatting for readability. 
            Add inline comments to explain complex parts of the query.
            
            **Ensure that:**
            - The ORDER BY clause directly applies the aggregate function (e.g., `ORDER BY AVG(oi.freight_value) DESC`)
            - Only the required column(s) as per the user question are returned.
            
            Example 1 output structure:
            
            <sql_query>
            SELECT column1
            FROM table1 t1
            JOIN table2 t2 ON t1.id = t2.id
            WHERE condition = 'value'
            GROUP BY column1
            ORDER BY column1 DESC
            LIMIT 10;
            -- Inline comment explaining a complex part of the query
            </sql_query>
            
            Example 2 output structure:
            
            <sql_query>
            SELECT COUNT(*) AS 'Sellers' FROM (
            	SELECT s.seller_id
            	FROM order_items i
            	JOIN sellers s ON i.seller_id = s.seller_id
            	JOIN orders o ON i.order_id = o.order_id
            	WHERE o.order_status = 'delivered'
            	GROUP BY s.seller_id
            	HAVING SUM(i.price) > 100000
            );
            -- Inline comment explaining a complex part of the query
            </sql_query>
            
            If the user's question is unclear or cannot be answered with the given schema:
            - Explain why the query cannot be generated as requested
            - Suggest alternative approaches or ask for clarification if applicable
            
            Remember to use lowercase for condition matching in WHERE clauses, and use the LIKE operator for city and state comparisons. Ensure that the generated query is valid SQL syntax and executable in a SQLite database.
            
            Now, please generate an appropriate SQL query based on the user's question, following the instructions above. If you need to make any assumptions or if any part of the question is ambiguous, state your assumptions clearly before presenting the SQL query. 
            """;

    /**
     * Generates an SQL query based on a human-readable query.
     *
     * @param humanQuery the human-readable query
     * @return the generated SQL query
     */
    public AIQuestionResponse generateSqlQuery(String humanQuery) {
        try {
            AIQuestionResponse aiQuestionResponse = new AIQuestionResponse();
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(PROMPT);
            Message systemMessage = systemPromptTemplate.createMessage(Map.of("USER_QUESTION", humanQuery));

            Prompt prompt = new Prompt(List.of(systemMessage));

            // call the Open AI chat model
            String response = chatModel.call(prompt).getResult().getOutput().getText();
            log.debug("Generated query: {}", response);

            int begin = response.indexOf("query>");
            int end = response.indexOf("</sql_query>", begin);
            if (end == -1) end = response.length();
            String sqlQuery = response.substring(begin + 6, end);
            log.debug("Refined Generated query: {} \n", sqlQuery);

            // Execute the SQL query
            String dbResult = executeSQLQuery(sqlQuery);
            String result = " SQL query generated and executed successfully.\n The sql query is \n" + sqlQuery;
            aiQuestionResponse.setMessage("SQL query generated and executed successfully");
            aiQuestionResponse.setGeneratedSql(sqlQuery);
            aiQuestionResponse.setDbResult(dbResult);
            log.info(result);
            return aiQuestionResponse;

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
     * @return
     */
    public String executeSQLQuery(String sql) {
        try {
            log.debug("Executing SQL query: {}", sql);
            Query query = entityManager.createNativeQuery(sql);
            log.info("Results of the query: {}\n", query.getResultList());
            query.getResultList().forEach(result -> log.info("Result is: {} \n", result));
            return query.getResultList().get(0).toString();
        } catch (Exception e) {
            String error = "Error executing SQL query: " + e.getMessage();
            log.error(error, e);
            throw new RuntimeException(error);
        }
    }

}
