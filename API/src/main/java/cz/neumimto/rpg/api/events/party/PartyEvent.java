package cz.neumimto.rpg.api.events.party;

import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.party.IParty;
import cz.neumimto.rpg.api.events.Cancellable;

public interface PartyEvent extends Cancellable {

    IActiveCharacter getCharacter();

    IParty getParty();

    void setCharacter(IActiveCharacter character);

    void setParty(IParty party);

}
