package net.termer.rtflc.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.termer.rtflc.utils.ClassUtils;

/**
 * Utility class to setup Rtfl/Java interop functions
 * @author termer
 * @since 1.0
 */
public class JavaInteropFunctions {
	public JavaInteropFunctions(RtflRuntime rt) {
		/* Create functions */
		
		try {
			rt.exposeMethod(this, "java", new Class<?>[] {String.class, Object[].class});
			rt.exposeMethod(this, "jmethod", new Class<?>[] {Object.class, String.class, Object[].class});
		} catch(Exception e) {
			System.err.println("Serious issue occurred when trying to load Java interop functions:");
			e.printStackTrace();
		}
	}
	
	public Object java(String name, Object[] args) throws ClassNotFoundException, RuntimeException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Class.forName(name);
		
		// Fetch argument types
		Class<?>[] types = new Class<?>[args.length];
		for(int i = 0; i < args.length; i++)
			types[i] = ClassUtils.toPrimitive(args[i].getClass());
		
		// Find constructor
		Constructor<?> cons = null;
		for(Constructor<?> con : clazz.getConstructors()) {
			con.setAccessible(true);
			if(ClassUtils.classesMatch(types, con.getParameterTypes())) {
				cons = con;
				break;
			}
		}
		
		if(cons == null)
			throw new RuntimeException("No constructor exists with the specified argument types");
		
		return cons.newInstance(args);
	}
	public Object jmethod(Object object, String name, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> clazz = object.getClass();
		
		// Fetch argument types
		Class<?>[] types = new Class<?>[args.length];
		for(int i = 0; i < args.length; i++)
			types[i] = ClassUtils.toPrimitive(args[i].getClass());
		
		// Find method
		Method mtd = null;
		for(Method m : clazz.getMethods()) {
			m.setAccessible(true);
			if(m.getName().equals(name) && ClassUtils.classesMatch(types, m.getParameterTypes())) {
				mtd = m;
				break;
			}
		}
		
		return mtd.invoke(object, args);
	}
}
