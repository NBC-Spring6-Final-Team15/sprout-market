package com.sprarta.sproutmarket.domain.areas.controller;

import com.sprarta.sproutmarket.domain.areas.dto.AdmNameDto;
import com.sprarta.sproutmarket.domain.areas.dto.AdministrativeAreaRequestDto;
import com.sprarta.sproutmarket.domain.areas.service.AdministrativeAreaService;
import com.sprarta.sproutmarket.domain.common.ApiResponse;
import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdministrativeAreaController {
    private final AdministrativeAreaService administrativeAreaService;

    /**
     * geoJson 파일을 데이터베이스에 삽입하는 작업입니다.
     * 처음에 DB 세팅을 할 때, 해당 테이블에 수정이 일어나서 정합성이 깨졌을 때, 행정구역이 변해서 새로운 파일로 DB에 넣어야 할 때 필요합니다.
     */
    @PostMapping("/test/addGeoJson")
    public ResponseEntity<ApiResponse<String>> addGeoJson() {
        try {
            administrativeAreaService.insertGeoJsonData("json/HangJeongDong.geojson");
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.createSuccess("Created",201,"DB에 성공적으로 geojson 파일이 삽입됐습니다."));
        } catch (IOException e) {
            throw new ApiException(ErrorStatus.BAD_REQUEST_INVALID_FILE);
        }
    }

    /**
     * 어떤 특정한 좌표(위도,경도)를 받아와서 특정 행정동을 반환합니다.
     * @param requestDto : 위도,경도를 double 로 받는 DTO
     * @return : 행정동 String 반환
     */
    @PostMapping("/test/getHJD")
    public ResponseEntity<ApiResponse<String>> getHMD(@RequestBody AdministrativeAreaRequestDto requestDto) {
        return ResponseEntity.ok
                (ApiResponse.onSuccess(
                        administrativeAreaService.
                                findAdministrativeAreaByCoordinates(requestDto.getLongitude(), requestDto.getLatitude())));
    }


    /**
     * 어떤 특정 행정동 문자열을 불러와서 주변 반경의 행정동 리스트를 반환합니다.
     * @param admNm : 행정동 문자열 (예시 : 경상남도 산청군 생초면)
     * @return 행정동 이름 리스트 반환
     */
    @GetMapping("/test/getAreas")
    public ResponseEntity<ApiResponse<List<String>>> getAreas(@RequestParam String admNm) {
        List<String> areas = administrativeAreaService.findAdmNameListByAdmName(admNm);
        return ResponseEntity.ok(ApiResponse.onSuccess(areas));
    }
}