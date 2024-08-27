package blackCow.project.domain;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FloorInfo {
    private int minFloor;
    private int maxFloor;


    public FloorInfo(int minFloor, int maxFloor) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }
}
