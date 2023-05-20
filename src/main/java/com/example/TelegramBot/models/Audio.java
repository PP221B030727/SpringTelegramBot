package com.example.TelegramBot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Value;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Audio {
    private String content;
}