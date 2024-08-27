package blackCow.project.service;

import blackCow.project.domain.FloorInfo;
import blackCow.project.exception.BuildingNotFoundException;
import blackCow.project.exception.FloorNotFoundException;
import blackCow.project.repository.BuildingRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
@PropertySource("classpath:application-env.properties")
public class MapServiceImpl implements MapService{

    private final AmazonS3 s3Client;
    private final BuildingRepository buildingRepository;


    @Value("${cloud.aws.s3.bucket}")
    String bucket;

    public InputStreamResource getFloorImage(int buildingId, String floor) {

        // S3에서 객체 가져오기
        S3Object s3Object = findS3Object(buildingId, floor);

        // Resource 객체 생성
        return new InputStreamResource(s3Object.getObjectContent());
    }

    public FloorInfo getNumOfFloors(int buildingId){
        checkData(buildingId);
        return buildingRepository.getFloorInfo(buildingId);
    }

    public S3Object findS3Object(int buildingId, String floor) {
        checkData(buildingId, floor);

        //제4공학관을 제1공학관으로 매핑
        if(buildingId == 124) {
            buildingId = 121;
        }
        //지하 검색 시 b,B상관없게 처리해주기
        if(floor.startsWith("B"))
            floor = "b" + floor.substring(1);
        else if(floor.toLowerCase().endsWith("f")){
            floor = floor.substring(0, floor.length()-1);
        }
        String key = buildingId + "/" + floor + ".png";

        return s3Client.getObject(new GetObjectRequest(bucket, key));
    }

    private void checkData(int buildingId, String floor) {
        if(!buildingRepository.isValidBuilding(buildingId))
            throw new BuildingNotFoundException("NOT VALID BuildingId");
        else if(!buildingRepository.isValidFloor(buildingId, floor)){
            String msg;
            FloorInfo floorInfo = buildingRepository.getFloorInfo(buildingId);

            if(floorInfo.getMinFloor()==0)
                msg = "F1~F"+floorInfo.getMaxFloor();
            else
                msg = "B"+(-1*floorInfo.getMinFloor())+"~F"+floorInfo.getMaxFloor();

            throw new FloorNotFoundException("NOT VALID FLOOR[building = "+buildingRepository.getBuildingName(buildingId)
                    +"/floor = "+msg+"]");
        }
    }

    private void checkData(int buildingId) {
        if(!buildingRepository.isValidBuilding(buildingId))
            throw new BuildingNotFoundException("NOT VALID BuildingId");
    }

}
