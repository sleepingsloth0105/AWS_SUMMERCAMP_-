package blackCow.project.repository;


import blackCow.project.domain.Building;
import blackCow.project.domain.FloorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Repository
public class MemoryBuildingRepository implements BuildingRepository{
    private static final Map<Integer, Building> buildingFloorMap = new HashMap<>();

    public MemoryBuildingRepository() {
        buildingFloorMap.put(121, new Building(121, "제 1 공학관", new FloorInfo(0, 10)));
        buildingFloorMap.put(122, new Building(122, "제 2 공학관", new FloorInfo(-1, 7)));
        buildingFloorMap.put(123, new Building(123, "제 3 공학관", new FloorInfo(-1, 7)));
        buildingFloorMap.put(124, new Building(124, "제 4 공학관", new FloorInfo(0, 10)));
    }

    public FloorInfo getFloorInfo(int buildingId){
        return buildingFloorMap.get(buildingId).getFloorInfo();
    }

    public boolean isValidFloor(int buildingId, String floor){
        try {
            int floorNumber;
            if (floor.toLowerCase().startsWith("b")) {
                floorNumber = Integer.parseInt(floor.substring(1));
                return floorNumber <= (-1*getFloorInfo(buildingId).getMinFloor()) && floorNumber > 0;
            } else {
                if(floor.toLowerCase().endsWith("f")){
                    floor = floor.substring(0, floor.length()-1);
                }
                floorNumber = Integer.parseInt(floor);
                return floorNumber <= getFloorInfo(buildingId).getMaxFloor();
            }
        } catch (NumberFormatException e) {
            // 유효하지 않은 floor 입력 처리
            return false;
        }

    }
    public boolean isValidBuilding(int buildingId) {
        return buildingFloorMap.containsKey(buildingId);
    }

    @Override
    public String getBuildingName(int buildingId) {
        Building building = buildingFloorMap.get(buildingId);
        return building.getBuildingName();
    }

}
