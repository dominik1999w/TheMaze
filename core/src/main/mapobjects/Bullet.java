package mapobjects;

import map.generator.MapGenerator;

public class Bullet {
    private CollisionFinder collisionFinder;

    Bullet(MapGenerator mapGenerator) {
        collisionFinder = new CollisionFinder(mapGenerator, 0.05f);
    }
}
