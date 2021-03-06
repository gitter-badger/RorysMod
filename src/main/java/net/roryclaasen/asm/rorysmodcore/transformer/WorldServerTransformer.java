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

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;
import net.roryclaasen.rorysmod.util.RMLog;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class WorldServerTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) {
		byte[] data = arg2;
		try {
			if (arg0.equals("mt")) {
				RMLog.info("[WorldServer] About to patch [mt]", true);
				data = patchWakeAllPlayers(arg0, data, true);
				data = patchTick(arg0, data, true);
			}
			if (arg0.equals("net.minecraft.world.WorldServer")) {
				RMLog.info("[WorldServer] About to patch [net.minecraft.world.WorldServer]", true);
				data = patchWakeAllPlayers(arg0, data, false);
				data = patchTick(arg0, data, false);
			}
		} catch (Exception e) {
			RMLog.warn("[WorldServer] Patch failed!", true);
			e.printStackTrace();
		}
		if (data != arg2) {
			RMLog.info("[WorldServer] Finnished Patching! and applied changes", true);
		} else {
			// RMLog.info("[WorldServer] No changes applied", true);
		}
		return data;
	}

	public byte[] patchTick(String name, byte[] bytes, boolean obfuscated) {
		RMLog.info("[WorldServer] [tick] Patching", true);
		String targetMethodName = "";

		if (obfuscated == true) targetMethodName = "b";
		else targetMethodName = "tick";
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		Iterator<MethodNode> methods = classNode.methods.iterator();
		while (methods.hasNext()) {
			MethodNode method = methods.next();
			int invok_index = -1;
			if ((method.name.equals(targetMethodName) && method.desc.equals("()V"))) {
				AbstractInsnNode currentNode = null;
				AbstractInsnNode targetNode = null;

				Iterator<AbstractInsnNode> iter = method.instructions.iterator();

				int index = -1;

				int INVOKEVIRTUAL_COUNT = 0;
				while (iter.hasNext()) {
					index++;
					currentNode = iter.next();
					if (currentNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						INVOKEVIRTUAL_COUNT++;
						if (INVOKEVIRTUAL_COUNT == 9) {
							targetNode = currentNode;
							invok_index = index;
							break;
						}
					}
				}
				if (targetNode == null || invok_index == -1) {
					RMLog.info("[WorldServer] Did not find all necessary target nodes! ABANDON CLASS!", true);
					return bytes;
				}
				AbstractInsnNode p1 = method.instructions.get(invok_index);
				MethodInsnNode a1 = new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/world/WorldServer", "resetRainAndThunder", "()V", false);

				method.instructions.set(p1, a1);
				break;
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public byte[] patchWakeAllPlayers(String name, byte[] bytes, boolean obfuscated) {
		RMLog.info("[WorldServer] [wakeAllPlayers] Patching", true);
		String targetMethodName = "";

		if (obfuscated == true) targetMethodName = "d";
		else targetMethodName = "wakeAllPlayers";

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		Iterator<MethodNode> methods = classNode.methods.iterator();
		while (methods.hasNext()) {
			MethodNode method = methods.next();
			int invok_index = -1;
			if ((method.name.equals(targetMethodName) && method.desc.equals("()V"))) {
				AbstractInsnNode currentNode = null;
				AbstractInsnNode targetNode = null;

				Iterator<AbstractInsnNode> iter = method.instructions.iterator();
				int index = -1;
				while (iter.hasNext()) {
					index++;
					currentNode = iter.next();
					if (currentNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						invok_index = index;
						targetNode = currentNode;
						break;
					}
				}
				if (targetNode == null || invok_index == -1) {
					RMLog.info("[WorldServer] Did not find all necessary target nodes! ABANDON CLASS!", true);
					return bytes;
				}
				AbstractInsnNode p1 = method.instructions.get(invok_index);
				MethodInsnNode p2 = new MethodInsnNode(Opcodes.INVOKESTATIC, "net/roryclaasen/asm/rorysmodcore/transformer/StaticClass", "shouldWakeUp", "()Z", false);

				method.instructions.set(p1, p2);
				method.instructions.remove(method.instructions.get(invok_index - 1));
				break;
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}