package blackCow.project.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Building {

    private int buildingId;
    private String buildingName;
    private FloorInfo floorInfo;

    public Building(int buildingId, String buildingName, FloorInfo floorInfo) {
        this.buildingId = buildingId;
        this.buildingName = buildingName;
        this.floorInfo = floorInfo;
    }
}
