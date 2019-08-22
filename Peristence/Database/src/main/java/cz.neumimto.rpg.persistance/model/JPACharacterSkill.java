package cz.neumimto.rpg.persistance.model;

import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.CharacterClass;
import cz.neumimto.rpg.api.persistance.model.CharacterSkill;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by ja on 8.10.2016.
 */
@Entity(name = "rpg_character_skill")
public class JPACharacterSkill extends JPATimestampEntity implements CharacterSkill {

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "skill_id")
    private Long skillId;

    @ManyToOne(targetEntity = JPACharacterBase.class)
    @JoinColumn(name = "character_id")
    private CharacterBase characterBase;

    private int level;

    @ManyToOne(targetEntity = JPACharacterClass.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private CharacterClass fromClass;

    @Column(name = "catalog_id")
    private String catalogId;

    @Column(name = "cooldown")
    private Long cooldown;

    @Override
    public Long getId() {
        return skillId;
    }

    @Override
    public void setId(Long id) {
        this.skillId = id;
    }

    @Override
    public CharacterBase getCharacterBase() {
        return characterBase;
    }

    @Override
    public void setCharacterBase(CharacterBase characterBase) {
        this.characterBase = characterBase;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public CharacterClass getFromClass() {
        return fromClass;
    }

    @Override
    public void setFromClass(CharacterClass fromClass) {
        this.fromClass = fromClass;
    }

    @Override
    public String getCatalogId() {
        return catalogId;
    }

    @Override
    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    @Override
    public Long getCooldown() {
        return cooldown;
    }

    @Override
    public void setCooldown(Long cooldown) {
        this.cooldown = cooldown;
    }
}