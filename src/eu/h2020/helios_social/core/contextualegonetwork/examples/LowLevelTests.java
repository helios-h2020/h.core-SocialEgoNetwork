package eu.h2020.helios_social.core.contextualegonetwork.examples;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LowLevelTests {
	// THIS IS NOT AN EXAMPLE - USED AS A TEMP FILE FOR INTERNAL DEVELOPMENT
	public class TestClass {
		public void foo() {
			
		}
	}
	
	@SuppressWarnings("unchecked")
    public static <T extends Object> T createWrappedInstance(Class<T> wrappedClass, Class ...interfaces) {
        Object proxy = Proxy.newProxyInstance(wrappedClass.getClassLoader(), interfaces, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            	System.out.println(proxy.getClass().getName());
                Method m;
                try {
                    m = wrappedClass.getMethod(method.getName(), method.getParameterTypes());
                    m.setAccessible(true);
                }
                catch (Exception e) {
                    throw new UnsupportedOperationException(method.toString(), e);
                }
                try {
                	System.out.println("called function "+method.getName());
                    return m.invoke(this, args);
                }
                catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        });
        System.out.println(proxy.getClass());
        return (T)proxy;
    }
	

	public static void main(String[] args) {
		createWrappedInstance(TestClass.class).foo();
	}

}
