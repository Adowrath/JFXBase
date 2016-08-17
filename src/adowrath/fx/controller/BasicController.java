package adowrath.fx.controller;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;


import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;


import adowrath.fx.model.Model;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

@NonNullByDefault
public abstract class BasicController<M extends Model> implements Initializable {
	
	@Nullable
	private Parent root;
	private M ourModel = initModel();

	private void changed(@Nullable ObservableValue<? extends Locale> observable,
			Locale oldValue, Locale newValue) {
		ourModel.getLocList().updateList();
	}
	
	protected abstract @Nullable Parent initRoot();
	protected abstract M initModel();
	protected abstract void delegatedInit(@Nullable URL location, @Nullable ResourceBundle resources);
	protected abstract boolean isToTranslate();
	
	
	public final M getModel() {
		return ourModel;
	}
	
	@Override
	public final void initialize(@Nullable URL location, @Nullable ResourceBundle resources) {
		Model.addLocaleListener(this::changed);
		root = initRoot();
		
		delegatedInit(location, resources);
		
		if(isToTranslate()) {
			initLocalization();
		} else {
			System.err.println(toString() + " is not to translate? Is this a working matter?");
		}
	}
	
	public void initLocalization() {
		Parent r;
		if((r = root) == null)
			throw new IllegalStateException(toString() + " did not give a root element, but wants to be translated? Bug!");
		ourModel.getLocList().initLoc(r.getChildrenUnmodifiable());
	}
	
	@Override
	public abstract String toString();
}
