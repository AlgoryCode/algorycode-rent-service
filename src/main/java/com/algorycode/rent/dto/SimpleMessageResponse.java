package com.algorycode.rent.dto;

/**
 * Yalnızca kullanıcıya gösterilecek kısa metin içeren başarı yanıtları için kullanılır (ör. 201
 * Created gövdesi).
 */
public record SimpleMessageResponse(String message) {}
