package org.jerkar.api.depmanagement;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jerkar.api.utils.JkUtilsIterable;

/**
 * Association between moduleIds and version.
 * 
 * @author Jerome Angibaud
 */
public final class JkVersionProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Returns <code>true</code> if this object provides no versions about any {@link JkModuleId}.
     */
    @SuppressWarnings("unchecked")
    public static JkVersionProvider empty() {
        return new JkVersionProvider(Collections.EMPTY_MAP);
    }

    /**
     * @see #of(JkModuleId, JkVersion)
     */
    public static JkVersionProvider of(String moduleId, String version) {
        return of(JkModuleId.of(moduleId), version);
    }

    /**
     * @see #of(JkModuleId, JkVersion)
     */
    public static JkVersionProvider of(JkModuleId moduleId, String version) {
        return of(moduleId, JkVersion.ofName(version));
    }

    /**
     * Creates a {@link JkVersionProvider} holding a single version providing.
     */
    public static JkVersionProvider of(JkModuleId moduleId, JkVersion version) {
        final Map<JkModuleId, JkVersion> result = JkUtilsIterable.mapOf(moduleId, version);
        return new JkVersionProvider(result);
    }

    /**
     * Creates an empty version provider.
     */
    public static JkVersionProvider of() {
        return new JkVersionProvider(new HashMap<JkModuleId, JkVersion>());
    }

    /**
     * Creates a version provider from the specified versioned modules.
     */
    public static JkVersionProvider of(Iterable<JkVersionedModule> modules) {
        final Map<JkModuleId, JkVersion> result = new HashMap<JkModuleId, JkVersion>();
        for (final JkVersionedModule module : modules) {
            result.put(module.moduleId(), module.version());
        }
        return new JkVersionProvider(result);
    }

    /**
     * Creates a version provider containing versions for all in specified version providers.
     */
    public static JkVersionProvider mergeOf(Iterable<JkVersionProvider> versionProviders) {
        final Map<JkModuleId, JkVersion> result = new HashMap<JkModuleId, JkVersion>();
        for (final JkVersionProvider versionProvider : versionProviders) {
            result.putAll(versionProvider.map);
        }
        return new JkVersionProvider(result);
    }

    private final Map<JkModuleId, JkVersion> map;

    private JkVersionProvider(Map<JkModuleId, JkVersion> map) {
        super();
        this.map = map;
    }

    /**
     * Returns the version to use with specified module.
     */
    public JkVersion versionOf(JkModuleId moduleId) {
        return this.map.get(moduleId);
    }

    /**
     * Returns <code>true</code> if this providers is empty.
     */
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    /**
     * Returns a {@link JkVersionProvider} that is a union of this provider and the specified one.
     */
    public JkVersionProvider and(JkVersionProvider other) {
        final Map<JkModuleId, JkVersion> newMap = new HashMap<JkModuleId, JkVersion>(this.map);
        newMap.putAll(other.map);
        return new JkVersionProvider(newMap);
    }

    /**
     * Returns a {@link JkVersionProvider} that is the union of this provider and the specified one.
     */
    public JkVersionProvider and(JkModuleId moduleId, JkVersion version) {
        final Map<JkModuleId, JkVersion> newMap = new HashMap<JkModuleId, JkVersion>(this.map);
        newMap.put(moduleId, version);
        return new JkVersionProvider(newMap);
    }

    /**
     * @see JkVersionProvider#and(JkModuleId, JkVersion)
     */
    public JkVersionProvider and(JkModuleId moduleId, String version) {
        return and(moduleId, JkVersion.ofName(version));
    }

    /**
     * @see JkVersionProvider#and(JkModuleId, JkVersion)
     */
    public JkVersionProvider and(String moduleId, String version) {
        return and(JkModuleId.of(moduleId), version);
    }

    /**
     * @see JkVersionProvider#and(JkModuleId, JkVersion)
     */
    public JkVersionProvider and(String group, String name, String version) {
        return and(JkModuleId.of(group, name), version);
    }

    /**
     * Returns all modules that this object provides version for.
     */
    public Set<JkModuleId> moduleIds() {
        return map.keySet();
    }

}
