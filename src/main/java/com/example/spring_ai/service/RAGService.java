package com.example.spring_ai.service;

import com.example.spring_ai.advisor.TokenUsageAdvisor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RAGService {
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    @Value("classpath:faq.pdf")
    Resource pdfFile;

    public String askAI(String prompt){
        String template = """
                You are an AI assistant helping a developer.
                
                Rules:
                - Use only the information provided in the context
                - You May rephrase, summarize, and explain in natural language
                - Do not introduce new concepts or facts
                - If multiple context sections are relevant, combine them into a single explanation.
                - If the answer is not present, say "I don't know"
                
                context
                {context}
                
                Answer in a friendly, conversational tone.
                """;

        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(prompt)
                .topK(4)
                .similarityThreshold(0.4)
                .filterExpression("file_name == 'faq.pdf'")
                .build());

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        PromptTemplate promptTemplate = new PromptTemplate(template);
        String systemPrompt = promptTemplate.render(Map.of("context", context));


        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .advisors()
                .call()
                .content();
    }

    public void ingestPdfToVectorStore(){
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfFile);
        List<Document> pages = reader.get();

        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                .withChunkSize(200)
                .build();

        List<Document> chunks = tokenTextSplitter.apply(pages);
        vectorStore.add(chunks);
    }

    public String askAIWithAdvisors(String prompt, String userId){
        return chatClient.prompt()
                .system("""
                        You are an AI assistant called Cody.
                        Greet users with your Name (Cody) and the user name if you know their name.
                        Answer in a friendly, conversational tone.
                        """
                )
                .user(prompt)
                .advisors(a -> a
                        .advisors(

                                new SafeGuardAdvisor(List.of("Politics", "Gaming", "gaming", "politics")),
                                MessageChatMemoryAdvisor.builder(chatMemory)
                                                .build(),
                                VectorStoreChatMemoryAdvisor.builder(vectorStore)
                                        .defaultTopK(4)
                                        .build(),

                                QuestionAnswerAdvisor.builder(vectorStore)
                                        .searchRequest(SearchRequest.builder()
                                            .filterExpression("file_name == 'faq.pdf'")
                                                .topK(4)
                                            .build())
                                        .build(),

                                new TokenUsageAdvisor()
                        )
                        .param("chat_memory_conversation_id", userId)
                )
                .call()
                .content();
    }


    public static List<Document> springAiDocs(){
        return List.of(
                new Document(
                        "Spring AI provides abstractions like ChatClient, ChatModel, and EmbeddingModel to interact with LLMs",
                        Map.of("topic", "ai")
                ),

                new Document(
                        "Spring AI supports vector databases for storing and searching embeddings",
                        Map.of("topic", "vector-database")
                ),

                new Document(
                        "EmbeddingModel converts text into numerical vectors that can be used for semantic search",
                        Map.of("topic", "embeddings")
                ),

                new Document(
                        "ChatClient makes it easier to send prompts and receive responses from language models",
                        Map.of("topic", "chat-client")
                )
        );
    }
}
