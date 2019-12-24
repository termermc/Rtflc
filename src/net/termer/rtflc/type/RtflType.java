package net.termer.rtflc.type;

import net.termer.rtflc.runtime.Scope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.termer.rtflc.runtime.RuntimeException;

/**
 * Interface to define Rtfl data types. Classes that implement this are effectively wrappers around Java types.
 * @author termer
 * @since 1.0
 */
public interface RtflType {
	/**
	 * The name of the type
	 * @return The name of the type
	 * @since 1.0
	 */
	public String name();
	/**
	 * The Java value stored in this RtflType wrapper
	 * @return The this wrapper's Java value
	 * @since 1.0
	 */
	public Object value();
	/**
	 * Checks two RtflTypes to see if they qualify as equals in Rtfl
	 * @param value The value to check this RtflType against
	 * @param scope The scope in which to check (to use functions and variables)
	 * @return Whether the provided RtflType equals this RtflType in Rtfl
	 * @throws RuntimeException If any references are invalid or if executing a function fails
	 * @since 1.0
	 */
	public boolean equals(RtflType value, Scope scope) throws RuntimeException;
	
	/** Static utility methods **/
	/**
	 * Converts the provided Java type of an RtflType object
	 * @param javaType The Java object to convert to an RtflType object
	 * @return The new RtflType corresponding to the provided Java type
	 * @since 1.0
	 */
	public static RtflType fromJavaType(Object javaType) {
		RtflType val = new NullType();
		
		if(javaType instanceof Boolean) {
			val = new BoolType((boolean) javaType);
		} else if(javaType instanceof Integer) {
			val = new IntType((int) javaType);
		} else if(javaType instanceof Double) {
			val = new DoubleType((double) javaType);
		} else if(javaType instanceof String) {
			val = new StringType((String) javaType);
		} else if(javaType instanceof Object[]) {
			ArrayList<RtflType> tmpArr = new ArrayList<RtflType>();
			
			for(Object obj : (Object[]) javaType)
				tmpArr.add(fromJavaType(obj));
			
			val = new ArrayType(tmpArr);
		} else if(javaType instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) javaType;
			HashMap<String, RtflType> rtflMap = new HashMap<String, RtflType>();
			
			for(Entry<String, Object> entry : map.entrySet())
				rtflMap.put(entry.getKey(), fromJavaType(entry.getValue()));
			
			val = new MapType(rtflMap);
		} else if(javaType != null) {
			// Wrap in JavaObjectWrapperType
			val = new JavaObjectWrapperType(javaType);
		}
		
		return val;
	}
	/**
	 * Converts the provided RtflType into its corresponding Java type
	 * @param rtflType The RtflType object to convert to a Java type
	 * @return The new Java type object corresponding to the provided RtflType
	 * @since 1.0
	 */
	@SuppressWarnings("unchecked")
	public static Object toJavaType(RtflType rtflType) {
		Object java = null;
		
		if(rtflType instanceof ArrayType) {
			ArrayList<RtflType> rtflArr = (ArrayList<RtflType>) rtflType.value();
			ArrayList<Object> arr = new ArrayList<Object>();
			
			for(RtflType rt : rtflArr)
				arr.add(toJavaType(rt));
			
			java = arr.toArray();
		} else if(rtflType instanceof MapType) {
			Map<String, RtflType> map = (Map<String, RtflType>) rtflType.value();
			HashMap<String, Object> javaMap = new HashMap<String, Object>();
			
			for(Entry<String, RtflType> entry : map.entrySet())
				javaMap.put(entry.getKey(), toJavaType(entry.getValue()));
			
			java = javaMap;
		} else {
			java = rtflType.value();
		}
		
		return java;
	}
}
