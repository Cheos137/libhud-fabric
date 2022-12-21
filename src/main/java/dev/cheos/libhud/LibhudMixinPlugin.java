package dev.cheos.libhud;

import static org.objectweb.asm.Opcodes.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.service.MixinService;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot;

public class LibhudMixinPlugin implements IMixinConfigPlugin {
	private static final String GUI_CLASS_NAME = "net.minecraft.client.gui.Gui"; // Gui.class.getName();
	private final List<String> mixinClasses = new LinkedList<>();
	private final LibhudStreamHandler urlHandle = new LibhudStreamHandler();
	
	@Override
	public void onLoad(String mixinPackage) {
		
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
		try {
			Class<?> cMixinTransformer = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
			Class<?> cMixinProcessor = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");
			Class<?> cMixinConfig = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");
			
			Method mGetTransformer = MixinServiceKnot.class.getDeclaredMethod("getTransformer");
			Method mHasMixinsFor = cMixinConfig.getDeclaredMethod("hasMixinsFor", String.class);
			Method mGetMixinsFor = cMixinConfig.getDeclaredMethod("getMixinsFor", String.class);
			mGetTransformer.setAccessible(true);
			mHasMixinsFor.setAccessible(true);
			mGetMixinsFor.setAccessible(true);
			
			Field fProcessor = cMixinTransformer.getDeclaredField("processor");
			Field fConfigs = cMixinProcessor.getDeclaredField("pendingConfigs");
			fProcessor.setAccessible(true);
			fConfigs.setAccessible(true);
			
			IMixinTransformer transformer = (IMixinTransformer) mGetTransformer.invoke(null);
			Object processor = fProcessor.get(transformer);
			List<? extends IMixinConfig> configs = (List<? extends IMixinConfig>) fConfigs.get(processor);
			List<IMixinInfo> mixins = new ArrayList<>();
			for (IMixinConfig config : configs)
				if ((boolean) mHasMixinsFor.invoke(config, GUI_CLASS_NAME))
					mixins.addAll((List<? extends IMixinInfo>) mGetMixinsFor.invoke(config, GUI_CLASS_NAME));
			
			for (IMixinInfo mixin : mixins) {
				String className = mixin.getClassName();
				shadowCopyClass(className);
				this.mixinClasses.add("shadow." + className);
			}
			
			if (!this.mixinClasses.isEmpty())
				addClassSource();
			
			Libhud.LOGGER.debug("target mixins {}", mixins);
			Libhud.LOGGER.debug("dyn mixin classes {}", this.mixinClasses);
		} catch (Exception e) {
			Libhud.LOGGER.error(e);
		}
	}
	
	@Override
	public List<String> getMixins() {
		return this.mixinClasses;
	}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true; // does not apply to #getMixins()
	}
	
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		Libhud.LOGGER.info("preApply {}", mixinClassName);
	}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		
	}
	
	
	
	private void shadowCopyClass(String className) {
		if (MixinService.getService() instanceof MixinServiceKnot service) {
			try {
				Libhud.LOGGER.debug("shadowing {}", className);
				
				ClassWriter cw = new ClassWriter(ASM9);
				ClassNode src = service.getClassNode(className, false);
				ClassNode dst = LibhudMixinTransformer.transform(src);
				
				className = "dev.cheos.libhud.mixin.shadow." + className;
				dst.name = className.replace('.', '/');
				dst.accept(cw);
				
				this.urlHandle.add(className, cw.toByteArray());
			} catch (Exception e) {
				Libhud.LOGGER.error("Error shadowing mixin class", e);
			}
		}
	}
	
	private void addClassSource() throws ReflectiveOperationException, IllegalArgumentException, MalformedURLException {
		Class<?> cClassLoaderAccess = Class.forName("net.fabricmc.loader.impl.launch.knot.KnotClassDelegate$ClassLoaderAccess");
		Method mAddUrlFwd = cClassLoaderAccess.getDeclaredMethod("addUrlFwd", URL.class);
		mAddUrlFwd.setAccessible(true);
		ClassLoader cl = FabricLauncherBase.getLauncher().getTargetClassLoader();
		if (cClassLoaderAccess.isInstance(cl))
			mAddUrlFwd.invoke(cl, this.urlHandle.url());
	}
}
