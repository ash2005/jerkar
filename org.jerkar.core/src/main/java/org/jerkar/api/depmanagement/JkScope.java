package org.jerkar.api.depmanagement;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jerkar.api.utils.JkUtilsIterable;
import org.jerkar.api.utils.JkUtilsString;

/**
 * Defines a context where is defined dependencies of a given project. According
 * we need to compile, test or run the application, the dependencies may
 * diverge. For example, <code>Junit</code> library may only be necessary for
 * testing, so we can declare that
 * <code>Junit</scope> is only necessary for scope <code>TEST</code>.<br/>
 * 
 * Similar to Maven <code>scope</code> or Ivy <code>configuration</code>.
 * 
 * @author Jerome Angibaud
 */
public class JkScope implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link JkScope} passing its name.
     */
    @SuppressWarnings("unchecked")
    public static JkOptionableScope of(String name) {
        return new JkOptionableScope(name, Collections.EMPTY_SET, "", true);
    }

    private final Set<JkScope> extendedScopes;

    private final String name;

    private final String description;

    private final boolean transitive;

    private JkScope(String name, Set<JkScope> extendedScopes, String description,
            boolean transitive) {
        super();
        final String illegal = JkUtilsString.firstMatching(name, ",", "->");
        if (illegal != null) {
            throw new IllegalArgumentException("Scope name can't contain '" + illegal + "'");
        }
        this.extendedScopes = Collections.unmodifiableSet(extendedScopes);
        this.name = name;
        this.description = description;
        this.transitive = transitive;
    }

    /**
     * Returns the name of this scope. Name is used as identifier for scopes.
     */
    public String name() {
        return name;
    }

    /**
     * Human description for the purpose of this scope, can be <code>null</code>.
     */
    public String description() {
        return description;
    }

    /**
     * Scopes that are extended by this one.
     * 
     */
    public Set<JkScope> extendedScopes() {
        return this.extendedScopes;
    }

    /**
     * Returns <code>true</code> if the dependencies defined with this scope should be resolved recursively
     * (meaning returning the dependencies of the dependencies and so on)
     */
    public boolean transitive() {
        return this.transitive;
    }

    /**
     * Returns scopes this scope inherits from. It returns recursively parent scopes, parent of parent scopes
     * and so on.
     */
    public List<JkScope> ancestorScopes() {
        final List<JkScope> list = new LinkedList<JkScope>();
        list.add(this);
        for (final JkScope scope : this.extendedScopes) {
            for (final JkScope jkScope : scope.ancestorScopes()) {
                if (!list.contains(jkScope)) {
                    list.add(jkScope);
                }
            }
        }
        return list;
    }

    /**
     * Returns <code>true</code> if this scope extends the specified one.
     */
    public boolean isExtending(JkScope jkScope) {
        if (extendedScopes == null || extendedScopes.isEmpty()) {
            return false;
        }
        for (final JkScope parent : extendedScopes) {
            if (parent.equals(jkScope) || parent.isExtending(jkScope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a {@link JkScopeMapping} from this {@link JkScope} to the specified one.
     */
    public JkScopeMapping mapTo(JkScope targetScope) {
        return JkScopeMapping.of(this).to(targetScope);
    }

    /**
     * Returns a {@link JkScopeMapping} from this {@link JkScope} to the specified one.
     */
    public JkScopeMapping mapTo(String targetScope) {
        return JkScopeMapping.of(this).to(JkScope.of(targetScope));
    }

    /**
     * Returns <code>true</code> if this scope is one or is extending any of the specified scopes.
     */
    public boolean isInOrIsExtendingAnyOf(Iterable<? extends JkScope> scopes) {
        for (final JkScope scope : scopes) {
            if (scope.equals(this) || this.isExtending(scope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see #isInOrIsExtendingAnyOf(Iterable)
     */
    public boolean isInOrIsExtendingAnyOf(JkScope... scopes) {
        return isInOrIsExtendingAnyOf(Arrays.asList(scopes));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JkScope other = (JkScope) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * returns all specified scopes and all of their ancestors.
     */
    public static Set<JkScope> involvedScopes(Iterable<JkScope> scopes) {
        final Set<JkScope> result = JkUtilsIterable.setOf(scopes);
        for (final JkScope jkScope : scopes) {
            result.addAll(jkScope.ancestorScopes());
        }
        return result;
    }

    /**
     * A {@link JkScope} allowing to define other scope from it. It exists only
     * to serve the fluent API purpose as for clarity we can't create derived
     * <code>scopes</scope> directly from a {@link JkScope} .<br/>
     * Use the {@link #descr(String)} method last as it returns a
     * {@link JkScope}.
     * 
     * @author Jerome Angibaud
     */
    public static class JkOptionableScope extends JkScope {

        private static final long serialVersionUID = 1L;

        private JkOptionableScope(String name, Set<JkScope> extendedScopes, String descr,
                boolean transitive) {
            super(name, extendedScopes, descr, transitive);
        }

        /**
         * Returns a {@link JkOptionableScope} identical to this one but extending the specified scopes.
         */
        public JkOptionableScope extending(JkScope... scopes) {
            return new JkOptionableScope(name(), new HashSet<JkScope>(Arrays.asList(scopes)),
                    description(), transitive());
        }

        /**
         * Returns a {@link JkOptionableScope} identical to this one with the specified transitivity.
         */
        public JkOptionableScope transitive(boolean transitive) {
            return new JkOptionableScope(name(), extendedScopes(), description(), transitive);
        }

        /**
         * Returns a {@link JkOptionableScope} identical to this one with the specified description.
         */
        public JkScope descr(String description) {
            return new JkScope(name(), extendedScopes(), description, transitive());
        }

    }

}
