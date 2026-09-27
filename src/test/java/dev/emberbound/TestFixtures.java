package dev.emberbound;

import dev.emberbound.domain.Expedition;
import dev.emberbound.domain.HeroClass;
import dev.emberbound.domain.Player;
import dev.emberbound.domain.Region;
import java.util.EnumMap;

public final class TestFixtures {
    private TestFixtures() {}

    public static Expedition expedition(HeroClass hero, int enemiesPerRegion) {
        return expedition(new Player("Cihat", hero), enemiesPerRegion);
    }

    public static Expedition expedition(Player player, int enemiesPerRegion) {
        var progress = new EnumMap<Region, Expedition.Progress>(Region.class);
        for (Region region : Region.values()) {
            progress.put(region, new Expedition.Progress(enemiesPerRegion, enemiesPerRegion));
        }
        return new Expedition(player, 42L, progress, false);
    }
}
