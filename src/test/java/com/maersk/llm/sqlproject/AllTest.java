package com.maersk.llm.sqlproject;

import com.maersk.llm.sqlproject.springai.Model.AIQuestionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AllTest {

    public static final String URL_QUERY="http://localhost:8086/api/v1/query";

    @Test
    void get_the_most_orders_delelivered_by_sellers() {
        String question = "Which seller has delivered the most orders to customers in Rio de Janeiro? [string: seller_id]";
        execute(question);
    }

    @Test
    void get_the_highest_5_star_reviews(){
        String question = "Which product category has the highest rate of 5-star reviews? [string: category_name]";
        execute(question);
    }
    @Test
    void get_the_prodcut_review_scores(){
        String question = "What's the average review score for products in the 'beleza_saude' category? [float: score]";
        execute(question);
    }

    @Test
    void get_the_sellers_completed_orders(){
        String question = "How many sellers have completed orders worth more than 100000 BRL in total? [integer: count]";
        execute(question);
    }

    @Test
    void get_the_installement_count_check(){
        String question = "What's the most common payment installment count for orders over 1000 BRL? [integer: installments]";
        execute(question);
    }

    @Test
    void get_the_highest_average_freight(){
        String question = "Which city has the highest average freight value per order? [string: city_name]";
        execute(question);
    }
    @Test
    void get_the_most_expensive_product_category(){
        String question = "What's the most expensive product category based on average price? [string: category_name]";
        execute(question);
    }

    @Test
    void get_the_shortest_delivery_time(){
        String question = "Which product category has the shortest average delivery time? [string: category_name]";
        execute(question);
    }

    @Test
    void get_the_orders_from_multiple_sellers(){
        String question = "How many orders have items from multiple sellers? [integer: count]";
        execute(question);
    }

    @Test
    void get_the_percentage_of_orders_delivered(){
        String question = "What percentage of orders are delivered before the estimated delivery date? [float: percentage]";
        execute(question);
    }

    private static void execute(String question) {
        RestTemplate restTemplate = new RestTemplate();
        AIQuestionRequest aiQuestionRequest = new AIQuestionRequest();
        aiQuestionRequest.setQuestion(question);
        HttpEntity<AIQuestionRequest> request = new HttpEntity<>(aiQuestionRequest);
        String result = restTemplate.postForObject(URL_QUERY, request, String.class);
        System.out.printf("**Question**: %s\n", question);
        System.out.printf("**Result**: %s\n", result);
        assertTrue(result.contains("SQL query generated and executed successfully"));
        System.out.println("---------------------------------------------------");
        try {
            Thread.sleep(TimeUnit.SECONDS.toSeconds(5));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
