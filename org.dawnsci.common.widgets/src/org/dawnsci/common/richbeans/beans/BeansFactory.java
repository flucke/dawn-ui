/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.beans;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds a list of the IRichBeans used on the beamline. Has a series of utilities for interacting with the beans and the
 * xml files in which they are persisted.
 * <p>
 * The list of classes is either populated through a Spring instantiated instance of this class (server-side) or via the
 * uk.ac.common.beans.factory extension point (RCP client-side).
 */
public class BeansFactory {

	private static final Logger logger = LoggerFactory.getLogger(BeansFactory.class);

	private static Class<? extends Object>[] CLASSES;

	/**
	 * Check that CLASSES is init and throw an exception if it hasn't been yet. Using CLASSES in this file would
	 * generally through a {@link NullPointerException} anyway, this is just a more informative check that is less
	 * likely to be caught by over aggressive try/catchs that exist in this file.
	 * 
	 * @throws NullPointerException
	 *             thrown if CLASSES has not been initialised
	 */
	private static void checkInit() throws NullPointerException {
		if (CLASSES == null)
			throw new NullPointerException(
					"BeansFactory.CLASSES is null, therefore BeansFactory has not been initialized properly");
	}



	/**
	 * Can inject classes from spring or with static method. The classes are needed to define which XML files have GDA
	 * bean files with them.
	 */
	public BeansFactory() {
	}

	/**
	 * Fast way to determine if XML file is a bean in the system which can be read by castor and control things on the
	 * server.
	 * 
	 * @param beanFile
	 * @return true if bean file.
	 * @throws Exception
	 */
	public static boolean isBean(File beanFile) throws Exception {
		checkInit();
		for (int i = 0; i < CLASSES.length; i++) {
			if (BeansFactory.isBean(beanFile, CLASSES[i]))
				return true;
		}
		return false;
	}



	/**
	 * Find the first filename of an xml file persisting a bean of the given class
	 * 
	 * @param folder
	 * @param clazz
	 * @return String - filename
	 * @throws Exception
	 */
	public static String getFirstFileName(File folder, Class<? extends Object> clazz) throws Exception {
		final File[] fa = folder.listFiles();
		for (int i = 0; i < fa.length; i++) {
			if (BeansFactory.isBean(fa[i], clazz)) {
				return fa[i].getName();
			}
		}
		return null;
	}

	/**
	 * Checks if the XML file is a Castor persistence file of a bean in the given list
	 * 
	 * @param beanFile
	 * @return true if file is a saved version of this bean
	 * @throws Exception
	 */
	public static boolean isBean(final File beanFile, final Class<? extends Object> beanClass) throws Exception {
		final BufferedReader reader = new BufferedReader(new FileReader(beanFile));
		return isBean(reader, beanClass);
	}

	public static boolean isBean(final InputStream bean, final Class<? extends Object> beanClass) throws Exception {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(bean, "UTF-8"));
		return isBean(reader, beanClass);
	}

	private static boolean isBean(BufferedReader reader, Class<? extends Object> beanClass) throws Exception {
		try {
			@SuppressWarnings("unused")
			final String titleLine = reader.readLine(); // unused.
			final String tagLine = reader.readLine();
			if (tagLine == null)
				return false;
			final String tagName = beanClass.getName().substring(beanClass.getName().lastIndexOf(".") + 1);
			if (tagName == null)
				return false;
			if (tagLine.trim().equalsIgnoreCase("<" + tagName + ">")
					|| tagLine.trim().equalsIgnoreCase("<" + tagName + "/>")) {
				return true;
			}
			return false;
		} finally {
			reader.close();
		}
	}


	public static Class<? extends Object>[] getClasses() {
		checkInit();

		return CLASSES;
	}

	public static void setClasses(Class<? extends Object>[] cLASSES) {
		if (cLASSES == null)
			throw new NullPointerException("cLASSES must be-non null to initialzied BeansFactory");
		CLASSES = cLASSES;
	}

	private List<String> classList;

	public List<String> getClassList() {
		return classList;
	}

	public void setClassList(List<String> classList) {
		this.classList = classList;

		int i = 0;
		final Class<? extends Object>[] classes = (Class<? extends Object>[]) new Class<?>[classList.size()];
		for (String clazz : classList) {
			try {
				classes[i] = (Class<? extends Object>) Class.forName(clazz);
			} catch (ClassNotFoundException e) {
				logger.info("ClassNotFoundException, cannot load class " + clazz);
			}
			++i;
		}
		BeansFactory.setClasses(classes);
	}

	/**
	 * Changes a value on the given bean using reflection
	 * 
	 * @param bean
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	public static void setBeanValue(final Object bean, final String fieldName, final Object value) throws Exception {
		final String setterName = getSetterName(fieldName);
		
		// Faster to deal with primitives within an exception
		boolean tryDirectValue = true;
		try {
			if (value != null) {
				if (value instanceof Number) {
		
					Number number = (Number)value;
					final Method method = bean.getClass().getMethod(setterName, getPrimitiveClass(number));
					method.invoke(bean, value); // auto-boxing should sort this
					return;
					
				} if (value instanceof Boolean) {
					final Method method = bean.getClass().getMethod(setterName, boolean.class);
					method.invoke(bean, ((Boolean)value).booleanValue());
					return;
				}
			}
			
			tryDirectValue = false;
			Method method=null;
			if (value != null) {
				try {
				    method = bean.getClass().getMethod(setterName, value.getClass());
				} catch (NoSuchMethodException ne) {
					
					// Look for interfaces.
					final Class[] interfaces = value.getClass().getInterfaces();
					if (interfaces!=null) for (Class class1 : interfaces) {
						try {
							method = bean.getClass().getMethod(setterName, class1);
						} catch (NoSuchMethodException neOther) {
							continue;
						}
						if (method!=null) break;
					}
					if (method==null) {
						method = bean.getClass().getMethod(setterName, Object.class);
					}
				}
				
			} else {
				method = bean.getClass().getMethod(setterName, Object.class);
			}
			if (method!=null) method.invoke(bean, value);

		} catch (NoSuchMethodException ne) {
			
			// Happens when they implemented with the object not the primitive value.
			if (tryDirectValue) {
				final Method method;
				if (value != null) {
					method = bean.getClass().getMethod(setterName, value.getClass());
				} else {
					method = bean.getClass().getMethod(setterName, Object.class);
				}
				method.invoke(bean, value);
			}
		}
	}

	/**
	 * There is a better way to do this but cannot find it again
	 *  // TODO Use factory method for getting primitive type
	 * @param number
	 * @return
	 */
	private static Class<?> getPrimitiveClass(Number number) {
		if (number instanceof Double) {
			return double.class;
			
		} else if (number instanceof Float) {
			return float.class;

		} else if (number instanceof Integer) {
			return int.class;
			
		} else if (number instanceof Long) {
			return long.class;
			
		} else if (number instanceof Short) {
			return short.class;

		} else if (number instanceof Byte) {
			return byte.class;
		}
		
		return null;
	}


	/**
	 * Method gets value out of bean using reflection.
	 * 
	 * @param bean
	 * @param fieldName
	 * @return value
	 * @throws Exception
	 */
	public static Object getBeanValue(final Object bean, final String fieldName) throws Exception {
		final String getterName = getGetterName(fieldName);
		final Method method = bean.getClass().getMethod(getterName);
		return method.invoke(bean);
	}

	public static String getFieldWithUpperCaseFirstLetter(final String fieldName) {
		return fieldName.substring(0, 1).toUpperCase(Locale.US) + fieldName.substring(1);
	}

	private static String getName(final String prefix, final String fieldName) {
		return prefix + BeansFactory.getFieldWithUpperCaseFirstLetter(fieldName);
	}

	/**
	 * There must be a smarter way of doing this i.e. a JDK method I cannot find. However it is one line of Java so
	 * after spending some time looking have coded self.
	 * 
	 * @param fieldName
	 * @return String
	 */
	public static String getSetterName(final String fieldName) {
		if (fieldName == null) return null;
		return BeansFactory.getName("set", fieldName);
	}

	/**
	 * There must be a smarter way of doing this i.e. a JDK method I cannot find. However it is one line of Java so
	 * after spending some time looking have coded self.
	 * 
	 * @param fieldName
	 * @return String
	 */
	public static String getGetterName(final String fieldName) {
		if (fieldName == null)
			return null;
		return BeansFactory.getName("get", fieldName);
	}
	
	public static String getIsserName(final String fieldName) {
		if (fieldName == null)
			return null;
		return BeansFactory.getName("is", fieldName);
	}

	/**
	 * Can be used to test if a given class is a class defined by the extension point.
	 * 
	 * @param clazz
	 * @return true if class.
	 */
	public static boolean isClass(Class<? extends Object> clazz) {
		checkInit();

		for (int i = 0; i < CLASSES.length; i++) {
			if (CLASSES[i].equals(clazz))
				return true;
		}
		return false;
	}

	/**
	 * Deep copy using serialization. All objects in the graph must serialize to use this method or an exception will be
	 * thrown.
	 * 
	 * @param fromBean
	 * @return deeply cloned bean
	 */
	public static <T> T deepClone(final T fromBean) throws Exception {
		return deepClone(fromBean, fromBean.getClass().getClassLoader());
	}

	/**
	 * Creates a clone of any serializable object. Collections and arrays may be cloned if the entries are serializable.
	 * Caution super class members are not cloned if a super class is not serializable.
	 */
	public static <T> T deepClone(T toClone, final ClassLoader classLoader) throws Exception {
		if (null == toClone)
			return null;

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		ObjectOutputStream oOut = new ObjectOutputStream(bOut);
		oOut.writeObject(toClone);
		oOut.close();
		ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
		bOut.close();
		ObjectInputStream oIn = new ObjectInputStream(bIn) {
			/**
			 * What we are saying with this is that either the class loader or any of the beans added using extension
			 * points classloaders should be able to find the class.
			 */
			@Override
			protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
				try {
					return Class.forName(desc.getName(), false, classLoader);
				} catch (Exception ne) {
					for (int i = 0; i < CLASSES.length; i++) {
						try {
							return CLASSES[i].getClassLoader().loadClass(desc.getName());
						} catch (Exception e) {
							continue;
						}
					}
				}
				return null;
			}
		};
		bIn.close();
		// the whole idea is to create a clone, therefore the readObject must
		// be the same type in the toClone, hence of T
		@SuppressWarnings("unchecked")
		T copy = (T) oIn.readObject();
		oIn.close();

		return copy;
	}

}
