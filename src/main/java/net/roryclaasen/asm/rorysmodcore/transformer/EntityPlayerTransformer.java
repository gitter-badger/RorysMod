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

public class EntityPlayerTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) {
		byte[] data = arg2;
		try {
			if (arg0.equals("yz")) {
				RMLog.info("[EntityPlayer] About to patch [yz]", true);
				data = patchOnUpdate(arg0, data, true);
			}
			if (arg0.equals("net.minecraft.entity.player.EntityPlayer")) {
				RMLog.info("[EntityPlayer] About to patch [net.minecraft.entity.player.EntityPlayer]", true);
				data = patchOnUpdate(arg0, data, false);
			}
		} catch (Exception e) {
			RMLog.warn("[EntityPlayer] Patch failed!", true);
			e.printStackTrace();
		}
		if (data != arg2) {
			RMLog.info("[EntityPlayer] Finnished Patching! and applied changes", true);
		} else {
			// RMLog.info("[EntityPlayer] No changes applied", true);
		}
		return data;
	}

	public byte[] patchOnUpdate(String name, byte[] bytes, boolean obfuscated) {
		RMLog.info("[EntityPlayer] [onUpdate] Patching", true);
		String targetMethodName = "";

		if (obfuscated == true) targetMethodName = "h";
		else targetMethodName = "onUpdate";
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

				int GETFIELD_COUNT = 0;
				while (iter.hasNext()) {
					index++;
					currentNode = iter.next();
					if (currentNode.getOpcode() == Opcodes.GETFIELD) {
						GETFIELD_COUNT++;
						if (GETFIELD_COUNT == 13) {
							targetNode = currentNode;
							invok_index = index;
							break;
						}
					}
				}
				if (targetNode == null || invok_index == -1) {
					RMLog.info("[EntityPlayer] Did not find all necessary target nodes! ABANDON CLASS!", true);
					return bytes;
				}
				/*
				 * mv.visitLineNumber(305, l19);
				 * mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				 * mv.visitVarInsn(ALOAD, 0);
				 * -- mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/player/EntityPlayer", "worldObj", "Lnet/minecraft/world/World;");
				 * mv.visitFieldInsn(GETFIELD, "net/minecraft/world/World", "isRemote", "Z");
				 * Label l21 = new Label();
				 * mv.visitJumpInsn(IFNE, l21);
				 */
				@SuppressWarnings("unused")
				AbstractInsnNode p1, p2, p3;
				p1 = method.instructions.get(invok_index - 1); // mv.visitVarInsn(ALOAD, 0);
				p2 = method.instructions.get(invok_index); // mv.visitFieldInsn(GETFIELD, "net/minecraft/entity/player/EntityPlayer", "worldObj", "Lnet/minecraft/world/World;");
				p3 = method.instructions.get(invok_index + 1); // mv.visitFieldInsn(GETFIELD, "net/minecraft/world/World", "isRemote", "Z");

				//method.instructions.remove(p1);
				//method.instructions.remove(p2);
				//method.instructions.remove(p3);
				
				MethodInsnNode m1 = new MethodInsnNode(Opcodes.INVOKESTATIC, "net/roryclaasen/asm/rorysmodcore/transformer/StaticClass", "shouldWakeUp", "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false);
				
				method.instructions.set(p2, m1);
				method.instructions.remove(p3);
				break;
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}