package cz.neumimto.rpg.events;

import cz.neumimto.rpg.IEntity;
import cz.neumimto.rpg.scripting.JsBinding;
import org.spongepowered.api.entity.projectile.Projectile;

/**
 * Created by NeumimTo on 17.6.2017.
 */
@JsBinding(JsBinding.Type.CLASS)
public class ProjectileHitEvent extends CancellableEvent {

	private final Projectile projectile;
	private IEntity shooter;
	private IEntity target;
	private double projectileDamage;

	public ProjectileHitEvent(IEntity shooter, IEntity target, double projectileDamage, Projectile projectile) {

		this.shooter = shooter;
		this.target = target;
		this.projectileDamage = projectileDamage;
		this.projectile = projectile;
	}

	public IEntity getShooter() {
		return shooter;
	}

	public void setShooter(IEntity shooter) {
		this.shooter = shooter;
	}

	public IEntity getTarget() {
		return target;
	}

	public void setTarget(IEntity target) {
		this.target = target;
	}

	public double getProjectileDamage() {
		return projectileDamage;
	}

	public void setProjectileDamage(double projectileDamage) {
		this.projectileDamage = projectileDamage;
	}

	public Projectile getProjectile() {
		return projectile;
	}
}
