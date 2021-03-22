package map.generator;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import types.WallTypes;

public class Node {
    private final int x, y;
    private final List<Vector2> wallRelativePositions;

    Node(int x, int y) {
        this.x = x;
        this.y = y;
        wallRelativePositions = new ArrayList<>();
    }

    void removeEdge(Vector2 direction) {
        wallRelativePositions.add(direction);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("[");
        for (int i = 0; i < wallRelativePositions.size(); i++) {
            Vector2 relativePos = wallRelativePositions.get(i);
            res.append(WallTypes.valueOfRelativePos(relativePos));
            if(i!=wallRelativePositions.size()-1) {
                res.append(",");
            }
        }
        res.append("]");
        return res.toString();
    }
}
