package dev.emberbound.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class PlayerTest {
    @ParameterizedTest
    @EnumSource(HeroClass.class)
    void startsWithTheSelectedBuildAndSurvivalSupplies(HeroClass hero) {
        Player player = new Player("  Cihat Köse  ", hero);
        assertAll(
                () -> assertEquals("Cihat Köse", player.name()),
                () -> assertEquals(hero.health(), player.health()),
                () -> assertEquals(hero.damage(), player.damage()),
                () -> assertEquals(hero.gold(), player.gold()),
                () -> assertEquals(Weapon.FISTS, player.weapon()),
                () -> assertEquals(Armor.NONE, player.armor()),
                () -> assertEquals(2, player.bandages()));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                "   ",
                "abcdefghijklmnopqrstuvwxyz",
                "name\t",
                "\u001b[31mname",
                "name\n"
            })
    void rejectsBlankLongOrTerminalControlNames(String name) {
        assertThrows(IllegalArgumentException.class, () -> new Player(name, HeroClass.SAMURAI));
    }

    @ParameterizedTest
    @CsvSource({"-1, 5, 2", "25, 5, 2", "24, -1, 2", "24, 1000000, 2", "24, 5, -1", "24, 5, 10"})
    void rejectsInvalidRestoredStatistics(int health, int gold, int bandages) {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new Player(
                                "Ada",
                                HeroClass.KNIGHT,
                                health,
                                gold,
                                Weapon.FISTS,
                                Armor.NONE,
                                bandages));
    }

    @Test
    void armorAbsorbsDamageWithoutHealingThePlayer() {
        Player player = new Player("Ada", HeroClass.KNIGHT, 20, 0, Weapon.FISTS, Armor.HEAVY, 0);
        assertEquals(0, player.takeDamage(3));
        assertEquals(20, player.health());
        assertEquals(2, player.takeDamage(7));
        assertEquals(18, player.health());
        assertThrows(IllegalArgumentException.class, () -> player.takeDamage(-1));
    }

    @Test
    void lethalDamageStopsAtZeroAndCannotBeUndoneByRestOrShopping() {
        Player player = new Player("Ada", HeroClass.KNIGHT);
        assertEquals(24, player.takeDamage(100));
        assertFalse(player.isAlive());
        assertEquals(0, player.health());
        assertThrows(IllegalStateException.class, player::healFully);
        assertThrows(IllegalStateException.class, player::useBandage);
        assertThrows(IllegalStateException.class, () -> player.buy(Armor.LIGHT));
        assertThrows(IllegalStateException.class, () -> player.earnGold(4));
    }

    @Test
    void bandagesAreConsumedOnlyWhenTheyActuallyHeal() {
        Player player = new Player("Ada", HeroClass.KNIGHT);
        assertEquals(0, player.useBandage());
        assertEquals(2, player.bandages());
        player.takeDamage(3);
        assertEquals(3, player.useBandage());
        assertEquals(24, player.health());
        player.takeDamage(14);
        assertEquals(10, player.useBandage());
        assertEquals(20, player.health());
        assertEquals(0, player.useBandage());
        assertEquals(0, player.bandages());
        assertEquals(4, player.healFully());
    }

    @Test
    void weaponPurchaseIsAtomicAndCannotReplaceBetterEquipment() {
        Player player = fundedPlayer(100, 2);
        assertEquals(Player.Purchase.BOUGHT, player.buy(Weapon.SWORD));
        assertEquals(65, player.gold());
        assertEquals(11, player.damage());
        assertEquals(Player.Purchase.NOT_AN_UPGRADE, player.buy(Weapon.SWORD));
        assertEquals(Player.Purchase.NOT_AN_UPGRADE, player.buy(Weapon.PISTOL));
        assertEquals(65, player.gold());
        assertEquals(Weapon.SWORD, player.weapon());
        assertEquals(Player.Purchase.BOUGHT, player.buy(Weapon.RIFLE));
        assertEquals(20, player.gold());
        assertEquals(15, player.damage());
    }

    @Test
    void armorPurchaseIsAtomicAndCannotDowngrade() {
        Player player = fundedPlayer(40, 2);
        assertEquals(Player.Purchase.BOUGHT, player.buy(Armor.HEAVY));
        assertEquals(0, player.gold());
        assertEquals(Player.Purchase.NOT_AN_UPGRADE, player.buy(Armor.LIGHT));
        assertEquals(Armor.HEAVY, player.armor());
        assertEquals(0, player.gold());
    }

    @Test
    void insufficientFundsNeverChangeInventoryOrBalance() {
        Player player = new Player("Ada", HeroClass.KNIGHT);
        assertEquals(Player.Purchase.NOT_ENOUGH_GOLD, player.buy(Weapon.PISTOL));
        assertEquals(Player.Purchase.NOT_ENOUGH_GOLD, player.buy(Armor.LIGHT));
        assertEquals(Player.Purchase.NOT_ENOUGH_GOLD, player.buyBandage());
        assertEquals(5, player.gold());
        assertEquals(Weapon.FISTS, player.weapon());
        assertEquals(Armor.NONE, player.armor());
        assertEquals(2, player.bandages());
    }

    @Test
    void bandageCapacityCannotChargeForAnItemThatDoesNotFit() {
        Player player = fundedPlayer(20, 8);
        assertEquals(Player.Purchase.BOUGHT, player.buyBandage());
        assertEquals(9, player.bandages());
        assertEquals(14, player.gold());
        assertEquals(Player.Purchase.BAG_FULL, player.buyBandage());
        assertEquals(14, player.gold());
    }

    @Test
    void rewardsRejectNegativeValuesAndOverflow() {
        Player player = new Player("Ada", HeroClass.KNIGHT);
        player.earnGold(4);
        assertEquals(9, player.gold());
        assertThrows(IllegalArgumentException.class, () -> player.earnGold(-1));
        assertThrows(IllegalArgumentException.class, () -> player.earnGold(Integer.MAX_VALUE));
        assertEquals(9, player.gold());
    }

    private Player fundedPlayer(int gold, int bandages) {
        return new Player("Ada", HeroClass.KNIGHT, 24, gold, Weapon.FISTS, Armor.NONE, bandages);
    }
}
