package com.sprarta.sproutmarket.domain.areas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprarta.sproutmarket.domain.areas.dto.AdministrativeAreaRequestDto;
import com.sprarta.sproutmarket.domain.areas.repository.AdministrativeAreaRepository;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.MultiPolygon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministrativeAreaServiceTest {
    @Mock
    GeometryFactory geometryFactory;

    @Mock
    AdministrativeAreaRepository administrativeAreaRepository;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    AdministrativeAreaService administrativeAreaService;

    @Test
    void 정상적으로_geojson_파일이_객체로_변환돼서_DB에_접근하는지_확인 () throws IOException {
        //given
        String filePath = "test.geojson";

        FeatureCollection featureCollection = new FeatureCollection();
        Feature feature = new Feature();
        MultiPolygon multiPolygon = new MultiPolygon();

        feature.setGeometry(multiPolygon);
        feature.setProperty("adm_nm", "어쩌구시 어쩌구 어쩌구동");
        feature.setProperty("adm_cd2", "1111053000");
        feature.setProperty("sgg", "11110");
        feature.setProperty("sido", "11");
        feature.setProperty("sidonm", "어쩌구시");
        feature.setProperty("sggnm", "어쩌구");
        feature.setProperty("adm_cd", "11010530");
        featureCollection.setFeatures(Collections.singletonList(feature));

        // objectMapper.readValue가 호출될 때 featureCollection을 반환하도록 설정
        when(objectMapper.readValue(any(File.class), eq(FeatureCollection.class)))
                .thenReturn(featureCollection);

        // when
        administrativeAreaService.insertGeoJsonData(filePath);

        // then
        verify(administrativeAreaRepository, times(1)).saveAll(anyList());
    }

    @Test
    void 정상적으로_위도_경도가_잘_입력되는지_확인() {
        // given
        //경도, 위도
        AdministrativeAreaRequestDto requestDto = new AdministrativeAreaRequestDto(126.976889, 37.575651);
        //예상 반환
        String expectedAreaName = "서울특별시 종로구";
        when(administrativeAreaRepository.findAdministrativeAreaByPoint(anyString()))
                .thenReturn(Optional.of(expectedAreaName));

        // when
        administrativeAreaService.findAdministrativeAreaByCoordinates(requestDto.getLongitude(), requestDto.getLatitude());

        // then: 포인트 문자열이 예상한 대로 생성되었는지 검증
        String expectedPoint = String.format("POINT(%f %f)", requestDto.getLatitude(), requestDto.getLongitude());
        verify(administrativeAreaRepository, times(1)).findAdministrativeAreaByPoint(eq(expectedPoint));
    }
}