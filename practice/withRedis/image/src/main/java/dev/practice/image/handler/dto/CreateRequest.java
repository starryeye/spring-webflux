package dev.practice.image.handler.dto;

import lombok.Data;

@Data
public class CreateRequest {

    private final String id;
    private final String name;
    private final String url;
}
