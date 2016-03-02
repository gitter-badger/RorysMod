/*
Copyright 2016 Rory Claasen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package net.roryclaasen.asm.rorysmodcore.transformer;

import net.minecraft.entity.player.EntityPlayer;
import net.roryclaasen.rorysmod.core.Settings;

public class StaticClass {

	private StaticClass() {}

	public static boolean shouldWakeUp() {
		return !Settings.enableStayInBed;
	}

	public static boolean shouldWakeUp(EntityPlayer player) {
		if (player == null) return false;
		if (!player.worldObj.isRemote) {
			if (shouldWakeUp()) {
				return true;
			}
		}
		return false;
	}
}
