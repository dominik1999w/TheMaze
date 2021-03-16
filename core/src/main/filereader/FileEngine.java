package filereader;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.IOException;

import db.DatabaseEngine;

public class FileEngine implements DatabaseEngine {
    private final String FILEPATH = "mapexample.txt";

    @Override
    public void readMap(CellListBuilder builder) {
        try {
            BufferedReader reader = new BufferedReader(Gdx.files.internal(FILEPATH).reader());
            String line = reader.readLine();
            int y = 0;
            while (line != null) {
                String[] types = line.split(" ");
                for (int x = 0; x < types.length; x++) {
                    builder.buildCell(Integer.parseInt(types[x]), x, y);
                }
                line = reader.readLine();
                y += 1;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("error reading map file");
        }
    }
}
