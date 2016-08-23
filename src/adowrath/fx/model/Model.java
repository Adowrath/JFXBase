package adowrath.fx.model;

import java.util.Iterator;
import java.util.Locale;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;


import adowrath.fx.model.localization.LocalizedList;
import adowrath.fx.model.localization.Translator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

/**
 * The basic model,
 */
@NonNullByDefault
public abstract class Model {
	
	
	/**
	 * This object property holds the current Locale that is used
	 * accross the whole application.
	 */
	private static final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(Locale
			.getDefault());
	
	
	/**
	 * The localized list associated with this model.
	 */
	private final LocalizedList locList;
	
	/**
	 * The default constructor
	 */
	public Model() {
		locList = new LocalizedList();
	}
	
	/**
	 * @return
	 * 		the localized List
	 */
	public final LocalizedList getLocList() {
		return locList;
	}
	
	/**
	 * @param listener
	 *        a new changeListener that should listen to changes in
	 *        the locale
	 */
	public static final void addLocaleListener(ChangeListener<? super Locale> listener) {
		locale.addListener(listener);
	}
	
	/**
	 * @param newLocale
	 *        the new, non-null locale that is selected
	 */
	public static final void changeLocale(Locale newLocale) {
		locale.set(newLocale);
	}
	
	/**
	 * @return
	 * 		the currently selected locale.
	 */
	public static final Locale getLocale() {
		return locale.get();
	}
	
	/**
	 * Removes a previously registered listener. If it has not been
	 * previously registered, this is considered a no-op.
	 * 
	 * @param listener
	 *        the listener to be removed, cannot be null
	 * 
	 * @see javafx.beans.value.ObservableValue#removeListener(ChangeListener)
	 *      ObservableValue.removeListener
	 */
	public static final void removeLocaleListener(ChangeListener<? super Locale> listener) {
		locale.removeListener(listener);
	}
	
	
	/**
	 * This holds the individual menu items responsible for the
	 * language selection
	 */
	private static ObservableList<MenuItem> translatedItems = FXCollections
			.observableArrayList();
	
	/**
	 * @param men
	 *        the menu in which the language items should be placed
	 * @throws IllegalStateException
	 *         if the provided menu really was null. The annotation is
	 *         presented in the first place due to checkers being
	 *         unable to correctly identify {@code @FXML} fields as
	 *         being initialized and saves the user from the hassle of
	 *         proving the non-nullness by assertions and the like.
	 *         Also, it gives a clearer error message than a simple
	 *         NPE.
	 */
	public final void fillMenuWithTranslations(@Nullable Menu men) {
		if(translatedItems.isEmpty()) {
			translatedItems = loadLangs();
		}
		if(men == null)
			throw new IllegalStateException(toString()
					+ " menu was not properly initialized");
		men.getItems().clear();
		men.getItems().addAll(translatedItems);
	}
	
	/**
	 * @return
	 * 		a list of menu items generated from all found .lang
	 *         files
	 */
	private static final ObservableList<MenuItem> loadLangs() {
		final ObservableList<MenuItem> men = FXCollections
				.observableArrayList();
		
		
		EventHandler<ActionEvent> handler = ae -> {
			RadioMenuItem rmi = (RadioMenuItem) ae.getSource();
			String newLocale = null;
			for(Iterator<String> ite = rmi.getStyleClass().iterator(); ite
					.hasNext() && newLocale == null;) {
				String s = ite.next();
				if(s.startsWith("_lc_")) {
					newLocale = s.substring(4);
				}
			}
			assert newLocale != null;
			String[] parts = newLocale.split("_");
			Locale loc = Translator.getLocale(parts);
			
			Model.changeLocale(loc);
			
			men.forEach(i -> {
				String nLocale = null;
				for(Iterator<String> ite = i.getStyleClass().iterator(); ite
						.hasNext() && nLocale == null;) {
					String s = ite.next();
					if(s.startsWith("_lc_")) {
						nLocale = s.substring(4);
					}
				}
				assert nLocale != null;
				String[] nParts = nLocale.split("_");
				Locale nLoc = Translator.getLocale(nParts);
				i.setText(nLoc.getDisplayName(Model.getLocale()));
			});
		};
		
		ToggleGroup tg = new ToggleGroup();
		
		Translator.locales.values().forEach(set -> set.forEach(loc -> {
			RadioMenuItem rmi = new RadioMenuItem(loc
					.getDisplayName(Model.getLocale()));
			rmi.getStyleClass().add("_lc_" + loc.toString());
			rmi.setToggleGroup(tg);
			rmi.setOnAction(handler);
			men.add(rmi);
		}));
		
		return FXCollections.unmodifiableObservableList(men);
	}
}
