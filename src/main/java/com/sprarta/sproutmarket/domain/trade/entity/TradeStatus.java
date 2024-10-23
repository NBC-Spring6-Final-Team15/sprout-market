package com.sprarta.sproutmarket.domain.trade.entity;

import java.util.Arrays;

public enum TradeStatus {
    // TradeStatus를 정의하는 열거형
    WAITING,    // 요청보냄
    ACCEPTED,   // 요청 수락
    REJECTED,   // 요청 거부
    COMPLETED,  // 거래 완료
    CANCELLED;  // 거래 취소

    public static TradeStatus of(String role) {
        // 문자열로 전달된 역할을 TradeStatus 열거형으로 변환
        return Arrays.stream(TradeStatus.values()) // TradeStatus의 모든 값을 스트림으로 변환
            .filter(r -> r.name().equalsIgnoreCase(role)) // role과 대소문자 구분 없이 일치하는 값 필터링
            .findFirst() // 일치하는 첫 번째 값을 찾음
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 TradeStatus")); // 일치하는 값이 없으면 예외 발생
    }
}
