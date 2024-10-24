package com.sprarta.sproutmarket.domain.areas.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprarta.sproutmarket.domain.areas.entity.AdministrativeArea;
import com.sprarta.sproutmarket.domain.areas.repository.AdministrativeAreaRepository;
import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdministrativeAreaService {
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final AdministrativeAreaRepository administrativeAreaRepository;
    private final ObjectMapper objectMapper;

    /**
     * 행정구역 정보가 들어있는 GeoJson 파일을 가져와서 DB에 행정구역 정보를 입력합니다.
     * 처음 DB 세팅용, 혹은 의도치 않게 administrative_area 테이블에 수정이 일어났을 경우 테이블을 drop 하고 다시 넣는 용도
     * !!!!!!!!!!!절 대 개 발 자 용 코 드!!!!!!!!!!!!!
     * @param filePath : 행정구역 정보가 있는 geoJson 파일이 있는 경로입니다. 현재 루트폴더/json/해당파일로 설정돼있습니다.
     * @throws IOException 유효한 파일이 들어오지 않을 경우 발생하는 Exception
     */
    @Transactional
    public void insertGeoJsonData(String filePath) throws IOException {
        // GeoJSON 파일 읽기 및 역직렬화
        File file = new File(filePath);
        FeatureCollection featureCollection = objectMapper.readValue(file, FeatureCollection.class);

        List<AdministrativeArea> areas = new ArrayList<>();
        for (Feature feature : featureCollection.getFeatures()) {
            String admNm = feature.getProperties().get("adm_nm").toString();
            String admCd2 = feature.getProperties().get("adm_cd2").toString();
            String sgg = feature.getProperties().get("sgg").toString();
            String sido = feature.getProperties().get("sido").toString();
            String sidonm = feature.getProperties().get("sidonm").toString();
            String sggnm = feature.getProperties().get("sggnm").toString();
            String admCd = feature.getProperties().get("adm_cd").toString();

            // Geometry 를 JTS 의 MultiPolygon 으로 변환
            org.geojson.MultiPolygon geoJsonMultiPolygon = (org.geojson.MultiPolygon) feature.getGeometry();
            MultiPolygon jtsMultiPolygon = convertToJtsMultiPolygon(geoJsonMultiPolygon);

            // SRID 설정 (경도,위도 정보로 DB 조회할 수 있게 설정하는 ID)
            jtsMultiPolygon.setSRID(4326);

            //엔티티 생성
            AdministrativeArea area = AdministrativeArea.builder()
                    .admNm(admNm)
                    .admCd2(admCd2)
                    .sgg(sgg)
                    .sido(sido)
                    .sidonm(sidonm)
                    .sggnm(sggnm)
                    .admCd(admCd)
                    .geometry(jtsMultiPolygon)  // WKT 형식의 문자열을 저장합니다.
                    .build();

            areas.add(area);
        }

        //데이터 저장
        administrativeAreaRepository.saveAll(areas);
    }

    /**
     * 현재 역직렬화를 위해 geojson-jackson 으로 파일을 변환하는데,
     * MySQL 에 Multipolygon 으로 저장하기 위해 JTS 에서 제공하는 MultiPolygon 으로 변환합니다.
     *
     * @param geoJsonMultiPolygon GeoJSON MultiPolygon 타입입니다.
     * @return JTS MultiPolygon 타입으로 변환된 객체입니다.
     */
    private MultiPolygon convertToJtsMultiPolygon(org.geojson.MultiPolygon geoJsonMultiPolygon) {
        List<Polygon> polygons = geoJsonMultiPolygon.getCoordinates().stream()
                .map(this::createJtsPolygon)
                .toList();

        return geometryFactory.createMultiPolygon(polygons.toArray(new Polygon[0]));
    }

    /**
     * GeoJSON 의 다각형 좌표를 JTS Polygon 으로 생성합니다.
     *
     * @param polygonCoordinates GeoJSON 다각형의 좌표 리스트입니다.
     * @return JTS Polygon 객체입니다.
     */
    private Polygon createJtsPolygon(List<List<LngLatAlt>> polygonCoordinates) {
        List<LinearRing> linearRings = polygonCoordinates.stream()
                .map(this::createLinearRing)
                .toList();

        LinearRing shell = linearRings.get(0);
        LinearRing[] holes = linearRings.size() > 1
                ? linearRings.subList(1, linearRings.size()).toArray(new LinearRing[0])
                : null;

        return geometryFactory.createPolygon(shell, holes);
    }

    /**
     * GeoJSON 링 좌표를 JTS LinearRing 으로 변환합니다.
     *
     * @param ring GeoJSON 링의 좌표 리스트입니다.
     * @return JTS LinearRing 객체입니다.
     */
    private LinearRing createLinearRing(List<LngLatAlt> ring) {
        Coordinate[] coordinates = ring.stream()
                .map(coord -> new Coordinate(coord.getLongitude(), coord.getLatitude()))
                .toArray(Coordinate[]::new);

        return geometryFactory.createLinearRing(coordinates);
    }

    /**
     * double 타입의 위도, 경도를 입력하면 해당하는 행정구역을 반환합니다.
     * @param longitude : 경도 (예시 : 126.976889 )
     * @param latitude : 위도 (예시 : 37.575651 )
     * @return : 행정구역 (예시 : 서울특별시 마포구 합정동 )
     */
    public String findAdministrativeAreaByCoordinates(double longitude, double latitude) {
        String point = String.format("POINT(%f %f)",latitude, longitude);
        return administrativeAreaRepository.findAdministrativeAreaByPoint(point).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_ADMINISTRATIVE_AREA)
        );
    }

}


