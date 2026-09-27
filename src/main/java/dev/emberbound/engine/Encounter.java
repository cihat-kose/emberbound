package dev.emberbound.engine;

import dev.emberbound.domain.Expedition;
import dev.emberbound.domain.Player;
import dev.emberbound.domain.Region;
import java.util.Objects;

/** A deterministic combat state machine. Only completed kills are committed to the expedition. */
public final class Encounter {
    public enum Action {
        ATTACK,
        BANDAGE,
        RETREAT
    }

    public enum Outcome {
        FIGHTING,
        ENEMY_DEFEATED,
        REGION_CLEARED,
        RETREATED,
        PLAYER_DIED,
        NO_BANDAGE_USED
    }

    public record Turn(Outcome outcome, int dealt, int taken, int healed, int gold) {}

    private final Expedition expedition;
    private final Region region;
    private int enemyHealth;
    private boolean finished;

    public Encounter(Expedition expedition, Region region) {
        this.expedition = Objects.requireNonNull(expedition);
        this.region = Objects.requireNonNull(region);
        expedition.requireActive();
        if (expedition.progress(region).cleared())
            throw new IllegalStateException("This region is already clear.");
        enemyHealth = region.enemy().health();
    }

    public Turn act(Action action) {
        Objects.requireNonNull(action);
        expedition.requireActive();
        if (finished) throw new IllegalStateException("This encounter has ended.");
        Player player = expedition.player();
        if (action == Action.RETREAT) {
            finished = true;
            return new Turn(Outcome.RETREATED, 0, 0, 0, 0);
        }
        int dealt = 0;
        int healed = 0;
        if (action == Action.BANDAGE) {
            healed = player.useBandage();
            if (healed == 0) return new Turn(Outcome.NO_BANDAGE_USED, 0, 0, 0, 0);
        } else {
            dealt = Math.min(enemyHealth, player.damage());
            enemyHealth -= dealt;
            if (enemyHealth == 0) {
                int credited = expedition.defeatEnemy(region);
                boolean cleared = expedition.progress(region).cleared();
                finished = cleared;
                if (!cleared) enemyHealth = region.enemy().health();
                return new Turn(
                        cleared ? Outcome.REGION_CLEARED : Outcome.ENEMY_DEFEATED,
                        dealt,
                        0,
                        0,
                        credited);
            }
        }
        int taken = player.takeDamage(region.enemy().damage());
        finished = !player.isAlive();
        return new Turn(finished ? Outcome.PLAYER_DIED : Outcome.FIGHTING, dealt, taken, healed, 0);
    }

    public Region region() {
        return region;
    }

    public int enemyHealth() {
        return enemyHealth;
    }

    public boolean finished() {
        return finished;
    }
}
