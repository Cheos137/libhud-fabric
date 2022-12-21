package dev.cheos.libhud;

import org.objectweb.asm.tree.ClassNode;

public class LibhudMixinTransformer {
	public static ClassNode transform(ClassNode src) {
		ClassNode dst = new ClassNode();
		src.accept(dst);
		
		
		
		
		
		
		return dst;
	}
	
	
}
