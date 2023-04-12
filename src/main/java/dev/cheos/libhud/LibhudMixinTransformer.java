package dev.cheos.libhud;

import static org.objectweb.asm.Opcodes.*;

import java.util.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.util.Annotations;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class LibhudMixinTransformer {
	static final String LIBHUD_GUI_INTERNAL_NAME = LibhudMixinPlugin.LIBHUD_GUI_CLASS_NAME.replace('.', '/');
	static final Type LIBHUD_GUI_TYPE = Type.getObjectType(LIBHUD_GUI_INTERNAL_NAME);
	
	static final String SHADOW = desc(Shadow.class),                // https://github.com/SpongePowered/Mixin/blob/master/src/main/java/org/spongepowered/asm/mixin/Shadow.java
						INJECT = desc(Inject.class),                // https://github.com/SpongePowered/Mixin/blob/master/src/main/java/org/spongepowered/asm/mixin/injection/Inject.java
						MODIFY_VAR = desc(ModifyVariable.class),    // https://github.com/SpongePowered/Mixin/blob/master/src/main/java/org/spongepowered/asm/mixin/injection/ModifyVariable.java
						MODIFY_CONST = desc(ModifyConstant.class),  // https://github.com/SpongePowered/Mixin/blob/master/src/main/java/org/spongepowered/asm/mixin/injection/ModifyConstant.java
						MODIFY_ARGS = desc(ModifyArgs.class),       // https://github.com/SpongePowered/Mixin/blob/master/src/main/java/org/spongepowered/asm/mixin/injection/ModifyArgs.java
						MODIFY_ARG = desc(ModifyArg.class);         // https://github.com/SpongePowered/Mixin/blob/master/src/main/java/org/spongepowered/asm/mixin/injection/ModifyArg.java
	
	static final Set<String> REMOVE_TYPES = ImmutableSet.of(
//			SHADOW,
//			INJECT,
//			MODIFY_VAR,
//			MODIFY_CONST,
//			MODIFY_ARGS,
//			MODIFY_ARG,
			desc(Overwrite.class),       // probably directly tied to super, otherwise don't care anyways
			desc(Redirect.class),        // probably directly tied to super, otherwise don't care anyways
			desc(Invoker.class),         // directly tied to super, we don't care
			desc(Accessor.class),        // directly tied to super, we don't care
			desc(SoftOverride.class)     // don't care
	);
	
	public LibhudMixinTransformer() {
		super();
	}
	
	public static ClassNode transform(ClassNode src) {
		ClassNode dst = new ClassNode();
		src.accept(dst);
		transformClass(dst);
		return dst;
	}
	
	private static void transformClass(ClassNode cnode) {
		cnode.access |= ACC_SYNTHETIC;
		
		ctortransform:
		if ("java/lang/Object".equals(cnode.superName)) { // we can safely transform // TODO: otherwise we can just hope for the best or should we not transform at all?
			if (!new ArrayList<>(cnode.methods).removeIf(mnode -> "<init>".equals(mnode.name) && mnode.parameters.isEmpty())) {
				MethodNode mnode = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, new String[0]);
				InsnList insns = mnode.instructions;
				cnode.superName = LIBHUD_GUI_INTERNAL_NAME;
				insns.clear();
				insns.add(new VarInsnNode(ALOAD, 0));
				insns.add(new InsnNode(ACONST_NULL));
				insns.add(new InsnNode(ACONST_NULL));
				insns.add(new MethodInsnNode(INVOKESPECIAL, LIBHUD_GUI_INTERNAL_NAME, "<init>", "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/entity/ItemRenderer;)V"));
				insns.add(new InsnNode(RETURN));
			} else {
				MethodNode mnode = null;
				for (MethodNode node : cnode.methods)
					if ("<init>".equals(node.name) && node.parameters.isEmpty()) {
						mnode = node;
						break;
					}
				if (mnode == null)
					break ctortransform; // this can never happen, check just for safety anyways
				InsnList insns = mnode.instructions;
				if (insns.size() < 3)
					break ctortransform; // something's wrong, we bail (there's no way a super call can fit into less than 3 insns (ALOAD_0, INVOKESPECIAL, RETURN is the minimum)
				if (insns.get(0) instanceof VarInsnNode vinode && vinode.getOpcode() != ALOAD && vinode.var != 0)
					break ctortransform; // something's wrong with the original super call
				cnode.superName = LIBHUD_GUI_INTERNAL_NAME;
				AbstractInsnNode aload0 = insns.get(0);
				Iterator<AbstractInsnNode> it = insns.iterator(1);
				for (AbstractInsnNode node = it.next(); it.hasNext(); node = it.next()) // remove original super call
					if (node.getOpcode() == INVOKESPECIAL) {
						it.remove();
						break;
					} else it.remove();
				// insns following are in reverse order as they are all inserted *after* the first insn -> last must be inserted first, others get inserted in front of it
				insns.insert(aload0, new MethodInsnNode(INVOKESPECIAL, LIBHUD_GUI_INTERNAL_NAME, "<init>", "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/entity/ItemRenderer;)V"));
				insns.insert(aload0, new InsnNode(ACONST_NULL));
				insns.insert(aload0, new InsnNode(ACONST_NULL));
			}
		}
		
		for (Iterator<MethodNode> it = cnode.methods.iterator(); it.hasNext();)
			transformMethod(cnode, it.next(), it::remove);
		for (Iterator<FieldNode> it = cnode.fields.iterator(); it.hasNext();)
			transformField(cnode, it.next(), it::remove);
		
		AnnotationNode mixin = Annotations.getInvisible(cnode, Mixin.class);
		Iterable<Type> values = Annotations.getValue(mixin, "value");
		Iterable<String> targets = Annotations.getValue(mixin, "targets");
		
		if (values != null) {
			ArrayList<Type> valueList = Lists.newArrayList(Annotations.<Iterable<Type>>getValue(mixin, "value"));
			for (int i = 0; i < valueList.size(); i++)
				if (LibhudMixinPlugin.GUI_CLASS_NAME.equals(valueList.get(i).getClassName()))
					valueList.set(i, LIBHUD_GUI_TYPE);
			Annotations.setValue(mixin, "value", valueList);
		}
		
		if (targets != null) {
			ArrayList<String> targetList = Lists.newArrayList(Annotations.<Iterable<String>>getValue(mixin, "targets"));
			for (int i = 0; i < targetList.size(); i++)
				if (LibhudMixinPlugin.GUI_CLASS_NAME.equals(targetList.get(i)))
					targetList.set(i, LibhudMixinPlugin.LIBHUD_GUI_CLASS_NAME);
			Annotations.setValue(mixin, "targets", targetList);
		}
	}
	
	/*
	 * TODO:
	 * - remap injection points / discard if no matching injection point
	 * - discard shadow methods/fields and remap all references to them
	 * -> INJECT, SHADOW done
	 * - how do MODIFY_***** work? probably need to remap references for them if applicable / otherwise discard
	 */
	
	private static void transformMethod(ClassNode cnode, MethodNode mnode, Runnable removeHook) {
		Set<String> annotations = new HashSet<>();
		for (AnnotationNode anode : mnode.visibleAnnotations) {
			annotations.add(anode.desc);
			if (REMOVE_TYPES.contains(anode.desc)) {
				removeHook.run();
				return;
			}
		}
		for (AnnotationNode anode : mnode.invisibleAnnotations) {
			annotations.add(anode.desc);
			if (REMOVE_TYPES.contains(anode.desc)) {
				removeHook.run();
				return;
			}
		}
		
		
		
		
		
	}
	
	private static void transformField(ClassNode cnode, FieldNode fnode, Runnable removeHook) {
		Set<String> annotations = new HashSet<>();
		for (AnnotationNode anode : fnode.visibleAnnotations) {
			annotations.add(anode.desc);
			if (REMOVE_TYPES.contains(anode.desc)) {
				removeHook.run();
				return;
			}
		}
		for (AnnotationNode anode : fnode.invisibleAnnotations) {
			annotations.add(anode.desc);
			if (REMOVE_TYPES.contains(anode.desc)) {
				removeHook.run();
				return;
			}
		}
		
		
		
		
		
		
	}
	
	
	private static String desc(Class<?> clazz) {
		return Type.getDescriptor(clazz);
	}
	
	public static class TransformationException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		public TransformationException(String message) {
			super(message);
		}
	}
}
