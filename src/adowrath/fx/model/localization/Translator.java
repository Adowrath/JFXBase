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

@NonNullByDefault
public final class Translator {
	
	private static Map<String, String> transMap = new HashMap<>();
	private static Map<String, String> fallBack = new HashMap<>();
	private static Locale currentLocale = Model.getLocale();
	@Nullable
	public static String project = null;
	
	public static final Map<String, Set<Locale>> locales = new HashMap<>();
	
	@SuppressWarnings("null")
	private static void loadLocales() {
		Stream<Path> files = null;
		try {
			files = Files.list(Paths.get(getLangURI(currentLocale)).getParent());
			files.forEach(path -> {
				String locale = path.getFileName().toString();
				if(locale.endsWith(".lang")) {
					locale = locale.substring(0, locale.length() - 5);
				} else
					return;
				String[] parts = locale.split("_");
				String lang = parts[0];
				Locale nLoc = getLocale(parts);
				assert lang != null;
				
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
				throw new IllegalArgumentException(String.join("_", parts) + " is not a valid lang-file!");
		}
		return nLoc;
	}
	
	public static void init(String string) {
		project = string;
		loadLocales();
		load(fallBack, Locale.US != null ? Locale.US : new Locale("en"));
		load(transMap, currentLocale);
		Model.addLocaleListener(Translator::changed);
	}
	
	private static void changed(@Nullable ObservableValue<? extends Locale> observable,
			Locale oldValue, Locale newValue) {
		loadLocale(newValue);
	}
	
	@SuppressWarnings("null")
	public static String translate(String key) {
		if(!currentLocale.equals(Model.getLocale())) {
			loadLocale(Model.getLocale());
		}
		String val;
		return ((val = fallBack.get(key)) != null) || ((val = transMap.get(key)) != null) ? val : key;
	}
	
	public static void loadLocale(Locale loc) {
		if(!currentLocale.equals(loc)) {
			currentLocale = loc;
			transMap.clear();
			load(transMap, currentLocale);
		}
	}
	
	private static void load(Map<String, String> map, Locale loc) {
		Logger log = Logger.getLogger("Translator-" + loc);
		BufferedReader br = null;
		try {
			br = Files.newBufferedReader(Paths.get(getLangURI(loc)),
					Charset.forName("UTF-8"));
			
			br.lines()
			
			.forEach(line -> {
				if(!(line.startsWith("#") || line.startsWith("//"))) {
					int i = line.indexOf('=');
					int c = Math.min(line.indexOf('#'), line.indexOf("//"));
					if(i >= 0) {
						String key = line.substring(0, i).trim(),
						value = line.substring(i + 1,
								c >= 0 ? c : line.length()).trim();
						assert key != null && value != null;
						
						if(map.containsKey(key)) {
							log.log(Level.WARNING, "Found duplicate entry at " + key);
						} else {
							map.put(key, value);
						}
					}
				}
			});
			
		} catch(IOException | UncheckedIOException e) {
			log.log(Level.SEVERE, "Failed to load language file for \"" + loc
					+ "\", falling back to en_US!", e);
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(IOException e) {
					
				}
			}
		}
	}
	
	private static @Nullable URI getLangURI(Locale loc) {
		StringBuilder fileString = new StringBuilder();
		fileString.append("/lang/");
		if(project != null) {
			fileString.append(project + "/");
		}
		fileString.append(loc.toString());
		fileString.append(".lang");
		try {
			return Translator.class.getResource(fileString.toString()).toURI();
		} catch(URISyntaxException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
