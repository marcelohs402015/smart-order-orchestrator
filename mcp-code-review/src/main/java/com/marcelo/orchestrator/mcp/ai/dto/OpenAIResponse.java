package com.marcelo.orchestrator.mcp.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenAIResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("object")
    private String object;
    
    @JsonProperty("created")
    private Long created;
    
    @JsonProperty("choices")
    private List<Choice> choices;
    
    @JsonProperty("usage")
    private Usage usage;
    
    public boolean isSuccess() {
        return choices != null && !choices.isEmpty() 
            && choices.get(0) != null 
            && choices.get(0).getMessage() != null
            && choices.get(0).getMessage().getContent() != null;
    }
    
    public String getContent() {
        if (!isSuccess()) {
            return null;
        }
        return choices.get(0).getMessage().getContent().trim();
    }
    
    @Getter
    @Setter
    public static class Choice {
        @JsonProperty("index")
        private Integer index;
        
        @JsonProperty("message")
        private Message message;
        
        @JsonProperty("finish_reason")
        private String finishReason;
    }
    
    @Getter
    @Setter
    public static class Message {
        @JsonProperty("role")
        private String role;
        
        @JsonProperty("content")
        private String content;
    }
    
    @Getter
    @Setter
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
