package pl.edu.agh.hiputsmapgenerator.model;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class Patch {
    private String id;
    private List<String> neighbouring_patches_ids = new LinkedList<>();

    public Patch(String id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return id + ", " +
                listToString();
    }

    private String listToString(){
        return String.join(":ELEM:", neighbouring_patches_ids);
    }
}
