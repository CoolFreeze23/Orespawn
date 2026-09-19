package danger.orespawn.entity.client.animation;

/**
 * How the species' synched attacking flag reads on the client, as the SPEC's trigger inventory classifies it
 * (contract sections 4.3 and 4.4; /): a STATE flag is held while a target is engaged and drives {@code
 * w_aggro} (the {@code aggro_idle} / {@code calm_idle} weights); an EVENT flag pulses at the strike and its rising
 * edge fires {@code attack} (transport 2). {@link #NONE} - the default on every replacement - reads no
 * flag: {@code w_aggro} stays 0 and {@code attack} waits for the packet transport ({@code
 * AnimatableManager.tryTriggerAnimation("triggers", "attack")} from GeckoLib's own trigger path).
 */
public enum AttackingFlag {
    NONE,
    STATE,
    EVENT
}
