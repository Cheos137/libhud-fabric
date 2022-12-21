package dev.cheos.libhud;

import java.io.*;
import java.net.*;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

// thanks to https://github.com/Chocohead/Fabric-ASM for helping me find this solution
public class LibhudStreamHandler extends URLStreamHandler {
	private final Map<String, byte[]> classes = new HashMap<>();
	
	void add(String className, byte[] b) {
		this.classes.put("/" + className.replace('.', '/') + ".class", b);
	}
	
	URL url() {
		try {
			return new URL("libhud-mixin", null, -1, "/", this);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		String path = url.getPath();
		if (this.classes.containsKey(path))
			return new LibhudConnection(url, this.classes.get(path));
		return null;
	}
	
	private static final class LibhudConnection extends URLConnection {
		private final byte[] b;
		
		private LibhudConnection(URL url, byte[] b) {
			super(url);
			this.b = b;
		}

		@Override
		public void connect() throws IOException {
			throw new UnsupportedOperationException("connections not supported by " + getClass().getSimpleName());
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(this.b);
		}
		
		@Override public Permission getPermission() throws IOException { return null; }
	}
}
