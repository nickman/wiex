package com.heliosapm.wiex.server.collectors.util;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.TimeUnit;

public class MemoryCounterAgent {
  private static Instrumentation instrumentation;

  /** Initializes agent */
  public static void premain(String agentArgs,
                             Instrumentation instrumentation) {
    MemoryCounterAgent.instrumentation = instrumentation;
    log("instrumentation:" + instrumentation.getClass().getName());
    
  }
  
  public static void log(Object message) {
	  System.out.println(message);
  }

  /** Returns object size. */

  public static long sizeOf(Object obj) {
	  if (instrumentation == null) {		
		  throw new IllegalStateException("Instrumentation environment not initialised.");
	  }	  
	  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	  Thread.currentThread().setContextClassLoader(instrumentation.getClass().getClassLoader());
	  try {
		  if (isSharedFlyweight(obj)) {
			  return 0;
		  }
		  return instrumentation.getObjectSize(obj);
	  } finally {
		  Thread.currentThread().setContextClassLoader(classLoader);
	  }
  }

  /**
   * Returns deep size of object, recursively iterating over its
   * fields and superclasses.
   */

  public static long deepSizeOf(Object obj) {
    Map<Object, Object> visited = new IdentityHashMap<Object, Object>();
    
    Stack<Object> stack = new Stack<Object>();
    stack.push(obj);

    long result = 0;
    int objectCount = 0;
    do {
      result += internalSizeOf(stack.pop(), stack, visited);
      objectCount++;
    } while (!stack.isEmpty());
    System.out.println(obj.getClass().getName() + " Sub-Object Count:" + objectCount);
    return result;
  }

  /**
   * Returns true if this is a well-known shared flyweight.
   * For example, interned Strings, Booleans and Number objects.
   */

  private static boolean isSharedFlyweight(Object obj) {
    // optimization - all of our flyweights are Comparable
    if (obj instanceof Comparable) {
      if (obj instanceof Enum) {
        return true;
      } else if (obj instanceof String) {
        return (obj == ((String) obj).intern());
      } else if (obj instanceof Boolean) {
        return (obj == Boolean.TRUE || obj == Boolean.FALSE);
      } else if (obj instanceof Integer) {
        return (obj == Integer.valueOf((Integer) obj));
      } else if (obj instanceof Short) {
        return (obj == Short.valueOf((Short) obj));
      } else if (obj instanceof Byte) {
        return (obj == Byte.valueOf((Byte) obj));
      } else if (obj instanceof Long) {
        return (obj == Long.valueOf((Long) obj));
      } else if (obj instanceof Character) {
        return (obj == Character.valueOf((Character) obj));
      }
    }
    return false;
  }

  private static boolean skipObject(Object obj, Map visited) {
    return obj == null

        || visited.containsKey(obj)
        || isSharedFlyweight(obj);
  }

  private static long internalSizeOf(
      Object obj, Stack<Object> stack, Map<Object, Object> visited) {
    if (skipObject(obj, visited)) {
      return 0;
    }

    Class clazz = obj.getClass();
    if (clazz.isArray()) {
      addArrayElementsToStack(clazz, obj, stack);
    } else {
      // add all non-primitive fields to the stack
      while (clazz != null) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
          if (!Modifier.isStatic(field.getModifiers())
              && !field.getType().isPrimitive()) {
            field.setAccessible(true);
            try {
              stack.add(field.get(obj));
            } catch (IllegalAccessException ex) {
              throw new RuntimeException(ex);
            }
          }
        }
        clazz = clazz.getSuperclass();
      }
    }
    visited.put(obj, null);
    return sizeOf(obj);
  }

  private static void addArrayElementsToStack(
      Class clazz, Object obj, Stack<Object> stack) {
    if (!clazz.getComponentType().isPrimitive()) {
      int length = Array.getLength(obj);
      for (int i = 0; i < length; i++) {
        stack.add(Array.get(obj, i));
      }
    }
  }
  
  public static void main(String[] args) {
	  MemoryCounterAgentTest.measureSize(new Object());
	  MemoryCounterAgentTest.measureSize(new HashMap());
	  MemoryCounterAgentTest.measureSize(new LinkedHashMap());
	  MemoryCounterAgentTest.measureSize(new ReentrantReadWriteLock());
	  MemoryCounterAgentTest.measureSize(new byte[1000]);
	  MemoryCounterAgentTest.measureSize(new boolean[1000]);
	  MemoryCounterAgentTest.measureSize(new String("Hello World".toCharArray()));
	  MemoryCounterAgentTest.measureSize("Hello World");
	  MemoryCounterAgentTest.measureSize(10);
	  MemoryCounterAgentTest.measureSize(100);
	  MemoryCounterAgentTest.measureSize(1000);
	  long start = System.currentTimeMillis();
	  MemoryCounterAgentTest.measureSize(new ScheduledThreadPoolExecutor(1000));
	  MemoryCounterAgentTest.measureSize(new ThreadPoolExecutor(1000, 1000, 1000, TimeUnit.MILLISECONDS, new java.util.concurrent.ArrayBlockingQueue(100)));
	  long elapsed = System.currentTimeMillis()-start;
	  System.out.println("Elapsed:" + elapsed);
//	  for(int i = 0; i < 1000; i++) {
//		  log("SizeOf:" + i);
//		  MemoryCounterAgentTest.measureSize(i);
//	  }
	  MemoryCounterAgentTest.measureSize(Thread.State.TERMINATED);
	  }

  
}


class MemoryCounterAgentTest {
	  public static void measureSize(Object o) {
	    long memShallow = MemoryCounterAgent.sizeOf(o);
	    long memDeep = MemoryCounterAgent.deepSizeOf(o);
	    System.out.printf("%s, shallow=%d, deep=%d%n",
	        o.getClass().getSimpleName(),
	        memShallow, memDeep);
	  }
}


