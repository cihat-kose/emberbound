package dev.emberbound.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/** Region populations are rolled once so retreating or reloading never rerolls rewards. */
public final class Expedition {
    public record Progress(int total, int remaining) {
        public Progress {
            if (total < 1 || total > 3 || remaining < 0 || remaining > total) {
                throw new IllegalArgumentException("Invalid region progress.");
            }
        }

        public boolean cleared() {
            return remaining == 0;
        }
    }

    private final Player player;
    private final long seed;
    private final EnumMap<Region, Progress> progress = new EnumMap<>(Region.class);
    private boolean escaped;

    public static Expedition start(String name, HeroClass hero, long seed) {
        Random random = new Random(seed);
        Map<Region, Progress> regions = new EnumMap<>(Region.class);
        for (Region region : Region.values()) {
            int count = random.nextInt(3) + 1;
            regions.put(region, new Progress(count, count));
        }
        return new Expedition(new Player(name, hero), seed, regions, false);
    }

    public Expedition(Player player, long seed, Map<Region, Progress> progress, boolean escaped) {
        this.player = Objects.requireNonNull(player);
        this.seed = seed;
        for (Region region : Region.values()) {
            this.progress.put(
                    region,
                    Objects.requireNonNull(progress.get(region), "Missing region progress."));
        }
        if (escaped && (!hasAllSupplies() || !player.isAlive())) {
            throw new IllegalArgumentException(
                    "Only a living survivor with all supplies can escape.");
        }
        this.escaped = escaped;
    }

    public int defeatEnemy(Region region) {
        requireActive();
        Progress current = progress(region);
        if (current.cleared())
            throw new IllegalStateException("This region has already been cleared.");
        int credited = player.earnGold(region.enemy().gold());
        progress.put(region, new Progress(current.total(), current.remaining() - 1));
        return credited;
    }

    public int rest() {
        requireActive();
        int healed = player.healFully();
        escaped = hasAllSupplies();
        return healed;
    }

    public void requireActive() {
        if (!player.isAlive() || escaped)
            throw new IllegalStateException("This expedition has ended.");
    }

    public Player player() {
        return player;
    }

    public long seed() {
        return seed;
    }

    public Progress progress(Region region) {
        return progress.get(Objects.requireNonNull(region));
    }

    public Map<Region, Progress> progress() {
        return Map.copyOf(progress);
    }

    public boolean escaped() {
        return escaped;
    }

    public boolean hasAllSupplies() {
        return progress.values().stream().allMatch(Progress::cleared);
    }

    public long suppliesCollected() {
        return progress.values().stream().filter(Progress::cleared).count();
    }

    public int enemiesDefeated() {
        return progress.values().stream()
                .mapToInt(value -> value.total() - value.remaining())
                .sum();
    }
}
