package pl.edu.agh.hiputsmapgenerator.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Lane {
    private String source;
    private String target;
    private int length = 100;
    private int max_speed = 50;
    private boolean is_priority_road = false;
    private boolean is_one_way = false;
    private String patch_id;
    private String tags = "";

    public Lane(String source, String target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public String toString() {
        return source + "," +
                target + "," +
                length + "," +
                max_speed + "," +
                is_priority_road + "," +
                is_one_way + "," +
                patch_id + ",";
    }
}
