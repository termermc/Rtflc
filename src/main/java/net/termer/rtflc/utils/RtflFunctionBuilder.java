package net.termer.rtflc.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import net.termer.rtflc.runtime.RtflFunction;
import net.termer.rtflc.runtime.RtflRuntime;
import net.termer.rtflc.runtime.RuntimeException;
import net.termer.rtflc.runtime.Scope;
import net.termer.rtflc.type.NullType;
import net.termer.rtflc.type.RtflType;

/**
 * Utility class to build Rtfl functions from Java classes and methods
 * @author termer
 * @since 1.0
 */
public class RtflFunctionBuilder {
	/**
	 * Creates a new Rtfl function from the provided static Java method
	 * @param clazz The class which contains the method
	 * @param name The name of the method
	 * @param parameters The method's parameter types
	 * @return The new Rtfl function for the provided method data
	 * @throws SecurityException If Java is not allowed to access the specified method
	 * @throws NoSuchMethodException If the specified method does not exist
	 * @since 1.0
	 */
	public static RtflFunction fromStaticMethod(Class<?> clazz, String name, Class<?>[] parameters) throws NoSuchMethodException, SecurityException {
		return new JavaMethodFunction(clazz, name, parameters, null);
	}
	/**
	 * Creates a new Rtfl function from the provided Java method
	 * @param clazz The class which contains the method
	 * @param name The name of the method
	 * @param parameters The method's parameter types
	 * @param obj The Object to use when invoking the method
	 * @return The new Rtfl function for the provided method data
	 * @throws SecurityException If Java is not allowed to access the specified method
	 * @throws NoSuchMethodException If the specified method does not exist
	 * @since 1.0
	 */
	public static RtflFunction fromMethod(Class<?> clazz, String name, Class<?>[] parameters, Object obj) throws NoSuchMethodException, SecurityException {
		return new JavaMethodFunction(clazz, name, parameters, obj);
	}
	
	/**
	 * RtflFunction implementation that can execute a Java method and proxy its output to a proper RtflType object
	 * @author termer
	 * @since 1.0
	 */
	public static class JavaMethodFunction implements RtflFunction {
		private final Class<?>[] _params;
		private final Object _obj;
		private final Method _method;
		
		public JavaMethodFunction(Class<?> clazz, String name, Class<?>[] parameters, Object obj) throws NoSuchMethodException, SecurityException {
			_params = parameters;
			_obj = obj;
			_method = clazz.getMethod(name, parameters);
		}
		
		public RtflType run(RtflType[] args, RtflRuntime runtime, Scope scope) throws RuntimeException {
			RtflType result = new NullType();
			
			// Fetch Java type arguments
			ArrayList<Object> jargs = new ArrayList<Object>();
			for(RtflType rt : args)
				jargs.add(RtflType.toJavaType(rt));
			
			// Check if param types match
			if(args.length == _params.length) {
				boolean match = true;
				for(int i = 0; i < args.length; i++)
					if(!ClassUtils.classMatches(jargs.get(i).getClass(), _params[i])) {
						match = false;
						break;
					}
				
				// Throw error if types did not match
				if(!match)
					throw new RuntimeException("Argument types do not match");
				
				// Invoke method
				try {
					result = RtflType.fromJavaType(_method.invoke(_obj, jargs.toArray()));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					// Invoking the method failed
					e.printStackTrace();
					throw new RuntimeException("Failed to execute method");
				}
			} else {
				throw new RuntimeException("Must provide "+_params.length+" argument"+(_params.length == 1 ? "" : 's'));
			}
			
			return result;
		}
	}
}
