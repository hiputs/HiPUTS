package pl.edu.agh.hiputsmapgenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputsmapgenerator.model.Lane;
import pl.edu.agh.hiputsmapgenerator.model.Node;
import pl.edu.agh.hiputsmapgenerator.model.Patch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.sqrt;


public class HiPUTSMapGeneratorApplication {

    private static final String MAP_PATH = "..\\data\\";
    private static final Pair mapDims = new Pair(256,256); // all nodes in map for one worker
    private static final String MAP_PATCHES_NAME = "_"+mapDims.toString();
    private static Pair patchDims = new Pair(2,2); // nodes in patch
    private static int laneLength = 500; //meters


    public static void main(String[] args) throws IOException {

        if(args.length > 3) {
            laneLength = Integer.parseInt(args[1]);
            patchDims = new Pair(Integer.parseInt(args[2]),Integer.parseInt(args[3]));
        }

        mapDims.setX(patchDims.getX() * (int) mapDims.getX() / patchDims.getX());
        mapDims.setY(patchDims.getY() * (int) mapDims.getY() / patchDims.getY());

        // genMap(1);
        // genMap(3);
        // genMap(4);
        // genMap(8);
        genMap(9);
        // genMap(12);
        // genMap(16);
        // genMap(20);
        // genMap(24);
        // genMap(28);
        // genMap(36);
        // genMap(48);
        // genMap(64);
        // genMap(128);
        // genMap(256);
        // genMap(384);

    }

    private static void genMap(int workerCount) throws IOException {
        Pair multi = calc_dims_multipliers(workerCount);
        Pair totalSize = new Pair(mapDims.getX() * multi.getX(), mapDims.getY()*multi.getY());

        Node[][] map = new Node[totalSize.getX()][totalSize.getY()];

        fillMap(map, totalSize);

        List<Lane> edges = new LinkedList<>();
        createLanesHorizontal(map, totalSize, edges);
        createTorusLanesHorizontal(map, totalSize, edges);
        createLanesVertical(map, totalSize, edges);
        createTorusLanesVertical(map, totalSize, edges);

        List<Patch> patches = new LinkedList<>();
        createPatches(map, totalSize, patches);

        new File(getDirPath(workerCount)).mkdir();
        saveEdges(edges, workerCount);
        saveNodes(map, workerCount);
        savePatches(patches, workerCount);
    }

    private static Pair calc_dims_multipliers(int workerCount){
        int multiX = (int) sqrt(workerCount);

        int diff = 1;
        while(workerCount % multiX != 0 && multiX >= 1){ // finds number near sqrt which divides workerCount without rest
            multiX += diff;

            if(diff < 0){
                diff -= 1;
            }
            else{
                diff += 1;
            }
            diff *= -1;
        }
        if(multiX < 1){
            multiX = 1;
        }

        return new Pair(multiX, workerCount / multiX);
    }


    private static void savePatches(List<Patch> patches, int workerCount) throws IOException {
        PrintWriter writer = new PrintWriter(getDirPath(workerCount) + "/patches.csv", StandardCharsets.UTF_8);
        writer.println("id,neighbouring_patches_ids");
        patches.forEach(p -> writer.println(p.toString()));
        writer.close();
    }

    private static String getDirPath(int workerCount) {
        return MAP_PATH + "square-map" + workerCount + MAP_PATCHES_NAME ;
    }

    private static void saveNodes(Node[][] map, int workerCount) throws IOException {
        PrintWriter writer = new PrintWriter(getDirPath(workerCount) + "/nodes.csv", StandardCharsets.UTF_8);
        writer.println("id,longitude,latitude,is_crossroad,patch_id,tags");

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                writer.println(map[i][j].toString());
            }
        }

        writer.close();
    }

    private static void saveEdges(List<Lane> edges, int workerCount) throws IOException {
        PrintWriter writer = new PrintWriter(getDirPath(workerCount)+ "/edges.csv", StandardCharsets.UTF_8);
        writer.println("source,target,length,max_speed,is_priority_road,is_one_way,patch_id,tags");
        edges.forEach(e -> writer.println(e.toString()));
        writer.close();
    }

    private static void createPatches(Node[][] map, Pair dims, List<Patch> patches) {
//        int n = size / 2;
        int xPatches = dims.getX()/patchDims.getX();
        int yPatches = dims.getY()/ patchDims.getY();

        for (int i = 0; i < xPatches; i++) {
            for (int j = 0; j < yPatches; j++) {
                Patch patch = new Patch(String.format("%s-%s", i, j));

                //left
                if (j > 0) {
                    patch.getNeighbouring_patches_ids().add(String.format("%s-%s", i, j - 1));
                }

                //right
                if (j < yPatches - 1) {
                    patch.getNeighbouring_patches_ids().add(String.format("%s-%s", i, j + 1));
                }

                //up
                if (i > 0) {
                    patch.getNeighbouring_patches_ids().add(String.format("%s-%s", i - 1, j));
                }

                //down
                if (i < xPatches - 1) {
                    patch.getNeighbouring_patches_ids().add(String.format("%s-%s", i + 1, j));
                }

                // torus neighbours:

                // left
                if (j == 0) {
                    patch.getNeighbouring_patches_ids().add(String.format("%s-%s", i, yPatches - 1));
                }
                // right
                if (j == yPatches - 1) {
                    patch.getNeighbouring_patches_ids().add(String.format("%s-%s", i, 0));
                }
                //up
                if (i == 0) {
                    patch.getNeighbouring_patches_ids().add(String.format("%s-%s", xPatches - 1, j));
                }
                //down
                if (i == xPatches - 1) {
                    patch.getNeighbouring_patches_ids().add(String.format("%s-%s", 0, j));
                }

                patches.add(patch);

                List<Lane> allPatchLanes = new LinkedList<>();

                int dimX = patchDims.getX();
                int dimY = patchDims.getY();

                for(int k = 0; k<patchDims.getX(); k++) {
                    for (int l = 0; l < patchDims.getY(); l++) {
                        map[i * dimX + k][j * dimY + l].setPatch_id(patch.getId());
                        allPatchLanes.addAll(map[i * dimX + k][j * dimY + l].getLanes());
                    }
                }

                allPatchLanes.forEach(lane -> lane.setPatch_id(patch.getId()));
            }
        }
    }

    private static void createLanesVertical(Node[][] map, Pair dims, List<Lane> edges) {
        for (int i = 0; i < dims.getY(); i++) {
            for (int j = 0; j < dims.getX() -1; j++) {
                Lane lane1 = new Lane(map[j][i].getId(), map[j+1][i].getId(), laneLength);
                Lane lane2 = new Lane(map[j+1][i].getId(), map[j][i].getId(), laneLength);
                map[j][i].getLanes().add(lane1);
                map[j+1][i].getLanes().add(lane2);

                edges.add(lane1);
                edges.add(lane2);
            }
        }
    }

    private static void createTorusLanesVertical(Node[][] map, Pair dims, List<Lane> edges) {
        int lastIdx = dims.getY() - 1;

        for (int j = 0; j < dims.getX(); j++) {
            Lane lane1 = new Lane(map[j][0].getId(), map[j][lastIdx].getId(), laneLength);
            Lane lane2 = new Lane(map[j][lastIdx].getId(), map[j][0].getId(), laneLength);
            map[j][0].getLanes().add(lane1);
            map[j][lastIdx].getLanes().add(lane2);

            edges.add(lane1);
            edges.add(lane2);
        }

    }

    private static void createLanesHorizontal(Node[][] map, Pair dims, List<Lane> edges) {
        for (int i = 0; i < dims.getX(); i++) {
            for (int j = 0; j < dims.getY() - 1; j++) {
                Lane lane1 = new Lane(map[i][j].getId(), map[i][j + 1].getId(), laneLength);
                Lane lane2 = new Lane(map[i][j + 1].getId(), map[i][j].getId(), laneLength);

                map[i][j].getLanes().add(lane1);
                map[i][j + 1].getLanes().add(lane2);

                edges.add(lane1);
                edges.add(lane2);
            }
        }
    }

    private static void createTorusLanesHorizontal(Node[][] map, Pair dims, List<Lane> edges) {
        int lastIdx = dims.getX() - 1;

        for (int j = 0; j < dims.getY(); j++) {
            Lane lane1 = new Lane(map[0][j].getId(), map[lastIdx][j].getId(), laneLength);
            Lane lane2 = new Lane(map[lastIdx][j].getId(), map[0][j].getId(), laneLength);

            map[0][j].getLanes().add(lane1);
            map[lastIdx][j].getLanes().add(lane2);

            edges.add(lane1);
            edges.add(lane2);
        }
    }

    private static void fillMap(Node[][] map, Pair dims) {
        float multiplier = (float) laneLength / 100;
        for (int i = 0; i < dims.getX(); i++) {
            for (int j = 0; j < dims.getY(); j++) {
                map[i][j] = Node.builder()
                        .id(String.format("N%d-%d", i, j))
                        .latitude(String.valueOf(j*multiplier))
                        .longitude(String.valueOf(i*multiplier))
                        .build();
            }
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Pair {
        private int x; // in dims: longitude
        private int y; // in dims: latitude

        public String toString(){
            return x+"x"+y;
        }

    }

}
