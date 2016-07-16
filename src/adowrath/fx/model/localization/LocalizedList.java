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

@NonNullByDefault
public class LocalizedList {

	private Map<Styleable, String> backingMap = new HashMap<>();
	
	public void updateList() {
		for(Entry<Styleable, String> e : backingMap.entrySet()) {
			setText(e.getKey(), e.getValue());
		}
	}
	
	public void initLoc(List<? extends Styleable> s) {
		assert s != null;
		
		for(Styleable c : s) {
			assert c != null;
			locChildren(c);
		}
	}

	private void locChildren(Styleable c) {
		String localizable = getKey(c);
		if(localizable != null) {
			backingMap.put(c, localizable);
			setText(c, localizable);
		}
		
		if(c instanceof MenuBar) {
			MenuBar mb = (MenuBar)c;
			initLoc(mb.getMenus());
		} else if(c instanceof Menu) {
			Menu mb = (Menu)c;
			initLoc(mb.getItems());
		} else if(c instanceof TableView) { 
			TableView<?> tv = (TableView<?>)c;
			initLoc(tv.getColumns());
		} else if(c instanceof Parent) {
			initLoc(((Parent)c).getChildrenUnmodifiable());
		}
	}
	
	private void setText(Styleable s, String st) {
		try {
			s.getClass().getMethod("setText", String.class).invoke(s, translate(st));
		} catch(IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException ex) {
			System.out.printf("The styleable node %1$s (of class %2$s) does not "
					+ "have an accessible #setText method", s, s.getClass());
			System.out.println();
		}
	}
	
	private @Nullable String getKey(Styleable s) {
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
