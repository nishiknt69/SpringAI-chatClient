package com.example.spring_ai.service;

import com.example.spring_ai.dto.Joke;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {
    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public float[] getEmbedding(String text){
        return embeddingModel.embed(text);
    }

    public void ingestDataToVectorStore(){
        List<Document> movies = List.of(
                new Document(
                        "A thief who steals corporate secrets through the use of dream-sharing technology.",
                        Map.of("title", "Inception", "genre", "Sci-Fi", "year", 2010)
                ),

                new Document(
                        "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.",
                        Map.of("title", "The Matrix", "genre", "Sci-Fi", "year", 1999)
                ),

                new Document(
                        "A young wizard begins his magical education and discovers that he has a special connection to a powerful dark wizard.",
                        Map.of("title", "Harry Potter and the Sorcerer's Stone", "genre", "Fantasy", "year", 2001)
                ),

                new Document(
                        "A group of superheroes joins forces to protect Earth from a powerful alien army led by a dangerous warlord.",
                        Map.of("title", "The Avengers", "genre", "Action", "year", 2012)
                ),

                new Document(
                        "A young lion prince must overcome tragedy and accept his responsibility as the future king of the Pride Lands.",
                        Map.of("title", "The Lion King", "genre", "Animation", "year", 1994)
                ),

                new Document(
                        "A young woman discovers a mysterious world hidden behind a magical door and meets a strange alternate version of her family.",
                        Map.of("title", "Coraline", "genre", "Fantasy", "year", 2009)
                ),

                new Document(
                        "A scientist creates a dinosaur theme park using genetic engineering, but the creatures escape and threaten the visitors.",
                        Map.of("title", "Jurassic Park", "genre", "Adventure", "year", 1993)
                ),

                new Document(
                        "A man stranded on Mars must use his scientific knowledge and creativity to survive while NASA works to bring him home.",
                        Map.of("title", "The Martian", "genre", "Sci-Fi", "year", 2015)
                ),

                new Document(
                        "A teenager discovers he has extraordinary powers and must learn how to use them while dealing with the responsibilities of being a superhero.",
                        Map.of("title", "Spider-Man", "genre", "Action", "year", 2002)
                ),

                new Document(
                        "A young woman joins a dangerous competition where participants fight for survival in a dystopian society.",
                        Map.of("title", "The Hunger Games", "genre", "Dystopian", "year", 2012)
                )


        );
        vectorStore.add(movies);
    }

    public List<Document> similaritySearch(String text){
        return vectorStore.similaritySearch(SearchRequest
                        .builder()
                        .query(text)
                        .topK(2)
                        .build());
    }

//    public void ingestDataToVectorStore(String text){
//        Document document = new Document(text);
//
//        vectorStore.add(List.of(document));
//    }

    public String getJoke(String topic){



        String systemPrompt = """
                you are sarcastic joker, you make poetic jokes in 4 lines.
                You don't make jokes about politics.
                Give a joke on the topic: {topic}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(systemPrompt);
        String renderedText = promptTemplate.render(Map.of("topic", topic));

        var response = chatClient.prompt()
                .user(renderedText)
                .advisors(
                        new SimpleLoggerAdvisor()
                )
                .call()
                .entity(Joke.class);
//                .chatClientResponse();


//        return response.chatResponse().getResult().getOutput().getText();

        return response.text();
    }
}
