//package dev.cheos.libhud;
//
//import java.lang.annotation.Annotation;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import org.objectweb.asm.Handle;
//import org.objectweb.asm.Opcodes;
//import org.objectweb.asm.tree.*;
//import org.spongepowered.asm.logging.Level;
//import org.spongepowered.asm.mixin.*;
//import org.spongepowered.asm.mixin.MixinEnvironment.CompatibilityLevel;
//import org.spongepowered.asm.mixin.MixinEnvironment.Phase;
//import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
//import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
//import org.spongepowered.asm.mixin.transformer.ClassInfo;
//import org.spongepowered.asm.mixin.transformer.ext.Extensions;
//import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
//import org.spongepowered.asm.util.Annotations;
//import org.spongepowered.asm.util.LanguageFeatures;
//import org.spongepowered.asm.util.asm.MethodNodeEx;
//
///**
// * Near duplicate of org.spongepowered.asm.mixin.transformer.MixinInfo, used for easy access to access-restricted class.
// */
//public class WrappedMixinInfo {
//	enum Variant {
//		STANDARD,
//		INTERFACE,
//		ACCESSOR,
//		PROXY;
//	}
//
//	public class MixinMethodNode {
//		private static final Class<?> REF_CLASS;
//		private static final Method mVisitInvokeDynamicInsn, mIsInjector, mIsSurrogate, mIsSynthetic, mGetVisibleAnnotation, mGetInjectorAnnotation;
//
//		static {
//			try {
//				REF_CLASS = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo$MixinMethodNode");
//				mVisitInvokeDynamicInsn = REF_CLASS.getDeclaredMethod("visitInvokeDynamicInsn", String.class, String.class, Handle.class, Object[].class);
//				mIsInjector = REF_CLASS.getDeclaredMethod("isInjector");
//				mIsSurrogate = REF_CLASS.getDeclaredMethod("isSurrogate");
//				mIsSynthetic = REF_CLASS.getDeclaredMethod("isSynthetic");
//				mGetVisibleAnnotation = REF_CLASS.getDeclaredMethod("getVisibleAnnotation", Class.class);
//				mGetInjectorAnnotation = REF_CLASS.getDeclaredMethod("getInjectorAnnotation");
//
//				for (Field f : MixinMethodNode.class.getDeclaredFields())
//					if (f.get(null) instanceof Method m)
//						m.setAccessible(true);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		private final MethodNodeEx handle;
//
//		public MixinMethodNode(MethodNodeEx handle) {
//			if (!REF_CLASS.isInstance(handle)) throw new IllegalArgumentException("handle must be of type " + REF_CLASS.getName());
//			this.handle = handle;
//		}
//
//		public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
//			try {
//				mVisitInvokeDynamicInsn.invoke(this.handle, name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		public boolean isInjector() {
//			try {
//				return (boolean) mIsInjector.invoke(this.handle);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		public boolean isSurrogate() {
//			try {
//				return (boolean) mIsSurrogate.invoke(this.handle);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		public boolean isSynthetic() {
//			try {
//				return (boolean) mIsSynthetic.invoke(this.handle);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		public AnnotationNode getVisibleAnnotation(Class<? extends Annotation> annotationClass) {
//			try {
//				return (AnnotationNode) mGetVisibleAnnotation.invoke(this.handle, annotationClass);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		public AnnotationNode getInjectorAnnotation() {
//			try {
//				return (AnnotationNode) mGetInjectorAnnotation.invoke(this.handle);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		public MethodNodeEx getHandle() {
//			return this.handle;
//		}
//	}
//
//	public class MixinClassNode {
//		private static final Class<?> REF_CLASS;
//		private static final Method mGetFields;
//		private static final Field fMixinMethods;
//
//		static {
//			try {
//				REF_CLASS = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo$DeclaredTarget");
//				mGetFields = REF_CLASS.getDeclaredMethod("getFields");
//				fMixinMethods = REF_CLASS.getDeclaredField("mixinMethods");
//				mGetFields.setAccessible(true);
//				fMixinMethods.setAccessible(true);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		private final ClassNode handle;
//
//		public MixinClassNode(ClassNode handle) {
//			this.handle = handle;
//		}
//
//		@SuppressWarnings("unchecked")
//		public List<MixinMethodNode> getMixinMethods() {
//			try {
//				return (List<MixinMethodNode>) fMixinMethods.get(this.handle);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		@SuppressWarnings("unchecked")
//		public List<FieldNode> getFields() {
//			try {
//				return (List<FieldNode>) mGetFields.invoke(this.handle);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		public ClassNode getHandle() {
//			return this.handle;
//		}
//	}
//
//	class State {
//		private final ClassNode classNode;
//		private final ClassInfo classInfo;
//		private boolean detachedSuper;
//		private boolean unique;
//		public final Set<String> interfaces = new HashSet<>();
//		public final List<InterfaceInfo> softImplements = new ArrayList<>();
//		public final Set<String> syntheticInnerClasses = new HashSet<>();
//		public final Set<String> innerClasses = new HashSet<>();
//		public MixinClassNode validationClassNode;
//
//		State(ClassNode classNode) {
//			this(classNode, null);
//		}
//
//		State(ClassNode classNode, ClassInfo classInfo) {
//			this.classNode = classNode;
//			this.connect();
//			this.classInfo = classInfo != null ? classInfo : ClassInfo.fromClassNode(this.getValidationClassNode());
//		}
//
//		public void connect() {
//			this.validationClassNode = this.createClassNode(0);
//		}
//
//		public void complete() {
//			this.validationClassNode = null;
//		}
//
//		ClassInfo getClassInfo() {
//			return this.classInfo;
//		}
//
//		ClassNode getClassNode() {
//			return this.classNode;
//		}
//
//		MixinClassNode getValidationClassNode() {
//			if (this.validationClassNode == null) {
//				throw new IllegalStateException("Attempted a validation task after validation is complete on " + this + " in " + WrappedMixinInfo.this);
//			}
//			return this.validationClassNode;
//		}
//
//		boolean isDetachedSuper() {
//			return this.detachedSuper;
//		}
//
//		boolean isUnique() {
//			return this.unique;
//		}
//
//		List<? extends InterfaceInfo> getSoftImplements() {
//			return this.softImplements;
//		}
//
//		Set<String> getSyntheticInnerClasses() {
//			return this.syntheticInnerClasses;
//		}
//
//		Set<String> getInnerClasses() {
//			return this.innerClasses;
//		}
//
//		Set<String> getInterfaces() {
//			return this.interfaces;
//		}
//
//		MixinClassNode createClassNode(int flags) {
//			MixinClassNode mixinClassNode = new MixinClassNode(WrappedMixinInfo.this);
//			this.classNode.accept(mixinClassNode);
//			return mixinClassNode;
//		}
//
//		void validate(SubType type, List<ClassInfo> targetClasses) {
//			MixinClassNode classNode = this.getValidationClassNode();
//
//			MixinPreProcessorStandard preProcessor = type.createPreProcessor(classNode).prepare(WrappedMixinInfo.this.getExtensions());
//			for (ClassInfo target : targetClasses) {
//				preProcessor.conform(target);
//			}
//
//			type.validate(this, targetClasses);
//
//			this.detachedSuper = type.isDetachedSuper();
//			this.unique = Annotations.getVisible(classNode, Unique.class) != null;
//
//			this.validateInner();
//			this.validateClassFeatures();
//			this.validateRemappables(targetClasses);
//
//			this.readImplementations(type);
//			this.readInnerClasses();
//
//			this.validateChanges(type, targetClasses);
//
//			this.complete();
//		}
//
//		private void validateInner() {
//			if (!this.classInfo.isProbablyStatic()) {
//				throw new InvalidMixinException(WrappedMixinInfo.this, "Inner class mixin must be declared static");
//			}
//		}
//
//		private void validateClassFeatures() {
//			CompatibilityLevel compatibilityLevel = MixinEnvironment.getCompatibilityLevel();
//			int requiredLanguageFeatures = LanguageFeatures.scan(this.validationClassNode);
//			if (requiredLanguageFeatures == 0 || compatibilityLevel.supports(requiredLanguageFeatures)) {
//				return;
//			}
//
//			int missingFeatures = requiredLanguageFeatures & ~compatibilityLevel.getLanguageFeatures();
//			CompatibilityLevel minRequiredLevel = CompatibilityLevel.requiredFor(requiredLanguageFeatures);
//
//			throw new InvalidMixinException(WrappedMixinInfo.this, String.format(
//					"Unsupported mixin, %s requires the following unsupported language features: %s, these features require compatibility level %s",
//					WrappedMixinInfo.this, LanguageFeatures.format(missingFeatures), minRequiredLevel != null ? minRequiredLevel.toString() : "UNKNOWN"));
//		}
//
//		private void validateRemappables(List<ClassInfo> targetClasses) {
//			if (targetClasses.size() > 1) {
//				for (FieldNode field : this.validationClassNode.fields) {
//					this.validateRemappable(Shadow.class, field.name, Annotations.getVisible(field, Shadow.class));
//				}
//
//				for (MethodNode method : this.validationClassNode.methods) {
//					this.validateRemappable(Shadow.class, method.name, Annotations.getVisible(method, Shadow.class));
//					AnnotationNode overwrite = Annotations.getVisible(method, Overwrite.class);
//					if (overwrite != null && ((method.access & Opcodes.ACC_STATIC) == 0 || (method.access & Opcodes.ACC_PUBLIC) == 0)) {
//						throw new InvalidMixinException(WrappedMixinInfo.this, "Found @Overwrite annotation on " + method.name + " in " + WrappedMixinInfo.this);
//					}
//				}
//			}
//		}
//
//		private void validateRemappable(Class<Shadow> annotationClass, String name, AnnotationNode annotation) {
//			if (annotation != null && Annotations.getValue(annotation, "remap", Boolean.TRUE)) {
//				throw new InvalidMixinException(WrappedMixinInfo.this, "Found a remappable @" + annotationClass.getSimpleName() + " annotation on " + name
//						+ " in " + this);
//			}
//		}
//
//		void readImplementations(SubType type) {
//			this.interfaces.addAll(this.validationClassNode.interfaces);
//			this.interfaces.addAll(type.getInterfaces());
//
//			AnnotationNode implementsAnnotation = Annotations.getInvisible(this.validationClassNode, Implements.class);
//			if (implementsAnnotation == null) {
//				return;
//			}
//
//			List<AnnotationNode> interfaces = Annotations.getValue(implementsAnnotation);
//			if (interfaces == null) {
//				return;
//			}
//
//			for (AnnotationNode interfaceNode : interfaces) {
//				InterfaceInfo interfaceInfo = InterfaceInfo.fromAnnotation(WrappedMixinInfo.this, interfaceNode);
//				this.softImplements.add(interfaceInfo);
//				this.interfaces.add(interfaceInfo.getInternalName());
//				// only add interface if its initial initialisation
//				if (!(this instanceof Reloaded)) {
//					this.classInfo.addInterface(interfaceInfo.getInternalName());
//				}
//			}
//		}
//
//		void readInnerClasses() {
//			for (InnerClassNode inner : this.validationClassNode.innerClasses) {
//				ClassInfo innerClass = ClassInfo.forName(inner.name);
//				if ((inner.outerName != null && inner.outerName.equals(this.classInfo.getName()))
//						|| inner.name.startsWith(this.validationClassNode.name + "$")) {
//					if (innerClass.isProbablyStatic() && innerClass.isSynthetic()) {
//						this.syntheticInnerClasses.add(inner.name);
//					} else if (!innerClass.isMixin()) {
//						this.innerClasses.add(inner.name);
//					}
//				}
//			}
//		}
//
//		public void validateChanges(SubType type, List<ClassInfo> targetClasses) {
//			type.createPreProcessor(this.validationClassNode).prepare(WrappedMixinInfo.this.getExtensions());
//		}
//	}
//
//	public static final class DeclaredTarget {
//		private static final Class<?> REF_CLASS;
//		private static final Field fName, fIsPrivate;
//
//		static {
//			try {
//				REF_CLASS = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo$DeclaredTarget");
//				fName = REF_CLASS.getDeclaredField("name");
//				fIsPrivate = REF_CLASS.getDeclaredField("isPrivate");
//				fName.setAccessible(true);
//				fIsPrivate.setAccessible(true);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		private final Object handle;
//
//		public DeclaredTarget(Object handle) {
//			this.handle = handle;
//		}
//
//		public String getName() {
//			try {
//				return (String) fName.get(this.handle);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		public boolean isPrivate() {
//			try {
//				return (boolean) fIsPrivate.get(this.handle);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		@Override
//		public String toString() {
//			return this.handle.toString();
//		}
//	}
//
//	private static final Class<?> REF_CLASS;
//	private static Method mReadDeclaredTargets, mReadTargets, mShouldApplyMixin, mReadPriority,
//			mReadPseudo, mIsReloading, mRemapClassName, mHasDeclaredTarget,
//			mGetState, mGetClassInfo, mGetConfig, mGetParent,
//			mGetPriority, mGetName, mGetClassName, mGetClassRef,
//			mGetClassBytes, mIsDetachedSuper, mIsUnique, mIsVirtual,
//			mIsAccessor, mIsLoadable, mIsRequired, mGetLoggingLevel,
//			mGetPhase, mGetClassNode, mGetDeclaredTargetClasses, mGetTargetClasses,
//			mGetSyntheticInnerClasses, mGetInnerClasses, mGetTargets,
//			mGetInterfaces, mGetExtensions;
//
//	static {
//		try {
//			REF_CLASS = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo");
//			mReadDeclaredTargets = REF_CLASS.getDeclaredMethod("readDeclaredTargets", Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo$MixinClassNode"), boolean.class);
//			mReadTargets = REF_CLASS.getDeclaredMethod("readTargets", AnnotationNode.class);
//			mShouldApplyMixin = REF_CLASS.getDeclaredMethod("shouldApplyMixin", boolean.class, String.class);
//			mReadPriority = REF_CLASS.getDeclaredMethod("readPriority", ClassNode.class);
//			mReadPseudo = REF_CLASS.getDeclaredMethod("readPseudo", ClassNode.class);
//			mIsReloading = REF_CLASS.getDeclaredMethod("isReloading");
//			mRemapClassName = REF_CLASS.getDeclaredMethod("remapClassName", String.class);
//			mHasDeclaredTarget = REF_CLASS.getDeclaredMethod("hasDeclaredTarget", String.class);
//			mGetState = REF_CLASS.getDeclaredMethod("getState");
//			mGetClassInfo = REF_CLASS.getDeclaredMethod("getClassInfo");
//			mGetConfig = REF_CLASS.getDeclaredMethod("getConfig");
//			mGetParent = REF_CLASS.getDeclaredMethod("getParent");
//			mGetPriority = REF_CLASS.getDeclaredMethod("getPriority");
//			mGetName = REF_CLASS.getDeclaredMethod("getName");
//			mGetClassName = REF_CLASS.getDeclaredMethod("getClassName");
//			mGetClassRef = REF_CLASS.getDeclaredMethod("getClassRef");
//			mGetClassBytes = REF_CLASS.getDeclaredMethod("getClassBytes");
//			mIsDetachedSuper = REF_CLASS.getDeclaredMethod("isDetachedSuper");
//			mIsUnique = REF_CLASS.getDeclaredMethod("isUnique");
//			mIsVirtual = REF_CLASS.getDeclaredMethod("isVirtual");
//			mIsAccessor = REF_CLASS.getDeclaredMethod("isAccessor");
//			mIsLoadable = REF_CLASS.getDeclaredMethod("isLoadable");
//			mIsRequired = REF_CLASS.getDeclaredMethod("isRequired");
//			mGetLoggingLevel = REF_CLASS.getDeclaredMethod("getLoggingLevel");
//			mGetPhase = REF_CLASS.getDeclaredMethod("getPhase");
//			mGetClassNode = REF_CLASS.getDeclaredMethod("getClassNode", int.class);
//			mGetDeclaredTargetClasses = REF_CLASS.getDeclaredMethod("getDeclaredTargetClasses");
//			mGetTargetClasses = REF_CLASS.getDeclaredMethod("getTargetClasses");
//			mGetSyntheticInnerClasses = REF_CLASS.getDeclaredMethod("getSyntheticInnerClasses");
//			mGetInnerClasses = REF_CLASS.getDeclaredMethod("getInnerClasses");
//			mGetTargets = REF_CLASS.getDeclaredMethod("getTargets");
//			mGetInterfaces = REF_CLASS.getDeclaredMethod("getInterfaces");
//			mGetExtensions = REF_CLASS.getDeclaredMethod("getExtensions");
//
//			for (Field f : WrappedMixinInfo.class.getDeclaredFields())
//				if (f.get(null) instanceof Method m)
//					m.setAccessible(true);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	private final IMixinInfo handle;
//
//	public WrappedMixinInfo(IMixinInfo handle) {
//		if (!REF_CLASS.isInstance(handle)) throw new IllegalArgumentException("handle must be of type " + REF_CLASS.getName());
//		this.handle = handle;
//	}
//
//	@SuppressWarnings("unchecked")
//	public List<DeclaredTarget> readDeclaredTargets(MixinClassNode classNode, boolean ignorePlugin) {
//		try {
//			return ((List<Object>) mReadDeclaredTargets.invoke(this.handle, classNode.getHandle(), ignorePlugin)).stream().map(DeclaredTarget::new).collect(Collectors.toList());
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public Iterable<Object> readTargets(AnnotationNode mixin) {
//		try {
//			return (Iterable<Object>) mReadTargets.invoke(this.handle, mixin);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean shouldApplyMixin(boolean ignorePlugin, String targetName) {
//		try {
//			return (boolean) mShouldApplyMixin.invoke(this.handle, ignorePlugin, targetName);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public int readPriority(ClassNode classNode) {
//		try {
//			return (int) mReadPriority.invoke(this.handle, classNode);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean readPseudo(ClassNode classNode) {
//		try {
//			return (boolean) mReadPseudo.invoke(this.handle, classNode);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean isReloading() {
//		try {
//			return (boolean) mIsReloading.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public String remapClassName(String className) {
//		try {
//			return (String) mRemapClassName.invoke(this.handle, className);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean hasDeclaredTarget(String targetClass) {
//		try {
//			return (boolean) mHasDeclaredTarget.invoke(this.handle, targetClass);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public State getState() {
//		try {
//			return new State(mGetState.invoke(this.handle));
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public ClassInfo getClassInfo() {
//		try {
//			return (ClassInfo) mGetClassInfo.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public IMixinConfig getConfig() {
//		try {
//			return (IMixinConfig) mGetConfig.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public IMixinConfig getParent() {
//		try {
//			return (IMixinConfig) mGetParent.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public int getPriority() {
//		try {
//			return (int) mGetPriority.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public String getName() {
//		try {
//			return (String) mGetName.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public String getClassName() {
//		try {
//			return (String) mGetClassName.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public String getClassRef() {
//		try {
//			return (String) mGetClassRef.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public byte[] getClassBytes() {
//		try {
//			return (byte[]) mGetClassBytes.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean isDetachedSuper() {
//		try {
//			return (boolean) mIsDetachedSuper.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean isUnique() {
//		try {
//			return (boolean) mIsUnique.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean isVirtual() {
//		try {
//			return (boolean) mIsVirtual.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean isAccessor() {
//		try {
//			return (boolean) mIsAccessor.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean isLoadable() {
//		try {
//			return (boolean) mIsLoadable.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public boolean isRequired() {
//		try {
//			return (boolean) mIsRequired.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public Level getLoggingLevel() {
//		try {
//			return (Level) mGetLoggingLevel.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public Phase getPhase() {
//		try {
//			return (Phase) mGetPhase.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public MixinClassNode getClassNode(int flags) {
//		try {
//			return new MixinClassNode((ClassNode) mGetClassNode.invoke(this.handle, flags));
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public List<String> getDeclaredTargetClasses() {
//		try {
//			return (List<String>) mGetDeclaredTargetClasses.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public List<String> getTargetClasses() {
//		try {
//			return (List<String>) mGetTargetClasses.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public Set<String> getSyntheticInnerClasses() {
//		try {
//			return (Set<String>) mGetSyntheticInnerClasses.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public Set<String> getInnerClasses() {
//		try {
//			return (Set<String>) mGetInnerClasses.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public List<ClassInfo> getTargets() {
//		try {
//			return (List<ClassInfo>) mGetTargets.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	public Set<String> getInterfaces() {
//		try {
//			return (Set<String>) mGetInterfaces.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public Extensions getExtensions() {
//		try {
//			return (Extensions) mGetExtensions.invoke(this.handle);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	@Override
//	public String toString() {
//		return this.handle.toString();
//	}
//
//	public IMixinInfo getHandle() {
//		return this.handle;
//	}
//}
