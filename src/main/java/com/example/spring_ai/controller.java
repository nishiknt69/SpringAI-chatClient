package com.example.spring_ai;

import com.example.spring_ai.tool.FlightBookingTools;
import com.example.spring_ai.tool.TravellingTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class controller {

    private final ChatClient chatClient;
    private final TravellingTools travellingTools;
    private final FlightBookingTools flightBookingTools;
    private final ChatMemory chatMemory;

    @PostMapping("/chat")
    public String chat(@RequestBody String message, @RequestParam String userId){

        String systemPrompt = String.format("""
                You are a friendly flight booking assistant.
                Use the available tools to create, view or update bookings.
                Always confirms actions with the user when possible.
                
                IMPORTANT: The current user's ID is "%s".
                When calling tools that require a userId, Always use this exact value.
                """, userId);

        return chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .tools(travellingTools, flightBookingTools)
                .advisors(a -> a
                        .advisors(
                                MessageChatMemoryAdvisor.builder(chatMemory)
                                        .build()
                        )
                        .param("chat_memory_conversation_id", userId)

                )
                .call()
                .content();
    }
}
