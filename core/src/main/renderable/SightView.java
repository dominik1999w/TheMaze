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
    private final int quality = 5;

    public SightView(Map map, Point2D localPlayerPosition, int mapSize) {
        this.detector = new LineMapCollisionDetector(map);
        this.localPlayerPosition = localPlayerPosition;
        this.mapSize = mapSize;
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888,
                Gdx.graphics.getWidth()  * quality,
                Gdx.graphics.getHeight()  * quality, false);
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        spriteBatch.end();
        fbo.bind();

        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ZERO);
        shapeRenderer.rect(0,0, Gdx.graphics.getWidth() * quality, Gdx.graphics.getHeight() * quality);
        shapeRenderer.flush();

        //shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_ZERO, GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE_MINUS_DST_ALPHA);

        float shiftX = - (float)Gdx.graphics.getWidth() / quality / 2 + localPlayerPosition.x();
        float shiftY = - (float)Gdx.graphics.getHeight() / quality / 2 + localPlayerPosition.y();

        shapeRenderer.rect(0, 0,Gdx.graphics.getWidth()* quality, -shiftY * quality);
        shapeRenderer.rect(0, 0,-shiftX * quality, Gdx.graphics.getHeight() * quality);
        shapeRenderer.rect(0, (mapSize * MapConfig.BOX_SIZE - shiftY - 1.5f + (float)MapConfig.WALL_THICKNESS / 2) * quality,Gdx.graphics.getWidth() * quality,Gdx.graphics.getHeight() * quality);
        shapeRenderer.rect((mapSize * MapConfig.BOX_SIZE - shiftX - 1.5f + (float)MapConfig.WALL_THICKNESS / 2) * quality, 0,Gdx.graphics.getWidth() * quality,Gdx.graphics.getHeight() * quality);


        List<float[]> triangles = detector.getSightTriangles(localPlayerPosition, 128, 360);
        for (float[] t : triangles){
            shapeRenderer.triangle(
                    (t[0] - shiftX) * quality,
                    (t[1] - shiftY) * quality,
                    (t[2] - shiftX) * quality,
                    (t[3] - shiftY) * quality,
                    (t[4] - shiftX) * quality,
                    (t[5] - shiftY) * quality);
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
        //draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY,
        //          float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY)
        spriteBatch.draw(texture, shiftX + (float)MapConfig.WALL_THICKNESS / 2, shiftY + (float)MapConfig.WALL_THICKNESS / 2, texture.getWidth(), texture.getHeight(), 0, 0, quality, quality);
    }
}
