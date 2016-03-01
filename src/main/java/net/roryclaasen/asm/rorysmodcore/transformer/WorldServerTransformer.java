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
				RMLog.info("About to patch WorldServer [mt]", true);
				data = patchWakeAllPlayers(arg0, data, true);
				data = patchTick(arg0, data, true);
			}
			if (arg0.equals("net.minecraft.world.WorldServer")) {
				RMLog.info("About to patch WorldServer [net.minecraft.world.WorldServer]", true);
				data = patchWakeAllPlayers(arg0, data, false);
				data = patchTick(arg0, data, false);
			}
		} catch (Exception e) {
			RMLog.warn("Patch failed!", true);
			e.printStackTrace();
		}
		if (data != arg2) {
			RMLog.info("Finnished Patching! and applied changes", true);
		}
		return data;
	}

	public byte[] patchTick(String name, byte[] bytes, boolean obfuscated) {
		RMLog.info("[tick] Patching", true);
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
				@SuppressWarnings("unused")
				AbstractInsnNode targetNode = null;

				Iterator<AbstractInsnNode> iter = method.instructions.iterator();

				int index = -1;

				while (iter.hasNext()) {
					index++;
					currentNode = iter.next();
					/*
					 * mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
					 * mv.visitVarInsn(ALOAD, 0);
					 * mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/WorldServer", "wakeAllPlayers", "()V", false);
					 */
					int INVOKEVIRTUAL_COUNT = 0;
					if (currentNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						INVOKEVIRTUAL_COUNT++;
						targetNode = currentNode;

						invok_index = index;
						if (INVOKEVIRTUAL_COUNT == 9) break;
					}
				}
				AbstractInsnNode p1, p2, p3, p4, p5;
				p1 = method.instructions.get(invok_index);
				p2 = method.instructions.get(invok_index - 1);
				p3 = method.instructions.get(invok_index - 2);
				p4 = method.instructions.get(invok_index - 3);
				p5 = method.instructions.get(invok_index - 4);

				method.instructions.remove(p5);
				method.instructions.remove(p4);
				method.instructions.remove(p3);
				method.instructions.remove(p2);
				method.instructions.remove(p1);
				break;
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public byte[] patchWakeAllPlayers(String name, byte[] bytes, boolean obfuscated) {
		RMLog.info("[wakeAllPlayers] Patching", true);
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

				Iterator<AbstractInsnNode> iter = method.instructions.iterator();
				int index = -1;
				while (iter.hasNext()) {
					index++;
					currentNode = iter.next();
					if (currentNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
						invok_index = index;
						break;
					}
				}

				AbstractInsnNode p1;
				p1 = method.instructions.get(invok_index);
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