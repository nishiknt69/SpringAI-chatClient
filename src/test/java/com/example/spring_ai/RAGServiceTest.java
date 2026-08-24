package com.example.spring_ai;

import com.example.spring_ai.service.RAGService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RAGServiceTest {

    @Autowired
    private RAGService ragService;

//    @Test
//    public void testIngest(){
//        ragService.ingestPdfToVectorStore();
//    }

    @Test
    public void testAskAI(){
        var response = ragService.askAI("How to connect to my discord account?");
        System.out.println(response);
    }
}
