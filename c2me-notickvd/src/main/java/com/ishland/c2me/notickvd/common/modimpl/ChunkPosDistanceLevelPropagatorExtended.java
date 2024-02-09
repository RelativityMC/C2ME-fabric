package com.ishland.c2me.notickvd.common.modimpl;

import net.minecraft.util.math.ChunkPos;

public abstract class ChunkPosDistanceLevelPropagatorExtended extends LevelPropagatorExtended {
	protected ChunkPosDistanceLevelPropagatorExtended(int i, int j, int k) {
		super(i, j, k);
	}

	@Override
	protected boolean isMarker(long id) {
		return id == ChunkPos.MARKER;
	}

	@Override
	protected void propagateLevel(long id, int level, boolean decrease) {
		if (!decrease || level < this.levelCount - 2) {
			ChunkPos chunkPos = new ChunkPos(id);
			int i = chunkPos.x;
			int j = chunkPos.z;

			for(int k = -1; k <= 1; ++k) {
				for(int l = -1; l <= 1; ++l) {
					long m = ChunkPos.toLong(i + k, j + l);
					if (m != id) {
						this.propagateLevel(id, m, level, decrease);
					}
				}
			}
		}
	}

	@Override
	protected int recalculateLevel(long id, long excludedId, int maxLevel) {
		int i = maxLevel;
		ChunkPos chunkPos = new ChunkPos(id);
		int j = chunkPos.x;
		int k = chunkPos.z;

		for(int l = -1; l <= 1; ++l) {
			for(int m = -1; m <= 1; ++m) {
				long n = ChunkPos.toLong(j + l, k + m);
				if (n == id) {
					n = ChunkPos.MARKER;
				}

				if (n != excludedId) {
					int o = this.getPropagatedLevel(n, id, this.getLevel(n));
					if (i > o) {
						i = o;
					}

					if (i == 0) {
						return i;
					}
				}
			}
		}

		return i;
	}

	@Override
	protected int getPropagatedLevel(long sourceId, long targetId, int level) {
		return sourceId == ChunkPos.MARKER ? this.getInitialLevel(targetId) : level + 1;
	}

	protected abstract int getInitialLevel(long id);

	public void updateLevel(long chunkPos, int distance, boolean decrease) {
		this.updateLevel(ChunkPos.MARKER, chunkPos, distance, decrease);
	}
}
