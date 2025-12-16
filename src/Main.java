import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Main {

    public static void main(String[] args) throws Exception {
        // Example context switch
        Context.configType = 1;

        // First load of Config class
        ConfigLoader loader1 = new ConfigLoader("out/production/class-reload-demo"); // compile path
        Class<?> reloaded1 = loader1.loadClass("Config");

        // Copy static fields to original Config
        copyStaticFields(reloaded1, Config.class);
        System.out.println("After first reload: " + Config.configName);

        // Change context
        Context.configType = 2;

        // Reload in a new classloader
        ConfigLoader loader2 = new ConfigLoader("out/production/class-reload-demo");
        Class<?> reloaded2 = loader2.loadClass("Config");

        // Copy static fields again
        copyStaticFields(reloaded2, Config.class);
        System.out.println("After second reload: " + Config.configName);
    }

    /**
     * Copies all static fields (including final fields) from source to target class.
     * Handles Java 8–16 via modifiers hack and Java 17+ via getDeclaredFields0 workaround.
     */
    private static void copyStaticFields(Class<?> source, Class<?> target) throws Exception {
        int javaVersion = getJavaMajorVersion();

        for (Field f : source.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                Object value = f.get(null);

                Field targetField = target.getDeclaredField(f.getName());
                targetField.setAccessible(true);

                if (javaVersion > 8) {
                    setFinalStaticJava17(targetField, value);
                } else {
                    setFieldViaModifiers(targetField, value);
                }
            }
        }
    }

    /**
     * Modifiers hack for Java 8–16 (removes final flag and sets value)
     */
    private static void setFieldViaModifiers(Field field, Object value) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, value);
    }

    /**
     * Workaround for Java 17+ where 'modifiers' field is removed
     */
    private static void setFinalStaticJava17(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        // Access private Field fields via getDeclaredFields0
        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields0.setAccessible(true);
        Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);

        Field modifiersField = null;
        for (Field each : fields) {
            if ("modifiers".equals(each.getName())) {
                modifiersField = each;
                break;
            }
        }

        if (modifiersField == null) {
            throw new IllegalStateException("Cannot find 'modifiers' field in java.lang.reflect.Field");
        }

        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }

    public static int getJavaMajorVersion() {
        String version = System.getProperty("java.version"); // e.g., "1.8.0_341" or "17.0.6"
        if (version.startsWith("1.")) {
            // Java 8, 7, 6...
            return Integer.parseInt(version.substring(2, 3));
        } else {
            // Java 9+
            int dot = version.indexOf(".");
            if (dot != -1) {
                return Integer.parseInt(version.substring(0, dot));
            } else {
                return Integer.parseInt(version);
            }
        }
    }
}
