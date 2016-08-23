package adowrath.fx.model.localization;

import static adowrath.fx.model.localization.Translator.translate;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;


import javafx.css.Styleable;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableView;

/**
 * This List (not implementing the List interface, but still
 * conceptually being a list) saves all the translatable elements of
 * a certain view.
 * <br>
 * Only Styleables are viable for this, as the language key is stored
 * as a styleclass. These are represented as _tl_myLanguageKey
 * <br>
 * <br>
 * Please not that you cannot dynamically change the language key, as
 * that would introduce a large runtime overhead when translating, so
 * please, add the styleable again with
 * {@code initLoc(Collections.singletonList(styleable), false)}. This
 * prevents the children from also being re-added, change to
 * {@code true} if
 * you want this behaviour
 */
@NonNullByDefault public class LocalizedList {
	
	
	/**
	 * The backing map, associating each translatable item with its
	 * language key
	 */
	private Map<Styleable, String> backingMap = new HashMap<>();
	
	/**
	 * Updates the list with the new selected language
	 */
	public void updateList() {
		for(Entry<Styleable, String> e : backingMap.entrySet()) {
			setText(e.getKey(), e.getValue());
		}
	}
	
	/**
	 * @param styleables
	 *        a list of styleables to add
	 * @param doChildren
	 *        whether or not to translate the children, if it has any
	 *        defaults to true
	 */
	public void initLoc(List<? extends Styleable> styleables,
						boolean doChildren) {
		for(Styleable c : styleables) {
			locChildren(c, doChildren);
		}
	}
	
	/**
	 * Adds all children recursively.
	 * 
	 * @param styleables
	 *        a list of styleables to add
	 * @see #initLoc(List, boolean)
	 */
	public void initLoc(List<? extends Styleable> styleables) {
		for(Styleable c : styleables) {
			locChildren(c, true);
		}
	}
	
	/**
	 * @param container
	 *        adds the given Styleable, if it has a translation key,
	 *        and, if it has, any sub-items
	 * @param doChildren
	 *        whether or not to translate the children, if it has any
	 */
	private void locChildren(Styleable container, boolean doChildren) {
		String localizable = getKey(container);
		if(localizable != null) {
			backingMap.put(container, localizable);
			setText(container, localizable);
		}
		if(!doChildren)
			return;
		
		if(container instanceof MenuBar) {
			MenuBar mb = (MenuBar) container;
			initLoc(mb.getMenus(), true);
		} else if(container instanceof Menu) {
			Menu mb = (Menu) container;
			initLoc(mb.getItems(), true);
		} else if(container instanceof TableView) {
			TableView<?> tv = (TableView<?>) container;
			initLoc(tv.getColumns(), true);
		} else if(container instanceof Parent) {
			initLoc(((Parent) container).getChildrenUnmodifiable(), true);
		}
	}
	
	/**
	 * @param s
	 *        the styleable which needs its text replaced
	 * @param key
	 *        the language key
	 */
	private static void setText(Styleable s, String key) {
		try {
			s.getClass().getMethod("setText", String.class)
					.invoke(s, translate(key));
		} catch(IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException ex) {
			System.err.printf(
								"The styleable node %1$s (of class %2$s) does not "
										+ "have an accessible #setText method%n",
								s, s.getClass());
		}
	}
	
	/**
	 * @param s
	 *        Searches for the key specified in
	 * @return
	 * 		the language key or null if none was found
	 */
	private static @Nullable String getKey(Styleable s) {
		String tKey = null;
		for(String st : s.getStyleClass()) {
			if(st.startsWith("_tl_")) {
				tKey = st.substring(4);
				break;
			}
		}
		return tKey;
	}
}
