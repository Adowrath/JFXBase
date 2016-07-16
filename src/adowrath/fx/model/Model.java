package adowrath.fx.model;

import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;


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

@NonNullByDefault
public abstract class Model {
	
	private static final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(Locale.getDefault());
	
	
	private final LocalizedList locList;
	
	public Model() {
		locList = new LocalizedList();
	}
	
	public final LocalizedList getLocList() {
		return locList;
	}
	
	public static final void addLocaleListener(ChangeListener<? super Locale> listener) {
		locale.addListener(listener);
	}
	
	public static final void changeLocale(Locale newLocale) {
		Objects.requireNonNull(newLocale, "The new Locale was null");
		locale.set(newLocale);
	}
	
	public static final Locale getLocale() {
		return locale.get();
	}
	
	public static final void removeLocaleListener(ChangeListener<? super Locale> listener) {
		locale.removeListener(listener);
	}
	
	private static ObservableList<MenuItem> translatedItems = FXCollections.observableArrayList();
	
	public final void fillMenuWithTranslations(@Nullable Menu men) {
		if(translatedItems.isEmpty()) {
			translatedItems = loadLangs();
		}
		if(men == null)
			throw new IllegalStateException(toString() + " menu was not properly initialized");
		men.getItems().clear();
		for(MenuItem mi : translatedItems) {
			men.getItems().add(mi);
		}
	}
	
	private static final ObservableList<MenuItem> loadLangs() {
		final ObservableList<MenuItem> men = FXCollections.observableArrayList();
		
		
		EventHandler<ActionEvent> handler = ae -> {
			RadioMenuItem rmi = (RadioMenuItem) ae.getSource();
			String newLocale = null;
			for(Iterator<String> ite = rmi.getStyleClass().iterator(); ite.hasNext() && newLocale == null;) {
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
				for(Iterator<String> ite = i.getStyleClass().iterator(); ite.hasNext() && nLocale == null;) {
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
			RadioMenuItem rmi = new RadioMenuItem(loc.getDisplayName(Model.getLocale()));
			rmi.getStyleClass().add("_lc_" + loc.toString());
			rmi.setToggleGroup(tg);
			rmi.setOnAction(handler);
			men.add(rmi);
		}));
		
		return FXCollections.unmodifiableObservableList(men);
	}
}
