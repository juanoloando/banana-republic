package plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract base class untuk semua plugin loader.
 * Menggunakan Template Method pattern: subclass mendefinisikan
 * tipe plugin dan cara validasinya, base class menangani
 * mekanisme loading via URLClassLoader + Reflection.
 *
 * Design patterns: Template Method, Strategy (tiap subclass = strategi load berbeda)
 */
public abstract class PluginLoader<T> {

    private static final Logger LOGGER = Logger.getLogger(PluginLoader.class.getName());

    /**
     * Load satu kelas plugin dari JAR file.
     * Template method: subclass implementasi validatePlugin() dan getPluginInterface().
     *
     * @param jarFile file JAR yang akan diload
     * @param className fully-qualified nama kelas di dalam JAR
     * @return instance plugin yang sudah divalidasi
     * @throws PluginLoadException jika gagal load atau tidak valid
     */
    public final T loadPlugin(File jarFile, String className) throws PluginLoadException {
        assert jarFile != null : "JAR file must not be null";
        assert className != null && !className.isBlank() : "Class name must not be blank";
        assert jarFile.exists() : "JAR file does not exist: " + jarFile.getAbsolutePath();
        assert jarFile.getName().endsWith(".jar") : "File must be a .jar: " + jarFile.getName();

        try {
            URL jarUrl = jarFile.toURI().toURL();
            URLClassLoader loader = new URLClassLoader(
                new URL[]{jarUrl},
                Thread.currentThread().getContextClassLoader()
            );
            Class<?> rawClass = Class.forName(className, true, loader);
            validatePlugin(rawClass);
            @SuppressWarnings("unchecked")
            T instance = (T) rawClass.getDeclaredConstructor().newInstance();
            LOGGER.info("Plugin loaded: " + className + " from " + jarFile.getName());
            return instance;
        } catch (PluginLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new PluginLoadException(
                "Failed to load plugin class '" + className + "' from " + jarFile.getName(), e
            );
        }
    }

    /**
     * Scan semua kelas di dalam JAR yang mengimplementasikan interface plugin yang dimaksud.
     * Berguna untuk auto-discovery tanpa perlu tahu nama kelas.
     *
     * @param jarFile file JAR yang di-scan
     * @return list plugin yang berhasil diinstansiasi
     */
    public final List<T> loadAllFromJar(File jarFile) throws PluginLoadException {
        assert jarFile != null : "JAR file must not be null";
        assert jarFile.exists() : "JAR file does not exist: " + jarFile.getAbsolutePath();

        List<T> results = new ArrayList<>();
        try {
            URL jarUrl = jarFile.toURI().toURL();
            URLClassLoader loader = new URLClassLoader(
                new URL[]{jarUrl},
                Thread.currentThread().getContextClassLoader()
            );
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
                jar.stream()
                    .filter(e -> e.getName().endsWith(".class"))
                    .map(e -> e.getName().replace('/', '.').replace(".class", ""))
                    .forEach(cn -> {
                        try {
                            Class<?> cls = Class.forName(cn, true, loader);
                            if (getPluginInterface().isAssignableFrom(cls)
                                && !cls.isInterface()
                                && !java.lang.reflect.Modifier.isAbstract(cls.getModifiers())) {
                                @SuppressWarnings("unchecked")
                                T instance = (T) cls.getDeclaredConstructor().newInstance();
                                results.add(instance);
                                LOGGER.info("Auto-discovered plugin: " + cn);
                            }
                        } catch (Exception ignored) {
                            // kelas yang tidak bisa diinstansiasi diabaikan
                        }
                    });
            }
        } catch (Exception e) {
            throw new PluginLoadException("Failed to scan JAR: " + jarFile.getName(), e);
        }
        return results;
    }

    /**
     * Validasi bahwa kelas yang diload mengimplementasikan interface yang benar.
     * Subclass bisa override untuk tambahkan validasi spesifik.
     */
    protected void validatePlugin(Class<?> clazz) throws PluginLoadException {
        if (!getPluginInterface().isAssignableFrom(clazz)) {
            throw new PluginLoadException(
                "Class '" + clazz.getName() + "' does not implement "
                + getPluginInterface().getSimpleName()
            );
        }
    }

    /**
     * Hook method: subclass mendefinisikan interface yang harus diimplementasikan plugin.
     */
    protected abstract Class<T> getPluginInterface();
}
