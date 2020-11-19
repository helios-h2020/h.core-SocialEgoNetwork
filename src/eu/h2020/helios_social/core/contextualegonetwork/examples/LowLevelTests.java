package eu.h2020.helios_social.core.contextualegonetwork.examples;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class LowLevelTests {
	// THIS IS NOT AN EXAMPLE - USED AS A TEMP FILE FOR INTERNAL DEVELOPMENT
	/*public class TestClass {
		public void foo() {
			
		}
	}
	
	public static class TimingDynamicInvocationHandler implements InvocationHandler {
	    private final Map<String, Method> methods = new HashMap<>();
	    private Object target;
	    public TimingDynamicInvocationHandler(Object target) {
	        this.target = target;
	 
	        for(Method method: target.getClass().getDeclaredMethods()) {
	            this.methods.put(method.getName(), method);
	        }
	    }
	    @Override
	    public Object invoke(Object proxy, Method method, Object[] args) 
	      throws Throwable {
	        long start = System.nanoTime();
	        Object result = methods.get(method.getName()).invoke(target, args);
	        long elapsed = System.nanoTime() - start;
	 
	        System.out.println("Executing {} finished in {} ns"+method.getName()+ 
	          elapsed);
	 
	        return result;
	    }
	}
	
	@SuppressWarnings("unchecked")
    public static ContextualEgoNetworkData<T extends Object> T createWrappedInstance(Class<T> wrappedClass) {
        Object proxy = Proxy.newProxyInstance(wrappedClass.getClassLoader(), new Class[] {},  
        		new TimingDynamicInvocationHandler(new HashMap<>())
        );
        System.out.println(proxy.getClass());
        return (T)proxy;
    }

	public static void main(String[] args) {
		createWrappedInstance(TestClass.class).foo();
	}
	*/

}
