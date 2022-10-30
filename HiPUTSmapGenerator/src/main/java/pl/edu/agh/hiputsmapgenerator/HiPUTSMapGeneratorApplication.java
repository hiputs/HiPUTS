package pl.edu.agh.hiputsmapgenerator;

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

    private static final int BASE = 1000;

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        genMap(1);
        genMap(2);
        genMap(4);
        genMap(8);
        genMap(12);
        genMap(16);
        genMap(20);
        genMap(24);
        genMap(28);
        genMap(32);
        genMap(64);

    }

    private static void genMap(int workerCount) throws FileNotFoundException, UnsupportedEncodingException {
        int totalSize = BASE * workerCount;
        int size = (int) Math.sqrt(totalSize);
        Node[][] map = new Node[size][size];

        fillMap(map, size);

        List<Lane> edges = new LinkedList<>();
        createLanesHorizontal(map, size, edges);
        createLanesVertical(map, size, edges);

        List<Patch> patches = new LinkedList<>();
        createPatches(map, size, patches);

        new File("square-map" + workerCount).mkdir();
        saveEdges(edges, workerCount);
        saveNodes(map, workerCount);
        savePatches(patches, workerCount);
    }

    private static void savePatches(List<Patch> patches, int workerCount) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("square-map" + workerCount + "/patches.csv", "UTF-8");
        writer.println("id,neighbouring_patches_ids");
        patches.forEach(p -> writer.println(p.toString()));
        writer.close();
    }

    private static void saveNodes(Node[][] map, int workerCount) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("square-map" + workerCount + "/nodes.csv", "UTF-8");
        writer.println("id,longitude,latitude,is_crossroad,patch_id,tags");

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map.length; j++) {
                writer.println(map[i][j].toString());
            }
        }

        writer.close();
    }

    private static void saveEdges(List<Lane> edges, int workerCount) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("square-map" + workerCount + "/edges.csv", "UTF-8");
        writer.println("source,target,length,max_speed,is_priority_road,is_one_way,patch_id,tags");
        edges.forEach(e -> writer.println(e.toString()));
        writer.close();
    }

    private static void createPatches(Node[][] map, int size, List<Patch> patches) {
        int n = size / 2;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Patch patch = new Patch(String.format("P%s-%s", i, j));

                //left
                if (j > 0) {
                    patch.getNeighbouring_patches_ids().add(String.format("P%s-%s", i, j - 1));
                }

                //right
                if (j < n - 1) {
                    patch.getNeighbouring_patches_ids().add(String.format("P%s-%s", i, j + 1));
                }


                //up
                if (i > 0) {
                    patch.getNeighbouring_patches_ids().add(String.format("P%s-%s", i - 1, j));
                }


                //down
                if (i < n - 1) {
                    patch.getNeighbouring_patches_ids().add(String.format("P%s-%s", i + 1, j));
                }

                patches.add(patch);

                map[i * 2][j * 2].setPatch_id(patch.getId());
                map[i * 2 + 1][j * 2].setPatch_id(patch.getId());
                map[i * 2][j * 2 + 1].setPatch_id(patch.getId());
                map[i * 2 + 1][j * 2 + 1].setPatch_id(patch.getId());
            }
        }
    }

    private static void createLanesVertical(Node[][] map, int size, List<Lane> edges) {
        for (int i = 0; i < size -1; i++) {
            for (int j = 0; j < size; j++) {
                Lane lane1 = new Lane(map[j][i].getId(), map[j][i + 1].getId());
                Lane lane2 = new Lane(map[j][i + 1].getId(), map[j][i].getId());
                map[j][i].getLanes().add(lane1);
                map[j][i + 1].getLanes().add(lane2);

                edges.add(lane1);
                edges.add(lane2);
            }
        }
    }

    private static void createLanesHorizontal(Node[][] map, int size, List<Lane> edges) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size - 1; j++) {
                Lane lane1 = new Lane(map[i][j].getId(), map[i][j + 1].getId());
                Lane lane2 = new Lane(map[i][j + 1].getId(), map[i][j].getId());

                map[i][j].getLanes().add(lane1);
                map[i][j + 1].getLanes().add(lane2);

                edges.add(lane1);
                edges.add(lane2);
            }
        }
    }

    private static void fillMap(Node[][] map, int size) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                map[i][j] = Node.builder()
                        .id(String.format("N%d-%d", i, j))
                        .latitude(String.valueOf(i))
                        .longitude(String.valueOf(j))
                        .build();
            }
        }
    }

}
