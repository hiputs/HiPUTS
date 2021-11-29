package model.map;

import model.actor.LaneEditor;
import model.id.LaneId;

public class LaneRemote implements LaneReadWrite {

    /**
     * Unique lane identifier.
     */
    private LaneId id;

    private LaneEditor laneEditor;
}
