# Field guide

You are stranded on an island. Clear the Cave, Forest and River to collect Food, Firewood and Water, then return to the Safe House. Gathering the final supply does not end the game until you return home.

## Choose a survivor

| Character | Attack | Maximum HP | Starting gold |
| --- | ---: | ---: | ---: |
| Samurai | 5 | 21 | 15 |
| Archer | 7 | 18 | 20 |
| Knight | 8 | 24 | 5 |

Every survivor starts with two bandages, fists and no armor. A bandage restores up to 10 HP. The original prototype's character and equipment statistics are preserved.

## Explore and fight

| Region | Enemy | Enemy HP | Attack | Gold per defeat | Supply |
| --- | --- | ---: | ---: | ---: | --- |
| Cave | Zombie | 10 | 3 | 4 | Food |
| Forest | Vampire | 14 | 4 | 7 | Firewood |
| River | Bear | 20 | 7 | 12 | Water |

Each region contains 1–3 enemies, determined at the beginning of an expedition. `--seed 7` recreates the same population. Loading a save preserves every region's original population and remaining enemies.

1. **Attack:** you strike first. If the enemy survives, it immediately counterattacks. Damage taken is `max(0, enemy attack - armor block)`, capped at your remaining health.
2. **Bandage:** heal up to 10 HP, then take the enemy's counterattack. Using a bandage at full health or with an empty pouch spends neither a bandage nor a turn.
3. **Retreat (0):** leave safely without a counterattack. Kills, gold and supplies persist. An injured but undefeated enemy returns to full health on your next visit.

A new enemy never attacks before your next choice. Clear every enemy in a region to collect its supply. You cannot fight in a cleared region or claim its rewards again.

At zero HP the expedition ends. Rest and bandages cannot revive a defeated player. Your previous save remains available from the main menu.

## Gear and healing

| Weapon | Additional attack | Price |
| --- | ---: | ---: |
| Pistol | 2 | 25 |
| Sword | 3 | 35 |
| Rifle | 7 | 45 |

| Armor | Damage blocked per hit | Price |
| --- | ---: | ---: |
| Light | 1 | 15 |
| Medium | 3 | 25 |
| Heavy | 5 | 40 |

Equipment is immediately equipped and replaces the previous item; there is no resale or trade-in. Duplicate purchases and downgrades are rejected without charging gold. A bandage costs 6 gold, with a maximum of nine in your pouch.

The Safe House restores all HP for free. There is no time limit or penalty for returning to rest.

## A reliable first expedition

Start with the Cave. Defeat one enemy, retreat before engaging the next, and rest. Continue through the Forest and River. If you choose Samurai, spend the starting 15 gold on Light armor before fighting. Archer and Knight can survive one enemy at a time without an equipment purchase when they begin each fight at full health.

This conservative route is checked for all three characters across 100 seeds each. Staying for several fights without resting is riskier; the displayed incoming damage helps you decide when to leave.

## Controls and saves

| Screen | Choices |
| --- | --- |
| Main menu | 1 New expedition · 2 Continue · 3 Help · 0 Quit |
| Island map | 1 Safe House · 2 Shop · 3 Cave · 4 Forest · 5 River · 6 Journal · 7 Save · 0 Save and quit |
| Combat | 1 Attack · 2 Bandage · 0 Retreat |
| Victory | 1 Save completed expedition · 0 Finish |

Type a number and press Enter. Invalid or out-of-range entries are retried. Names accept 1–24 Unicode code points, excluding terminal control characters.

There is **no autosave**. Save manually from the map or use Save and quit. A successful save replaces the selected slot. Use `--save path/to/slot.properties` for separate slots. Saves include character, health, gold, gear, bandages, seed, regional progress and escape status.

The game does not exit if saving fails; you can keep playing or try again. Closing input exits without writing a save (Ctrl+D on Unix-like terminals; Ctrl+Z then Enter on Windows). Closing the terminal also loses progress since the last save.

The `--demo` flag watches a fixed-seed expedition using the same game code. It ignores interactive input, does not use save files, and always uses seed 7.
