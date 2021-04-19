package types;

public enum SkinType {
    GLASSY("skins/glassy/glassy-ui.json"),
    ATTACK("skins/biological-attack/skin/biological-attack-ui.json");
    private final String name;

    SkinType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
