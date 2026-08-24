package com.example.spring_ai;

import com.example.spring_ai.service.AIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AIServiceTest {

    @Autowired
    private AIService aiService;

    @Test
    public void testGetJoke(){
        var joke = aiService.getJoke("dance");
        System.out.println(joke);
    }

    @Test
    public void testAskAI(){
        var res = aiService.askAI("what is spring ai");
        System.out.println(res);
    }

    @Test
    public void testEmbedText(){
        var embed = aiService.getEmbedding("This is a big text here.");
        System.out.println(embed.length);
        for(float e : embed){
            System.out.println(e+" ");
        }
    }

//    @Test
//    public void testStoreData(){
//        aiService.ingestDataToVectorStore();
//    }

    @Test
    public void testSimilaritySearch(){
        var res = aiService.similaritySearch("space movie");

        System.out.println(res);
    }
}
