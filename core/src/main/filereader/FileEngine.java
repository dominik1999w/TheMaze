package filereader;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;

import db.DatabaseEngine;
import map.generator.MapGenerator;
import types.WallTypes;

public class FileEngine implements DatabaseEngine {
    private final String PreferenceName = "TheMazePreference";
    private final String MapKey = "map";

    @Override
    public void readMap(CellListBuilder builder) {
        String[] rows = Gdx.app.getPreferences(
                PreferenceName).getString(MapKey).split("\n");
        int x = 0, y;
        for (String row : rows) {
            String[] entries = row.split("#");
            y = 0;

            for (String entry : entries) {
                String coordinates = entry.substring(1, entry.length() - 1);
                String[] dirs = coordinates.split(",");

                List<WallTypes> walls = new ArrayList<>();
                for (String dir : dirs) {
                    if (dir.isEmpty()) {
                        continue;
                    }
                    walls.add(WallTypes.valueOf(dir));
                }

                builder.buildCell(x, y, walls);
                y++;
            }
            x++;
        }
    }

    @Override
    public void saveMap(MapGenerator.Node[][] map) {
        int n = map.length;
        int m = map[0].length;
        StringBuilder mapBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                mapBuilder.append(map[i][j].toString()).append("#");
            }
            mapBuilder.append("\n");
        }

        Gdx.app.getPreferences(
                PreferenceName
        ).putString(MapKey, mapBuilder.toString()).flush();

    }

}
