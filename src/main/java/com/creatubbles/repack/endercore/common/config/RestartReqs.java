package com.creatubbles.repack.endercore.common.config;

import net.minecraftforge.common.config.Property;

public enum RestartReqs {
	/**
	 * No restart needed for this config to be applied. Default value.
	 */
	NONE,

	/**
	 * This config requires the world to be restarted to take effect.
	 */
	REQUIRES_WORLD_RESTART,

	/**
	 * This config requires the game to be restarted to take effect. {@code REQUIRES_WORLD_RESTART} is implied when using this.
	 */
	REQUIRES_MC_RESTART;

	public Property apply(Property prop) {
		if (this == REQUIRES_MC_RESTART) {
			prop.setRequiresMcRestart(true);
		} else if (this == REQUIRES_WORLD_RESTART) {
			prop.setRequiresWorldRestart(true);
		}
		return prop;
	}
}