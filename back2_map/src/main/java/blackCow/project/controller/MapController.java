package blackCow.project.controller;


import blackCow.project.domain.FloorInfo;
import blackCow.project.service.MapService;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/map/buildings")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping("/floors")
    public ResponseEntity<InputStreamResource> getFloorInfo2(
            @RequestParam int bId,
            @RequestParam String f) {
        log.info("Request image of building = {} & floor = {}", bId, f);

        //이미지 받기
        InputStreamResource image = mapService.getFloorImage(bId, f);

        //s3Object 받기
        S3Object s3Object = mapService.findS3Object(bId, f);

        // HTTP 헤더 설정
        HttpHeaders httpHeaders = getHttpHeaders(s3Object);

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(image);

    }

    @GetMapping("/{buildingId}/floor-info")
    public FloorInfo numOfFloors(@PathVariable int buildingId){
        FloorInfo info = mapService.getNumOfFloors(buildingId);
        log.info("Get num of floors of building {} : minFloor = {} & maxFloor = {}", buildingId, info.getMinFloor(), info.getMaxFloor());
        return info;
    }

    private static HttpHeaders getHttpHeaders(S3Object s3Object) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_PNG);
        httpHeaders.setContentLength(s3Object.getObjectMetadata().getContentLength());

        //Cache-content
        httpHeaders.setCacheControl("max-age=120"); //120초 유지

        //Last-modified
        Date lastModified = s3Object.getObjectMetadata().getLastModified(); //s3 객체 마지막 수정 시간 적용하기
        httpHeaders.setLastModified(lastModified.getTime());
        return httpHeaders;
    }

}



