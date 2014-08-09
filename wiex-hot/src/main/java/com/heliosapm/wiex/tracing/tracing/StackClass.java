package com.heliosapm.wiex.tracing.tracing;

public class StackClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		A a = new A();
		a.doTrace();

		String s = Thread.currentThread().getStackTrace()[0].getClassName();
		System.out.println("I was called by " + s);
		
		stackTrace(Thread.currentThread().getStackTrace());
	}
	
	public static void stackTrace(StackTraceElement[] stack) {
		StringBuilder buff = new StringBuilder();
		//for(int i = stack.length-1; i > -1; i--) {
		for(int i = 0; i < stack.length; i++) {
//			for(int x = 0; x < i; x++) {
//				buff.append("\t");
//			}
			buff.append(stack[i].getClassName()).append("-->").append(stack[i].getMethodName());
			
			System.out.println(buff);
			buff.setLength(0);
		}
		System.out.println("======================");
		
	}

}

class A extends StackClass {
	
	public void doTrace() {
		report();
		B b = new B();
		b.doTrace();		
	}
	
	public void report() {
		String s = Thread.currentThread().getStackTrace()[4].getClassName();
		System.out.println("I am class " + this.getClass().getName() + " and I was called by " + s);
		stackTrace(Thread.currentThread().getStackTrace());
	}
}

class B extends A{
	
	public void doTrace() {
		report();
		C c = new C();
		c.doTrace();
	}
	
}

class C extends B {
	
	public void doTrace() {
		report();
	}
	
}
