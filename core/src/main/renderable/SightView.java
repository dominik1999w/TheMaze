package renderable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;

import map.Map;
import map.MapConfig;
import physics.mapcollision.LineMapCollisionDetector;
import util.Point2D;

public class SightView implements Renderable {
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final FrameBuffer fbo;

    private final LineMapCollisionDetector detector;
    private final Point2D localPlayerPosition;
    private final int mapSize;
    private final int quality = 1;

    public SightView(Map map, Point2D localPlayerPosition, int mapSize) {
        this.detector = new LineMapCollisionDetector(map);
        this.localPlayerPosition = localPlayerPosition;
        this.mapSize = mapSize;
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888,
                mapSize * MapConfig.BOX_SIZE  * quality,
                mapSize * MapConfig.BOX_SIZE  * quality, false);
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        spriteBatch.end();
        fbo.bind();

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ZERO);
        shapeRenderer.rect(0,0, mapSize * MapConfig.BOX_SIZE * quality, mapSize * MapConfig.BOX_SIZE * quality);
        shapeRenderer.flush();

        //shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_ZERO, GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE_MINUS_DST_ALPHA);
        List<float[]> triangles = detector.getSightTriangles(localPlayerPosition, 128, 360);
        for (float[] t : triangles){
            shapeRenderer.triangle(
                    (t[0] + (float)MapConfig.WALL_THICKNESS / 2) * quality,
                    (t[1] + (float)MapConfig.WALL_THICKNESS / 2) * quality,
                    (t[2] + (float)MapConfig.WALL_THICKNESS / 2) * quality,
                    (t[3] + (float)MapConfig.WALL_THICKNESS / 2) * quality,
                    (t[4] + (float)MapConfig.WALL_THICKNESS / 2) * quality,
                    (t[5] + (float)MapConfig.WALL_THICKNESS / 2) * quality);
            //System.out.format("%f %f %f %f %f %f \n", t[0], t[1], t[2], t[3], t[4], t[5]);
        }

        shapeRenderer.flush();

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        fbo.end();

        Texture texture;
        texture = fbo.getColorBufferTexture();
        //texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);

        spriteBatch.begin();
        spriteBatch.draw(texture, 0, 0, texture.getWidth(), texture.getHeight(), 0, 0, quality, quality);
    }
}
