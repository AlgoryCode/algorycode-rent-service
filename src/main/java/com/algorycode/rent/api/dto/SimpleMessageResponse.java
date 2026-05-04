package com.algorycode.rent.api.dto;

/**
 * Yalnızca kullanıcıya gösterilecek kısa metin içeren başarı yanıtları için kullanılır (ör. 201
 * Created gövdesi).
 */
public record SimpleMessageResponse(String message) {}
