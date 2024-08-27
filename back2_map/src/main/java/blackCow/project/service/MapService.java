package blackCow.project.service;

import blackCow.project.domain.FloorInfo;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.core.io.InputStreamResource;


public interface MapService {
    public InputStreamResource getFloorImage(int buildingId, String floor);

    public FloorInfo getNumOfFloors(int buildingId);

    public S3Object findS3Object(int buildingId, String floor);
}
