package types;

import com.badlogic.gdx.graphics.Texture;

public enum TextureType {
    Ground {
        @Override
        public Texture createTexture() {
            return new Texture("blacktile.png");
        }
    },
    Wall {
        @Override
        public Texture createTexture() {
            return new Texture("graytile.png");
        }
    };

    public abstract Texture createTexture();
}
