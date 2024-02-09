package magictheinjecting;

import java.io.File;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Set;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Vector;
import java.util.stream.Collectors;

public class MagicTheInjecting
extends Thread {
    public static byte[][] classes;

    private static Class tryGetClass(PrintWriter writer, ClassLoader cl, String... names) throws ClassNotFoundException {
    	ClassNotFoundException lastException = null;
    	for (String name : names) {
    		try {
    			return cl.loadClass(name);
    		} catch (ClassNotFoundException e) {
    			e.printStackTrace(writer);
    			lastException = e;
    		}
    	}
    	throw lastException;
    }

    @Override
    public void run() {
        try {
            PrintWriter writer = new PrintWriter(System.getProperty("user.home") + File.separator + "eloader-log.txt", "UTF-8");
            writer.println("Starting FFFFFFFFFFFFFF!");
            writer.flush();
            try {
                ClassLoader cl = null;
                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    ClassLoader threadLoader;
                    if (thread == null || thread.getContextClassLoader() == null || (threadLoader = thread.getContextClassLoader()).getClass() == null || threadLoader.getClass().getName() == null) continue;
                    String loaderName = threadLoader.getClass().getName();
                    writer.println("Thread: " + thread.getName() + " [" + loaderName + "]");
                    writer.flush();
                    //if (!thread.getName().contains("modloading-worker-11"))
                    //    continue;

                    if (!loaderName.contains("TransformingClassLoader") && !loaderName.contains("RelaunchClassLoader")) continue;
                    cl = threadLoader;

                    try{
                    Field f = ClassLoader.class.getDeclaredField("classes");
                    f.setAccessible(true);
                    ClassLoader classLoader = thread.getContextClassLoader();
                    Vector<Class> classes = (Vector<Class>) f.get(classLoader);
                    boolean found  = false;
                    for (int i = 0; i < classes.size(); i++) {
                        if(classes.get(i).getName().contains("ExampleMod"))
                            {
                                found = true;
                                break;
                            }

                        writer.println("CLASS " + classes.get(i));

                        Annotation[] ans = classes.get(i).getDeclaredAnnotations();
                        for (int j = 0; j < ans.length; j++) {
                            writer.println("ANNOTATION " + ans[j].toString());
                        }
                    }
                    if(found)
                        break;
                    }
                    catch(Exception ex)
                    {

                    }

                    break;
                }
                if (cl == null) {
                    throw new Exception("ClassLoader is null");
                }
                this.setContextClassLoader(cl);
                Class forgeEventHandlerAnnotation =  tryGetClass(writer, cl, "net.minecraftforge.eventbus.api.SubscribeEvent");
                Class modAnnotation = tryGetClass(writer, cl, "net.minecraftforge.fml.common.Mod");
                Class fmlInitializationEventClass = tryGetClass(writer, cl, "net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent", "net.minecraftforge.fml.common.event.FMLInitializationEvent");
                Class fmlPreInitializationEventClass = tryGetClass(writer, cl, "net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent", "net.minecraftforge.fml.common.event.FMLPreInitializationEvent");
                Method loadMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE, ProtectionDomain.class);
                loadMethod.setAccessible(true);
                writer.println("Loading " + classes.length + " classes");
                writer.flush();
                ArrayList<Object[]> mods = new ArrayList<>();
                for (byte[] classData : classes) {
                    if (classData == null) {
                        throw new Exception("classData is null");
                    }
                    if (cl.getClass() == null) {
                        throw new Exception("getClass() is null");
                    }
                    try {
                        Class tClass = (Class)loadMethod.invoke(cl, null, classData, 0, classData.length, cl.getClass().getProtectionDomain());

                        writer.println("Class: " + tClass.toString());
                        writer.flush();
                        Annotation[] ans = tClass.getDeclaredAnnotations();
                        for (int j = 0; j < ans.length; j++) {
                            writer.println("Annotation: " + ans[j].toString());
                            writer.flush();
                        }

                        if (tClass.getAnnotation(modAnnotation) == null) 
                        	continue;
                        Object[] mod = new Object[3];
                        mod[0] = tClass;
                        ArrayList<Method> fmlPreInitMethods = new ArrayList<Method>();
                        ArrayList<Method> fmlInitMethods = new ArrayList<Method>();
                        for (Method m : tClass.getDeclaredMethods()) {
                            writer.println("Method: " + m.toString());
                            writer.flush();
                            if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == fmlInitializationEventClass) {
                                writer.println("Method added: " + m.toString());
                                writer.flush();
	                            m.setAccessible(true);
	                            fmlInitMethods.add(m);
                        	}
                            if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == fmlPreInitializationEventClass) {
	                            m.setAccessible(true);
	                            fmlPreInitMethods.add(m);

                                 writer.println("Method added: " + m.toString());
                                writer.flush();
                        	}
                        }
                        mod[1] = fmlPreInitMethods;
                        mod[2] = fmlInitMethods;
                        mods.add(mod);
                        break;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Exception on defineClass", e);
                    }
                }
                writer.println(classes.length + " loaded successfully");
                writer.flush();
                for (Object[] mod : mods) {
                	Class modClass = (Class) mod[0];
                	ArrayList<Method> fmlPreInitMethods = (ArrayList<Method>) mod[1];
                	ArrayList<Method> fmlInitMethods = (ArrayList<Method>) mod[2];
                	Object modInstance = null;

					try {
                        writer.println("Instancing " + modClass.getName());
                        writer.flush();
                        modInstance = modClass.newInstance();
                        writer.println("Instanced");
                        writer.flush();
                    }
                    catch (Exception e) {
                        writer.println("Genexeption on instancing: " + e);
                        e.printStackTrace(writer);
                        writer.flush();
                        throw new Exception("Exception on instancing", e);
                    }

                    for (Method preInitMethod : fmlPreInitMethods) {
	                    try {
	                        writer.println("Preiniting " + preInitMethod);
	                        writer.flush();
	                        writer.println("Preinited");
	                        writer.flush();
	                        preInitMethod.invoke(modInstance, new Object[]{null});
	                    }
	                    catch (InvocationTargetException e) {
	                        writer.println("InvocationTargetException on preiniting: " + e);
	                        e.getCause().printStackTrace(writer);
	                        writer.flush();
	                        throw new Exception("Exception on preiniting (InvocationTargetException)", e.getCause());
	                    }
	                    catch (Exception e) {
	                        writer.println("Genexeption on preiniting: " + e);
	                        e.printStackTrace(writer);
	                        writer.flush();
	                        throw new Exception("Exception on preiniting", e);
	                    }
                	}

                	for (Method initMethod : fmlInitMethods) {
	                    try {
	                        writer.println("Initing " + initMethod);
	                        writer.flush();
	                        writer.println("Inited");
	                        writer.flush();
	                        initMethod.invoke(modInstance, new Object[]{null});
	                    }
	                    catch (InvocationTargetException e) {
	                        writer.println("InvocationTargetException on initing: " + e);
	                        e.getCause().printStackTrace(writer);
	                        writer.flush();
	                        throw new Exception("Exception on initing (InvocationTargetException)", e.getCause());
	                    }
	                    catch (Exception e) {
	                        writer.println("Genexeption on initing: " + e);
	                        e.printStackTrace(writer);
	                        writer.flush();
	                        throw new Exception("Exception on initing", e);
	                    }
                	}
                }
                writer.println("Successfully injected");
                writer.flush();
            }
            catch (Throwable e) {
                e.printStackTrace(writer);
                writer.flush();
            }
            writer.close();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static int injectCP(byte[][] classes) {
        try {
            MagicTheInjecting.classes = classes;
            MagicTheInjecting t = new MagicTheInjecting();
            t.start();
        }
        catch (Exception t) {
            // empty catch block
        }
        return 0;
    }

    public static byte[][] getByteArray(int size) {
        return new byte[size][];
    }
}
