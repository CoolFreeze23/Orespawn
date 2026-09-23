package de.dertoaster.multihitboxlib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.dertoaster.multihitboxlib.api.IMHLibPartIndexMember;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * ENT-S-174: the parts of a level, grouped by the entity that owns them, for NeoForge's two box queries.
 *
 *
 * <p>NeoForge keeps every part entity of a level in one flat map ({@code ServerLevel.dragonParts},
 * {@code ClientLevel.partEntities}, keyed by part id, filled and emptied by the level's
 * {@code EntityCallbacks.onTrackingStart/End}), and both {@code Level.getEntities} box queries walk ALL of it after the
 * section lookup, testing each part's box against the query. With the ENT-S-173 profiles on every species that map
 * holds five or six parts per loaded OreSpawn creature, and every query pays for all of them: a mob's push check each
 * tick, a projectile's sweep, an explosion, a hopper, an AI scan. This index keeps one group per owning entity with an
 * envelope, the union of its parts' current boxes; {@code MixinLevel} hands the query the parts of only the groups whose
 * envelope meets the query box, and NeoForge's own loop then applies its own tests to them unchanged.</p>
 *
 * <p>Exactness. A part's box meeting the query box ({@code AABB.intersects}, strict on every axis) implies its group's
 * envelope does, so no part the flat walk would have returned is dropped; the flat walk's own tests (the excluded
 * entity, the type test, the predicate, the limit) still run on every part handed over. The envelope is never stale:
 * every write of an entity's box goes through {@code Entity.setBoundingBox} (the only writer of {@code Entity.bb}
 * besides its initializer), where {@code MixinEntity} marks the part's group moved, and a moved group's envelope is
 * rebuilt from its parts' boxes before the query reads it. A NaN coordinate fails every comparison and drops out of the
 * envelope; such a part fails the flat walk's strict test on that axis anyway. Membership is the flat map's own: the
 * callbacks mixins wrap the map's {@code put} and {@code remove} in those callbacks and report each part in or out as
 * the map changes (a {@code put} over an occupied id retires the part it replaced), and the one writer of the map
 * outside those callbacks, MHLib's own {@code MHLibClientPartRegistration} (the modern robots' client parts, built after
 * the entity joined the level), reports its puts the same way; so the index and the map agree at every instant, inside
 * {@code EntityLeaveLevelEvent} handlers included. Whenever the map's size and the index's part count still disagree
 * (another mod writing the map through an accessor), or the query runs on a thread other than the one that built the
 * level, the query gets the flat map itself, which is exactly the old behaviour. Only the ORDER of the parts handed
 * over differs from the map's hash order: a typed query whose limit several parts would satisfy (an entity selector
 * with {@code limit} and {@code sort=arbitrary}) may pick another of them, and a ray meeting two parts at exactly the same
 * distance may resolve the tie the other way.</p>
 *
 * <p>Cost. A query reads one envelope per multipart entity in the level from a packed array instead of one box per
 * part. A group is marked moved only when one of its parts' boxes actually changes (MixinEntity compares the new box
 * with the old), so a creature standing still keeps its envelope, and a moving one pays one rebuild, a read of its
 * parts' boxes, at the first query after it moved. A query allocates nothing when no envelope meets its box, one list
 * view when one does, and one list when several do. Parts were the multiplier: 666 parts across the 103 generated
 * profiles, five or six per creature and up to 26 (the adult Prince).</p>
 */
public final class PartEntityIndex {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final PartEntity<?>[] NO_PARTS = new PartEntity<?>[0];

	/**
	 * One owning entity's parts that are in the level's map, and its slot in the packed arrays. The parts array is
	 * copy-on-write, so a query's view of it survives a part joining or leaving during the query's own loop.
	 */
	public static final class Group {
		private final PartEntityIndex index;
		@Nullable
		private final Entity parent;
		private PartEntity<?>[] parts = NO_PARTS;
		private int slot;

		private Group(final PartEntityIndex index, @Nullable final Entity parent, final int slot) {
			this.index = index;
			this.parent = parent;
			this.slot = slot;
		}

		/** From {@code Entity.setBoundingBox} of any of this group's parts (MixinEntity): the envelope must be rebuilt. */
		public void markMoved() {
			final boolean[] dirty = this.index.dirty;
			final int s = this.slot;
			if (s >= 0 && s < dirty.length) {
				dirty[s] = true;
			}
		}

		@Nullable
		public Entity parent() {
			return this.parent;
		}

		public int size() {
			return this.parts.length;
		}
	}

	private final Reference2ObjectOpenHashMap<Entity, Group> byParent = new Reference2ObjectOpenHashMap<>();
	private Group[] groups = new Group[16];
	/** Six doubles per slot: minX, minY, minZ, maxX, maxY, maxZ of the group's parts' boxes. */
	private double[] envelopes = new double[16 * 6];
	private boolean[] dirty = new boolean[16];
	private int groupCount;
	private int partCount;
	/** The thread the level was built on: the server thread, or the client's main thread. */
	private final Thread owner = Thread.currentThread();
	private boolean divergenceLogged;
	private long flatWalks;
	private long queries;

	// ── membership: the level's part map, call by call ──

	/**
	 * After {@code map.put(part.getId(), part)} in {@code onTrackingStart} returned {@code previous}: the part is in the
	 * map, and a different part the id held before is not.
	 */
	public void onPut(final PartEntity<?> part, @Nullable final Object previous) {
		if (previous == part) {
			return;
		}
		if (previous instanceof PartEntity<?> replaced) {
			this.onRemoved(replaced);
		}
		final Entity parent = part.getParent();
		Group group = this.byParent.get(parent);
		if (group == null) {
			final int slot = this.groupCount;
			this.ensureCapacity(slot + 1);
			group = new Group(this, parent, slot);
			this.groups[slot] = group;
			this.groupCount = slot + 1;
			this.byParent.put(parent, group);
		}
		final PartEntity<?>[] old = group.parts;
		final PartEntity<?>[] grown = Arrays.copyOf(old, old.length + 1);
		grown[old.length] = part;
		group.parts = grown;
		this.partCount++;
		this.dirty[group.slot] = true;
		if (part instanceof IMHLibPartIndexMember member) {
			member._mhlibAccess_setPartIndexGroup(group);
		}
	}

	/** After {@code map.remove(id)} in {@code onTrackingEnd} returned {@code removed} (null when the id held nothing). */
	public void onRemoved(@Nullable final Object removed) {
		if (!(removed instanceof PartEntity<?> part)) {
			return;
		}
		final Group group = this.byParent.get(part.getParent());
		if (group == null) {
			return;
		}
		final PartEntity<?>[] old = group.parts;
		int at = -1;
		for (int i = 0; i < old.length; i++) {
			if (old[i] == part) {
				at = i;
				break;
			}
		}
		if (at < 0) {
			return;
		}
		if (part instanceof IMHLibPartIndexMember member && member._mhlibAccess_getPartIndexGroup() == group) {
			member._mhlibAccess_setPartIndexGroup(null);
		}
		this.partCount--;
		if (old.length == 1) {
			this.drop(group);
			return;
		}
		final PartEntity<?>[] shrunk = new PartEntity<?>[old.length - 1];
		System.arraycopy(old, 0, shrunk, 0, at);
		System.arraycopy(old, at + 1, shrunk, at, old.length - at - 1);
		group.parts = shrunk;
		this.dirty[group.slot] = true;
	}

	private void drop(final Group group) {
		this.byParent.remove(group.parent);
		final int slot = group.slot;
		final int last = this.groupCount - 1;
		if (slot != last) {
			final Group moved = this.groups[last];
			this.groups[slot] = moved;
			moved.slot = slot;
			System.arraycopy(this.envelopes, last * 6, this.envelopes, slot * 6, 6);
			this.dirty[slot] = this.dirty[last];
		}
		this.groups[last] = null;
		this.dirty[last] = false;
		this.groupCount = last;
		group.parts = NO_PARTS;
		group.slot = -1;
	}

	private void ensureCapacity(final int slots) {
		if (slots <= this.groups.length) {
			return;
		}
		final int size = Math.max(slots, this.groups.length * 2);
		this.groups = Arrays.copyOf(this.groups, size);
		this.envelopes = Arrays.copyOf(this.envelopes, size * 6);
		this.dirty = Arrays.copyOf(this.dirty, size);
	}

	// ── the query ──

	/**
	 * The parts {@code Level.getEntities} walks for {@code box}: the parts of every group whose envelope meets the box, or
	 * {@code flat} itself when the index cannot vouch for the answer (see the class comment).
	 */
	public Collection<PartEntity<?>> near(final Collection<PartEntity<?>> flat, final AABB box) {
		this.queries++;
		if (Thread.currentThread() != this.owner) {
			this.flatWalks++;
			return flat;
		}
		if (flat.size() != this.partCount) {
			this.flatWalks++;
			if (!this.divergenceLogged) {
				this.divergenceLogged = true;
				LOGGER.warn("MHLib part index: the level's part map holds {} parts and the index {} in {} groups (something wrote the map "
						+ "outside its tracking callbacks); entity queries in this level walk the whole part map while the counts differ",
						flat.size(), this.partCount, this.groupCount);
			}
			return flat;
		}
		final int count = this.groupCount;
		if (count == 0) {
			return flat;
		}
		final double bx0 = box.minX, by0 = box.minY, bz0 = box.minZ, bx1 = box.maxX, by1 = box.maxY, bz1 = box.maxZ;
		final double[] env = this.envelopes;
		final boolean[] moved = this.dirty;
		PartEntity<?>[] single = null;
		List<PartEntity<?>> many = null;
		for (int i = 0; i < count; i++) {
			if (moved[i]) {
				this.rebuild(i);
			}
			final int o = i * 6;
			if (env[o] < bx1 && env[o + 3] > bx0 && env[o + 1] < by1 && env[o + 4] > by0 && env[o + 2] < bz1 && env[o + 5] > bz0) {
				final PartEntity<?>[] parts = this.groups[i].parts;
				if (single == null && many == null) {
					single = parts;
				} else {
					if (many == null) {
						many = new ArrayList<>(single.length + parts.length);
						Collections.addAll(many, single);
					}
					Collections.addAll(many, parts);
				}
			}
		}
		if (many != null) {
			return many;
		}
		return single != null ? Arrays.asList(single) : Collections.emptyList();
	}

	/** The envelope of slot {@code i} from its parts' current boxes; NaN coordinates fail every comparison and drop out. */
	private void rebuild(final int i) {
		this.dirty[i] = false;
		double x0 = Double.POSITIVE_INFINITY, y0 = Double.POSITIVE_INFINITY, z0 = Double.POSITIVE_INFINITY;
		double x1 = Double.NEGATIVE_INFINITY, y1 = Double.NEGATIVE_INFINITY, z1 = Double.NEGATIVE_INFINITY;
		for (PartEntity<?> part : this.groups[i].parts) {
			final AABB b = part.getBoundingBox();
			if (b.minX < x0) x0 = b.minX;
			if (b.minY < y0) y0 = b.minY;
			if (b.minZ < z0) z0 = b.minZ;
			if (b.maxX > x1) x1 = b.maxX;
			if (b.maxY > y1) y1 = b.maxY;
			if (b.maxZ > z1) z1 = b.maxZ;
		}
		final int o = i * 6;
		final double[] env = this.envelopes;
		env[o] = x0;
		env[o + 1] = y0;
		env[o + 2] = z0;
		env[o + 3] = x1;
		env[o + 4] = y1;
		env[o + 5] = z1;
	}

	// ── read-outs for the suite ──

	public int groupCount() {
		return this.groupCount;
	}

	public int partCount() {
		return this.partCount;
	}

	/** Queries answered with the whole map (another thread, or the counts out of step) since the level was built. */
	public long flatWalks() {
		return this.flatWalks;
	}

	/** Box queries that reached the index (MixinLevel's two wraps) since the level was built, the fallbacks included. */
	public long queries() {
		return this.queries;
	}

	@Nullable
	public Group groupOf(final Entity parent) {
		return this.byParent.get(parent);
	}
}
