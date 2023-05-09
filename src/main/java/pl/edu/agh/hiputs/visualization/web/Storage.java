package pl.edu.agh.hiputs.visualization.web;

import pl.edu.agh.hiputs.model.map.patch.Patch;

import java.util.List;

public class Storage {

    private static List<Patch> patches;

    public static List<Patch> getPatches() {
        return patches;
    }

    public static void setPatches(List<Patch> patches) {
        Storage.patches = patches;
    }
}
