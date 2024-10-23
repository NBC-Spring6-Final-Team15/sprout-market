package com.sprarta.sproutmarket.domain.trade.service;

import com.sprarta.sproutmarket.domain.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;


}
