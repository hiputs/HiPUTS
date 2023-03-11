package pl.edu.agh.hiputsmapgenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputsmapgenerator.model.Lane;
import pl.edu.agh.hiputsmapgenerator.model.Node;
import pl.edu.agh.hiputsmapgenerator.model.Patch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;


public class HiPUTSMapGeneratorApplication {

    private static final String MAP_PATH = "..\\data\\";

    private static final Dims mapDims = new Dims(9,9); // all nodes in map

    private static Dims patchDims = new Dims(3,3); // nodes in patch
    private static int laneLength = 500; //meters


    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        if(args.length > 3) {
            laneLength = Integer.parseInt(args[1]);
            patchDims = new Dims(Integer.parseInt(args[2]),Integer.parseInt(args[3]));
        }

        mapDims.setDimX(patchDims.getDimX() * (int) mapDims.getDimX() / patchDims.getDimX());
        mapDims.setDimY(patchDims.getDimY() * (int) mapDims.getDimY() / patchDims.getDimY());

        genMap(1);
       genMap(2);
//        genMap(4);
        // genMap(8);
        // genMap(12);
        // genMap(16);
        // genMap(20);
        // genMap(24);
        // genMap(28);
        // genMap(32);
        // genMap(64);

    }

    private static void genMap(int workerCount) throws FileNotFoundException, UnsupportedEncodingException {
        Dims totalSize = new Dims(mapDims.getDimX() * workerCount, mapDims.getDimY());

        Node[][] map = new Node[totalSize.getDimX()][totalSize.getDimY()];

        fillMap(map, totalSize);

        List<Lane> edges = new LinkedList<>();
        createLanesHorizontal(map, totalSize, edges);
        createTorusLanesHorizontal(map, totalSize, edges);
        createLanesVertical(map, totalSize, edges);
        createTorusLanesVertical(map, totalSize, edges);

        List<Patch> patches = new LinkedList<>();
        createPatches(map, totalSize, patches);

        new File(MAP_PATH + "square-map" + workerCount).mkdir();
        saveEdges(edges, workerCount);
        saveNodes(map, workerCount);
        savePatches(patches, workerCount);
    }

    private static void savePatches(List<Patch> patches, int workerCount) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(MAP_PATH + "square-map" + workerCount + "/patches.csv", "UTF-8");
        writer.println("id,neighbouring_patches_ids");
        patches.forEach(p -> writer.println(p.toString()));
        writer.close();
    }

    private static void saveNodes(Node[][] map, int workerCount) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(MAP_PATH + "square-map" + workerCount + "/nodes.csv", "UTF-8");
        writer.println("id,longitude,latitude,is_crossroad,patch_id,tags");

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                writer.println(map[i][j].toString());
            }
        }

        writer.close();
    }

    private static void saveEdges(List<Lane> edges, int workerCount) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(MAP_PATH + "square-map" + workerCount + "/edges.csv", "UTF-8");
        writer.println("source,target,length,max_speed,is_priority_road,is_one_way,patch_id,tags");
        edges.forEach(e -> writer.println(e.toString()));
        writer.close();
    }

    private static void createPatches(Node[][] map, Dims dims, List<Patch> patches) {
//        int n = size / 2;
        int xPatches = dims.getDimX()/patchDims.getDimX();
        int yPatches = dims.getDimY()/ patchDims.getDimY();

        for (int i = 0; i < xPatches; i++) {
            for (int j = 0; j < yPatches; j++) {
                Patch patch = new Patch(String.format("P%s-%s", i, j));

                //left
                if (j > 0) {
                    patch.getNeighbouring_patches_ids().add(String.format("P%s-%s", i, j - 1));
                }

                //right
                if (j < yPatches - 1) {
                    patch.getNeighbouring_patches_ids().add(String.format("P%s-%s", i, j + 1));
                }


                //up
                if (i > 0) {
                    patch.getNeighbouring_patches_ids().add(String.format("P%s-%s", i - 1, j));
                }


                //down
                if (i < xPatches - 1) {
                    patch.getNeighbouring_patches_ids().add(String.format("P%s-%s", i + 1, j));
                }

                patches.add(patch);

                List<Lane> allPatchLanes = new LinkedList<>();

                int dimX = patchDims.getDimX();
                int dimY = patchDims.getDimY();

                for(int k=0;k<patchDims.getDimX();k++) {
                    for (int l = 0; l < patchDims.getDimY(); l++) {
                        map[i * dimX + k][j * dimY + l].setPatch_id(patch.getId());
                        allPatchLanes.addAll(map[i * dimX + k][j * dimY + l].getLanes());
                    }
                }

                allPatchLanes.forEach(lane -> lane.setPatch_id(patch.getId()));
            }
        }
    }

    private static void createLanesVertical(Node[][] map, Dims dims, List<Lane> edges) {
        for (int i = 0; i < dims.getDimY(); i++) {
            for (int j = 0; j < dims.getDimX() -1; j++) {
                Lane lane1 = new Lane(map[j][i].getId(), map[j+1][i].getId(), laneLength);
                Lane lane2 = new Lane(map[j+1][i].getId(), map[j][i].getId(), laneLength);
                map[j][i].getLanes().add(lane1);
                map[j+1][i].getLanes().add(lane2);

                edges.add(lane1);
                edges.add(lane2);
            }
        }
    }

    private static void createTorusLanesVertical(Node[][] map, Dims dims, List<Lane> edges) {
        int lastIdx = dims.getDimY() - 1;

        for (int j = 0; j < dims.getDimX(); j++) {
            Lane lane1 = new Lane(map[j][0].getId(), map[j][lastIdx].getId(), laneLength);
            Lane lane2 = new Lane(map[j][lastIdx].getId(), map[j][0].getId(), laneLength);
            map[j][0].getLanes().add(lane1);
            map[j][lastIdx].getLanes().add(lane2);

            edges.add(lane1);
            edges.add(lane2);
        }

    }

    private static void createLanesHorizontal(Node[][] map, Dims dims, List<Lane> edges) {
        for (int i = 0; i < dims.getDimX(); i++) {
            for (int j = 0; j < dims.getDimY() - 1; j++) {
                Lane lane1 = new Lane(map[i][j].getId(), map[i][j + 1].getId(), laneLength);
                Lane lane2 = new Lane(map[i][j + 1].getId(), map[i][j].getId(), laneLength);

                map[i][j].getLanes().add(lane1);
                map[i][j + 1].getLanes().add(lane2);

                edges.add(lane1);
                edges.add(lane2);
            }
        }
    }

    private static void createTorusLanesHorizontal(Node[][] map, Dims dims, List<Lane> edges) {
        int lastIdx = dims.getDimX() - 1;

        for (int j = 0; j < dims.getDimY(); j++) {
            Lane lane1 = new Lane(map[0][j].getId(), map[lastIdx][j].getId(), laneLength);
            Lane lane2 = new Lane(map[lastIdx][j].getId(), map[0][j].getId(), laneLength);

            map[0][j].getLanes().add(lane1);
            map[lastIdx][j].getLanes().add(lane2);

            edges.add(lane1);
            edges.add(lane2);
        }
    }

    private static void fillMap(Node[][] map, Dims dims) {
        float multiplier = (float) laneLength / 100;
        for (int i = 0; i < dims.getDimX(); i++) {
            for (int j = 0; j < dims.getDimY(); j++) {
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
    private static class Dims {
        private int dimX; // longitude
        private int dimY; // latitude

    }

}
