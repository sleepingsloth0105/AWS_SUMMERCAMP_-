package blackCow.project.repository;

import blackCow.project.domain.FloorInfo;

public interface BuildingRepository {
    public FloorInfo getFloorInfo(int buildingId);
    public boolean isValidFloor(int buildingId, String floor);
    public boolean isValidBuilding(int buildingId);
    public String getBuildingName(int buildingId);
}
