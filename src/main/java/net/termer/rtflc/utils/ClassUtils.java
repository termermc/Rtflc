package net.termer.rtflc.utils;

/**
 * Utility class to box and unblock primitives
 * @author termer
 * @since 1.0
 */
public class ClassUtils {
	/**
	 * Returns if the provided classes are effectively the same, ignoring primitive boxing
	 * @param c1 The first class
	 * @param c2 The second class
	 * @return Whether the two classes are effectively the same
	 * @since 1.0
	 */
	public static boolean classMatches(Class<?> c1, Class<?> c2) {
		boolean matches = false;
		
		// Lookup table basically
		if(c2.isAssignableFrom(c1))
			matches = true;
		else if(c1 == Integer.class || c1 == int.class)
			matches = c2 == Integer.class || c2 == int.class;
		else if(c1 == Boolean.class || c1 == boolean.class)
			matches = c2 == Boolean.class || c2 == boolean.class;
		else if(c1 == Byte.class || c1 == byte.class)
			matches = c2 == Byte.class || c2 == byte.class;
		else if(c1 == Character.class || c1 == char.class)
			matches = c2 == Character.class || c2 == char.class;
		else if(c1 == Float.class || c1 == float.class)
			matches = c2 == Float.class || c2 == float.class;
		else if(c1 == Long.class || c1 == long.class)
			matches = c2 == Long.class || c2 == long.class;
		else if(c1 == Short.class || c1 == short.class)
			matches = c2 == Short.class || c2 == short.class;
		else if(c1 == Double.class || c1 == double.class)
			matches = c2 == Double.class || c2 == double.class;
		
		return matches;
	}
	
	/**
	 * Checks if two arrays of classes match eachother, ignoring primitive boxing
	 * @param cl1 The first class array
	 * @param cl2 The second class array
	 * @return Whether the two class arrays are effectively the same
	 * @since 1.0
	 */
	public static boolean classesMatch(Class<?>[] cl1, Class<?>[] cl2) {
		boolean match = true;
		
		if(cl1.length == cl2.length) {
			for(int i = 0; i < cl1.length; i++)
				if(!classMatches(cl1[i], cl2[i])) {
					match = false;
					break;
				}
		} else {
			match = false;
		}
		
		return match;
	}
	
	/**
	 * Gets the primitive version of a boxed primitive class, like Integer.
	 * If class is not a boxed primitive, this method just returns the original class.
	 * @param boxed The boxed to class to unbox
	 * @return The unboxed version of the boxed primitive, or the original class if not a boxed primitive
	 * @since 1.0
	 */
	public static Class<?> toPrimitive(Class<?> boxed) {
		Class<?> clazz = boxed;
		
		if(boxed == Integer.class)
			clazz = int.class;
		else if(boxed == Boolean.class)
			clazz = boolean.class;
		else if(boxed == Byte.class)
			clazz = byte.class;
		else if(boxed == Character.class)
			clazz = char.class;
		else if(boxed == Float.class)
			clazz = float.class;
		else if(boxed == Short.class)
			clazz = short.class;
		else if(boxed == Double.class)
			clazz = double.class;
		else if(boxed == Boolean.class)
			clazz = boolean.class;
		else if(boxed == Boolean.class)
			clazz = boolean.class;
		
		return clazz;
	}
}