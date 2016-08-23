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

/**
 * The basis of a simple controller.
 * 
 * @param <M> the {@link adowrath.fx.model.Model Model} type
 */
@NonNullByDefault public abstract class BasicController<M extends Model>
		implements Initializable {
	
	
	/**
	 * The parent of the frame associated with this controller.
	 * We need knowledge of this to properly localize this.
	 */
	private @Nullable Parent root;
	
	/**
	 * The model specific to this controller.
	 */
	private M ourModel = initModel();
	
	/**
	 * Observes and propagates changes in the locale to the localized
	 * list
	 * 
	 * @param observable
	 *        the locale holder
	 * @param oldValue
	 *        the old locale
	 * @param newValue
	 *        the new locale
	 */
	private void changed(	@Nullable ObservableValue<? extends Locale> observable,
							Locale oldValue,
							Locale newValue) {
		ourModel.getLocList().updateList();
	}
	
	/**
	 * @return
	 * 		the parent, or null if localization is not needed
	 */
	protected abstract @Nullable Parent initRoot();
	
	/**
	 * @return
	 * 		the model to be used for this controller
	 */
	protected abstract M initModel();
	
	/**
	 * This is a delegation of the
	 * {@link #initialize(URL, ResourceBundle) initialize} method so
	 * some default initialization can be done here
	 * 
	 * @param location
	 *        the location of the FXML file loaded
	 * @param resources
	 *        the resources associated with it, if any
	 */
	protected abstract void delegatedInit(	@Nullable URL location,
											@Nullable ResourceBundle resources);
	
	/**
	 * @return
	 * 		whether to translate the view associated with this
	 *         controller or not. If not, a warning will be printed to
	 *         the {@link java.lang.System#err default error stream}
	 */
	protected abstract boolean isToTranslate();
	
	
	/**
	 * @return the model of this controller
	 */
	public final M getModel() {
		return ourModel;
	}
	
	@Override
	public final void initialize(	@Nullable URL location,
									@Nullable ResourceBundle resources) {
		Model.addLocaleListener(this::changed);
		root = initRoot();
		
		delegatedInit(location, resources);
		
		if(isToTranslate()) {
			initLocalization();
		} else {
			System.err.println(toString()
					+ " is not to translate? Is this a working matter?");
		}
	}
	
	/**
	 * Initializes the {@link adowrath.fx.model.localization
	 * .LocalizedList Localized list} with the elements.
	 * 
	 * 
	 * Notice that this only inizializes the elements that were
	 * loaded from the FXML-document. If you add custom elements
	 * dynamically, make sure to add them yourselves.
	 */
	public void initLocalization() {
		Parent r;
		if((r = root) == null)
			throw new IllegalStateException(toString()
					+ " did not give a root element, but wants to be translated? Bug!");
		ourModel.getLocList().initLoc(r.getChildrenUnmodifiable());
	}
	
	@Override
	public abstract String toString();
}
