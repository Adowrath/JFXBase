package adowrath.fx.model.localization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;


import adowrath.fx.model.Model;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * The Translator, saving the translation items.
 * <br>
 * First thing you do should be calling the
 * {@link #init(String, Class)}
 * method, or else your translation will not work.
 * <br>
 * Do this before any FXML file gets loaded with its
 * {@code controller}.
 */
@NonNullByDefault
public final class Translator {
	
	
	/**
	 * The map that is the current language
	 */
	private static final Map<String, String> transMap = new HashMap<>();
	
	/**
	 * The map used as a fallback when no translation was found in the
	 * currently selected language file, based upon en_US.
	 */
	private static final Map<String, String> fallBack = new HashMap<>();
	
	/**
	 * The current locale.
	 */
	private static Locale currentLocale = Model.getLocale();
	
	/**
	 * The project name.
	 */
	private static @Nullable String project = null;
	
	/**
	 * The class of the project using this class.
	 */
	private static @Nullable Class<?> projectClass = null;
	
	/**
	 * All registered locales, indexed by the base language and then
	 * giving a set of all countries, like en_US and en_GB
	 */
	public static final Map<String, Set<Locale>> locales = new HashMap<>();
	
	/**
	 * Loads all the .lang files in the specified directory
	 */
	@SuppressWarnings("null")
	private static void loadLocales() {
		locales.clear();
		Stream<Path> files = null;
		try {
			files = Files
					.list(Paths.get(getLangURI(currentLocale)).getParent());
			files.forEach(path -> {
				String locale = path.getFileName().toString();
				if(locale.endsWith(".lang")) {
					locale = locale.substring(0, locale.length() - 5);
				} else
					return;
				String[] parts = locale.split("_");
				String lang = parts[0];
				Locale nLoc = getLocale(parts);
				
				locales.putIfAbsent(lang, new HashSet<>());
				locales.get(lang).add(nLoc);
			});
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			if(files != null) {
				files.close();
			}
		}
	}
	
	/**
	 * @param parts
	 *        the parts of the language, only up to 3 parts.
	 * @return
	 * 		the locale that is defined by this file name
	 */
	public static Locale getLocale(String[] parts) {
		Locale nLoc = null;
		switch(parts.length) {
			case 1:
				nLoc = new Locale(parts[0]);
				break;
			case 2:
				nLoc = new Locale(parts[0], parts[1]);
				break;
			case 3:
				nLoc = new Locale(parts[0], parts[1], parts[2]);
				break;
			case 0:
				throw new IllegalArgumentException("Empty Locale-Array given.");
			default:
				throw new IllegalArgumentException(String.join("_", parts)
						+ " is not a valid lang-file!");
		}
		return nLoc;
	}
	
	/**
	 * @param projectName
	 *        the name of this project. The language files should be
	 *        placed in /lang/{@code projectName}
	 * @param projectClz
	 *        the class is needed so that the language files can be
	 *        detected when this is used as an external jar file
	 */
	public static void init(String projectName, Class<?> projectClz) {
		project = projectName;
		projectClass = projectClz;
		
		loadLocales();
		load(fallBack, Locale.US);
		if(!Locale.US.equals(currentLocale)) {
			load(transMap, currentLocale);
		}
		
		Model.addLocaleListener(Translator::changed);
	}
	
	/**
	 * @param observable
	 *        the holder for the locale
	 * @param oldValue
	 *        the old locale, unused here
	 * @param newValue
	 *        the newly selected
	 */
	private static void changed(@Nullable ObservableValue<? extends Locale> observable,
								Locale oldValue,
								Locale newValue) {
		if(!loadLocale(newValue)) {
			Alert a = new Alert(AlertType.ERROR,
								"There was an error in loading the language file for "
										+ newValue + "!");
			a.showAndWait();
		}
	}
	
	/**
	 * @param key
	 *        the language key that should be used
	 * @return
	 * 		the translation, or, if it was found neither in the
	 *         current language nor in the fallBack
	 */
	@SuppressWarnings("null")
	public static String translate(String key) {
		String val;
		return ((val = fallBack.get(key)) != null)
				|| ((val = transMap.get(key)) != null) ? val : key;
	}
	
	/**
	 * @param loc
	 *        loads a new locale.
	 * @return
	 * 		true if there was no fatal problem with the loading
	 */
	private static boolean loadLocale(Locale loc) {
		if(!currentLocale.equals(loc)) {
			currentLocale = loc;
			transMap.clear();
			return load(transMap, currentLocale);
		}
		return true;
	}
	
	/**
	 * Loads the languages defined by the Locale and stores the
	 * translations in the map.
	 * <br>
	 * If the locale does not have a corresponding file (which should
	 * not happen, but it could have been deleted), this method prints
	 * an error and returns silently with a false
	 * 
	 * @param map
	 *        the map to store the translations in
	 * @param loc
	 *        the required locale
	 * @return
	 * 		true if it succeded, false if the file was not found or
	 *         there was an error
	 */
	private static boolean load(Map<String, String> map, Locale loc) {
		Logger log = Logger.getLogger("Translator-" + loc);
		BufferedReader br = null;
		try {
			URI uri = getLangURI(loc);
			if(uri == null) {
				System.err.println("The " + loc + " file has been deleted!");
				return false;
			}
			br = Files.newBufferedReader(	Paths.get(uri),
											Charset.forName("UTF-8"));
			
			br.lines().forEach((String line) -> {
				if(!(line.startsWith("#") || line.startsWith("//"))) {
					int i = line.indexOf('=');
					int c = Math.min(line.indexOf('#'), line.indexOf("//"));
					if(i >= 0) {
						String key = line.substring(0, i).trim(), value = line
								.substring(i + 1, c >= 0 ? c : line.length())
								.trim();
						
						if(map.containsKey(key)) {
							log.log(Level.WARNING,
									"Found duplicate entry at " + key);
						} else {
							map.put(key, value);
						}
					}
				}
			});
			
		} catch(IOException | UncheckedIOException e) {
			log.log(Level.SEVERE, "Failed to load language file for \"" + loc
					+ "\", falling back to en_US!", e);
			return false;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(IOException e) {
					
				}
			}
		}
		return true;
	}
	
	/**
	 * @param loc
	 *        The locale to find in the folder
	 * @return
	 * 		null if the file does not exist, else the uri to the
	 *         file
	 */
	@SuppressWarnings("null")
	private static @Nullable URI getLangURI(Locale loc) {
		StringBuilder fileString = new StringBuilder();
		fileString.append("/lang/");
		if(project != null) {
			fileString.append(project + "/");
		}
		fileString.append(loc.toString());
		fileString.append(".lang");
		
		try {
			return projectClass.getResource(fileString.toString()).toURI();
		} catch(URISyntaxException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
