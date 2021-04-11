package player;

public abstract class PlayerController {

    protected final Player player;

    protected PlayerController(Player player) {
        this.player = player;
    }

    public abstract void update(float delta);
}
