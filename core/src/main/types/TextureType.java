package types;

public enum TextureType {
    GROUND("graytile.png"),
    WALL("blacktile.png"),
    PLAYER("player.png"),
    BULLET("bullet.png");

    private final String name;

    TextureType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
