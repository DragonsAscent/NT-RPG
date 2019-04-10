package cz.neumimto.rpg.api.items;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by NeumimTo on 29.4.2018.
 */
public class WeaponClass {

	public static final WeaponClass ARMOR = new WeaponClass("Armor");

	public static final WeaponClass SHIELD = new WeaponClass("Shield");

	static {
		SHIELD.parent = ARMOR;
	}

	private final String name;

	private Set<WeaponClass> subClass = new HashSet<>();

	private Set<RpgItemType> items = new HashSet<>();

	private WeaponClass parent;

	private Set<Integer> properties = new HashSet<>();

	private Set<Integer> propertiesMults = new HashSet<>();

	public WeaponClass(String name) {
		this.name = name;
	}

	public Set<WeaponClass> getSubClass() {
		return subClass;
	}

	public void setSubClass(Set<WeaponClass> subClass) {
		this.subClass = subClass;
	}

	public Set<RpgItemType> getItems() {
		return items;
	}

	public void setItems(Set<RpgItemType> items) {
		this.items = items;
	}

	public Set<Integer> getProperties() {
		return properties;
	}

	public void setProperties(Set<Integer> properties) {
		this.properties = properties;
	}

	public WeaponClass getParent() {
		return parent;
	}

	public void setParent(WeaponClass parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public Set<Integer> getPropertiesMults() {
		return propertiesMults;
	}

	@Override
	public String toString() {
		return "WeaponClass{" +
				"name='" + name + '\'' +
				'}';
	}
}
