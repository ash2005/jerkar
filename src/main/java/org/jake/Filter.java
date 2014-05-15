package org.jake;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.jake.utils.AntPatternUtils;
import org.jake.utils.FileUtils;

public abstract class Filter {
	
	public static final Filter ACCEPT_ALL = new Filter() {

		@Override
		public boolean accept(String relativePath) {
			return true;
		}
		
	};
	
	public abstract boolean accept(String relativePath);
	
	public static Filter include(String ... antPatterns) {
		return new IncludeFilter(antPatterns);
	}
	
	public static Filter exclude(String ... antPatterns) {
		return new ExcludeFilter(antPatterns);
	}
		
	public Filter andIncludeOnly(String ... antPatterns) {
		return this.and(include(antPatterns));
	}
	
	public Filter andExcludeAll(String ... antPatterns) {
		return this.and(exclude(antPatterns));
	}
	
	public Filter and(Filter other) {
		return and(this, other);
	}
	
	public Filter or(Filter other) {
		return or(this, other);
	}
	
	public Filter reverse() {
		return new Filter() {

			@Override
			public boolean accept(String relativePath) {
				return !Filter.this.accept(relativePath);
			}
			
		};
	}
	
	public FileFilter toFileFilter(final File baseDir) {
		return new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				final String relativePath = FileUtils.getRelativePath(baseDir, file);
				return Filter.this.accept(relativePath);
			}
		};
	}
	
	
	private static class IncludeFilter extends Filter {
		
		private final String[] antPatterns;
		
		public IncludeFilter(String[] antPatterns) {
			super();
			this.antPatterns = antPatterns;
		}

		@Override
		public boolean accept(String relativePath) {
			
			for (final String antPattern : antPatterns) {
				
				boolean match = AntPatternUtils.doMatch(antPattern, relativePath);
				if (match) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "includes" + Arrays.toString(antPatterns);
		}
	} 
	
	private static class ExcludeFilter extends Filter {
		
		private final String[] antPatterns;
		
		public ExcludeFilter(String[] antPatterns) {
			super();
			this.antPatterns = antPatterns;
		}

		@Override
		public boolean accept(String relativePath) {
			
			for (final String antPattern : antPatterns) {
				
				boolean match = !AntPatternUtils.doMatch(antPattern, relativePath);
				if (match) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "excludes" + Arrays.toString(antPatterns);
		}
	} 
	
	private static Filter and(final Filter filter1, final Filter filter2) {
		
		return new Filter() {

			@Override
			public boolean accept(String candidate) {
				return filter1.accept(candidate) && filter2.accept(candidate);
			}

			@Override
			public String toString() {
				return "{" + filter1 + " & " + filter2 + "}";
			}
		};
	}
	
	private static Filter or(final Filter filter1, final Filter filter2) {
		
		return new Filter() {

			@Override
			public boolean accept(String candidate) {
				return filter1.accept(candidate) || filter2.accept(candidate);
			}

			@Override
			public String toString() {
				return "{" + filter1 + " | " + filter2 + "}";
			}
		};
	}

	
}