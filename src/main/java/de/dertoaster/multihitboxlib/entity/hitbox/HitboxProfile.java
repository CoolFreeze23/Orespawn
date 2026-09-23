package de.dertoaster.multihitboxlib.entity.hitbox;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.dertoaster.multihitboxlib.api.IMultipartEntity;

/**
 * ENT-S-174 (vendored change): {@code bone-sync-interval} is the fewest ticks between two bone packets of the master
 * client (IMultipartEntity#updateSynching, util.BoneSyncGate). 1, the default, is the library's every-tick stream; the
 * generated species profiles (tools/hitbox_specs) declare 2, the four bosses keep 1 (the two robots stream no bones). The
 * codec admits 1..{@link IMultipartEntity#BONE_INFORMATION_KEEPALIVE_TICKS}, the keepalive's period; a present value
 * outside it is an error that fails the world's datapack load, as any bad field of a synced registry does (the send gate
 * clamps to the same range besides). An interval of 7 or 8 would put every packet at the edge of the server's
 * 10-tick master timeout, where only the keepalive sits today.
 */
public record HitboxProfile(
		AssetEnforcementConfig assetConfig,
		boolean syncToModel,
		boolean trustClient,
		int partUpdateSteps,
		int synchedPartUpdateSteps,
		int boneSyncInterval,
		List<String> synchedBones,
		MainHitboxConfig mainHitboxConfig,
		List<SubPartConfig> partConfigs
		) {

	public static final Codec<HitboxProfile> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				AssetEnforcementConfig.CODEC.fieldOf("synched-assets").forGetter(HitboxProfile::assetConfig),
				Codec.BOOL.fieldOf("sync-with-model").forGetter(HitboxProfile::syncToModel),
				Codec.BOOL.optionalFieldOf("trust-client", false).forGetter(HitboxProfile::trustClient),
				Codec.INT.optionalFieldOf("part-update-steps", 3).forGetter(HitboxProfile::partUpdateSteps),
				Codec.INT.optionalFieldOf("synched-part-update-steps", 1).forGetter(HitboxProfile::synchedPartUpdateSteps),
				Codec.intRange(1, IMultipartEntity.BONE_INFORMATION_KEEPALIVE_TICKS).optionalFieldOf("bone-sync-interval", 1).forGetter(HitboxProfile::boneSyncInterval),
				Codec.STRING.listOf().fieldOf("synched-bones").forGetter(HitboxProfile::synchedBones),
				MainHitboxConfig.CODEC.fieldOf("main-hitbox").forGetter(HitboxProfile::mainHitboxConfig),
				SubPartConfig.CODEC.listOf().fieldOf("parts").forGetter(HitboxProfile::partConfigs)

			).apply(instance, HitboxProfile::new);
	});

}
