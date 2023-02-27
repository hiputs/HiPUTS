package pl.edu.agh.hiputsmapgenerator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Builder
public class Node {
    private String id;
    private String longitude;
    private String latitude;
    @Builder.Default
    private boolean is_crossroad = true;
    private String patch_id;
    @Builder.Default
    private String tags = "";

    @Builder.Default
    private List<Lane> lanes = new LinkedList<>();


    @Override
    public String toString() {
        return
                id + "," +
                        longitude + "," +
                        latitude + "," +
                        is_crossroad + "," +
                        patch_id + ",";
    }
}
