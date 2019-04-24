/*
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.neumimto.rpg.utils;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import cz.neumimto.rpg.GlobalScope;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.api.utils.Console;
import cz.neumimto.rpg.common.entity.PropertyServiceImpl;
import cz.neumimto.rpg.entities.IEntity;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.NDamageType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.entity.EyeLocationProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.EntityUniverse;
import org.spongepowered.common.event.damage.SpongeEntityDamageSourceBuilder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.neumimto.rpg.api.logging.Log.info;

/**
 * Created by NeumimTo on 25.7.2015.
 */
public class Utils {

	public static String LineSeparator = System.getProperty("line.separator");
	public static String Tab = "\t";
	public static Set<BlockType> transparentBlocks = new HashSet<>();
	public static Predicate<BlockRayHit<World>> SKILL_TARGET_BLOCK_FILTER =
			(Predicate<BlockRayHit<World>>)
					a -> !isTransparent(a.getExtent()
							.getBlockType(a.getBlockX(), a.getBlockY(), a.getBlockZ()));
	public static Pattern REGEXP_NUMBER = Pattern.compile("[-+]?\\d+([\\.,]\\d+)?");
	public static Pattern REGEXP_CLASS_MEMBER = Pattern.compile("^[a-z_]\\w*$");
	private static GlobalScope globalScope = NtRpgPlugin.GlobalScope;

	static {
		transparentBlocks.addAll(Arrays.asList(BlockTypes.AIR,
				BlockTypes.GRASS, BlockTypes.TALLGRASS, BlockTypes.GRASS, BlockTypes.BED,
				BlockTypes.WHEAT, BlockTypes.FLOWER_POT, BlockTypes.FIRE, BlockTypes.WATER, BlockTypes.LAVA, BlockTypes.FLOWING_WATER));
	}

	public static void applyOnNearbyPartyMembers(IActiveCharacter character, int distance, Consumer<IActiveCharacter> c) {
		double k = Math.pow(distance, 2);
		Player pl = character.getPlayer();
		for (IActiveCharacter iActiveCharacter : character.getParty().getPlayers()) {
			if (iActiveCharacter.getPlayer().getLocation().getPosition()
					.distanceSquared(pl.getLocation().getPosition()) <= k) {
				c.accept(iActiveCharacter);
			}
		}
	}

	public static void applyOnNearby(IActiveCharacter character, int distance, Consumer<Entity> e) {
		Player pl = character.getPlayer();
		pl.getWorld()
				.getIntersectingEntities(pl, distance, hit -> hit.getEntity() != pl)
				.stream().map(EntityUniverse.EntityHit::getEntity)
				.filter(Utils::isLivingEntity)
				.forEach(e);
	}

	public static void applyOnNearbyAndSelf(IActiveCharacter character, int distance, Consumer<Entity> e) {
		Player pl = character.getPlayer();
		pl.getWorld()
				.getIntersectingEntities(pl, distance)
				.stream().map(EntityUniverse.EntityHit::getEntity)
				.filter(Utils::isLivingEntity)
				.forEach(e);
	}

	public static double getPercentage(double n, double total) {
		return (n / total) * 100;
	}

	public static boolean isMoreThanPercentage(double a, double b, double percentage) {
		return ((a / b) * 100 - 100) >= percentage;
	}

	public static double round(double value, int precision) {
		int scale = (int) Math.pow(10, precision);
		return (double) Math.round(value * scale) / scale;
	}

	public static double round(float value, int precision) {
		int scale = (int) Math.pow(10, precision);
		return Math.round(value * scale) / scale;
	}

	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}

	public static Set<Entity> getNearbyEntities(Location l, int radius) {
		int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
		HashSet<Entity> set = new HashSet<>();
		double pow = Math.pow(radius, 2);
		for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
			for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
				Location chunkLoc = new Location(l.getExtent(), l.getBlockX() + (chX * 16), l.getBlockY(), l.getBlockZ() + (chZ * 16));
				for (Entity e : chunkLoc.getExtent().getEntities()) {
					if (e.getLocation().getPosition().distanceSquared(l.getPosition()) <= pow) {
						set.add(e);
					}
				}
			}
		}
		return set;
	}

	public static Set<Entity> getNearbyEntitiesPrecise(Location l, int radius) {
		int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
		HashSet<Entity> set = new HashSet<>();
		for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
			for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
				Location chunkLoc = new Location(l.getExtent(), l.getBlockX() + (chX * 16), l.getBlockY(), l.getBlockZ() + (chZ * 16));
				for (Entity e : chunkLoc.getExtent().getEntities()) {
					if (e.getLocation().getPosition().distance(l.getPosition()) <= radius) {
						set.add(e);
					}
				}
			}
		}
		return set;
	}

	public static Optional<Entity> spawnProjectile(IEntity caster, EntityType type) {
		return Optional.empty(); //todo
	}

	public static boolean isTransparent(BlockType e) {
		return transparentBlocks.contains(e);
	}

	public static Living getTargetedEntity(IActiveCharacter character, int range) {
		Player player = character.getPlayer();

		Vector3d r = player.getRotation();
		Vector3d dir = Quaterniond.fromAxesAnglesDeg(r.getX(), -r.getY(), r.getZ()).getDirection();
		Vector3d vec3d = player.getProperty(EyeLocationProperty.class).get().getValue();
		Optional<EntityUniverse.EntityHit> e = player
				.getWorld()
				.getIntersectingEntities(vec3d, dir, range,
						entityHit -> entityHit.getEntity() != character.getEntity() && isLivingEntity(entityHit.getEntity()))
				.stream().reduce((a, b) -> a.getDistance() < b.getDistance() ? a : b);

		if (e.isPresent()) {
			Optional<BlockRayHit<World>> end = BlockRay.from(player)
					.distanceLimit(range)
					.stopFilter(SKILL_TARGET_BLOCK_FILTER)
					.build()
					.end();
			if (!end.isPresent()) {
				return (Living) e.get().getEntity();
			} else {
				Entity entity = e.get().getEntity();
				Location<World> location = entity.getLocation();
				if (end.get().getBlockPosition()
						.distanceSquared(location.getBlockX(), location.getBlockZ(), location.getBlockZ()) <= 2) {
					return (Living) e.get().getEntity();
				}
			}
		}
		return null;
	}

	public static void hideProjectile(Projectile projectile) {
		projectile.offer(Keys.INVISIBLE, true);
	}

	public static String newLine(String s) {
		return Tab + s + LineSeparator;
	}

	/**
	 * Resets stats of vanilla player object back to default state, Resets max hp, speed
	 *
	 * @param player
	 */
	public static void resetPlayerToDefault(Player player) {
		player.offer(Keys.WALKING_SPEED, PropertyServiceImpl.WALKING_SPEED);
	}

	/**
	 * Inline negation of method references
	 */
	public static <T> Predicate<T> not(Predicate<T> t) {
		return t.negate();
	}

	//todo
	public static boolean canDamage(IActiveCharacter character, Living l) {
		if (character.getPlayer() == l) {
			return false;
		}
		if (l.getType() == EntityTypes.PLAYER) {
			if (character.hasParty()) {
				IActiveCharacter c = globalScope.characterService.getCharacter(l.getUniqueId());
				if (character.getParty().getPlayers().contains(c)) {
					return false;
				}
			}
		}
		DamageSource build = new SpongeEntityDamageSourceBuilder()
				.entity(character.getEntity())
				.type(NDamageType.DAMAGE_CHECK)
				.bypassesArmor()
				.absolute()
				.build();

		return l.damage(0, build);
	}

	public static boolean canDamage(IEntity entity, Living l) {
		DamageSource build = new SpongeEntityDamageSourceBuilder()
				.entity(entity.getEntity())
				.type(NDamageType.DAMAGE_CHECK)
				.bypassesArmor()
				.absolute()
				.build();

		return l.damage(0, build);
	}

	public static boolean isLivingEntity(Entity entity) {
		if (entity.isRemoved()) {
			return false;
		}
		Optional<Double> aDouble = entity.get(Keys.HEALTH);
		if (aDouble.isPresent()) {
			return aDouble.get() > 0;
		}
		return false;
	}

	public static void broadcastMessage(Text message, Player source, int radius) {
		double s = Math.pow(radius, 2);
		Collection<Player> onlinePlayers = Sponge.getServer().getOnlinePlayers();
		for (Player onlinePlayer : onlinePlayers) {
			if (onlinePlayer.getLocation().getPosition().distanceSquared(source.getLocation().getPosition()) <= s) {
				onlinePlayer.sendMessage(message);
			}
		}
	}

	public static String capitalizeFirst(String str) {
		return (Character.toUpperCase(str.charAt(0)) + str.substring(1)).replaceAll("_", " ");
	}

	public static String extractNumber(String string) {
		Matcher matcher = REGEXP_NUMBER.matcher(string);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static String extractClassMember(String string) {
		Matcher matcher = REGEXP_CLASS_MEMBER.matcher(string);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static String configNodeToReadableString(String t) {
		String a = t.replaceAll("_", " ");
		a = a.substring(0, 1).toUpperCase() + a.substring(1);
		return a;
	}

	public static void executeCommandBatch(Map<String, String> variables, List<String> commandTemplates) {
		for (String commandTemplate : commandTemplates) {
			for (Map.Entry<String, String> entry : variables.entrySet()) {
				commandTemplate = commandTemplate.replaceAll("\\{\\{" + entry.getKey() + "}}", entry.getValue());
			}
			try {
				info(Console.GREEN_BOLD + " Running Command (as a console): " + Console.YELLOW + commandTemplate);
				Sponge.getCommandManager().process(Sponge.getServer().getConsole(), commandTemplate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Location getLocationRelative(String location, Location original) {
		String[] split = location.split(";");
		String s = split[0];
		double x = original.getBlockX();
		x = getSinglePoint(s, x);

		s = split[1];
		double y = original.getBlockY();
		y = getSinglePoint(s, x);

		double z = original.getBlockZ();
		z = getSinglePoint(s, x);


		return new Location(original.getExtent(), x, y, z);
	}

	private static double getSinglePoint(String s, double n) {
		if (s.startsWith("~")) {
			return n + Double.parseDouble(extractNumber(s));
		} else {
			return Double.parseDouble(extractNumber(s));
		}
	}
}
