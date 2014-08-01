package org.dawnsci.processing.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

import uk.ac.diamond.scisoft.analysis.processing.model.IOperationModel;
import uk.ac.diamond.scisoft.analysis.processing.model.OperationModelField;

/**
 * @see https://www.eclipse.org/articles/Article-Properties-View/properties-view.html
 * 
 * @see https://www.eclipse.org/articles/Article-Tabbed-Properties/tabbed_properties_view.html
 * 
 * We make a properties source to allow editing of the model. We read annotations from the
 * model to provide the correct editor for a field or provide a custom editor.
 * 
 * @author fcp94556
 *
 */
class OperationModelSource implements IPropertySource {
	
	private IOperationModel model;

	OperationModelSource(final IOperationModel model) {
		this.model = model;
	}

	@Override
	public Object getEditableValue() {
		return this;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		
		// Decided not to use the obvious BeanMap here because class problems with
		// GDA and we have to read annotations anyway.
		final Field[] fields = model.getClass().getDeclaredFields();
		
		// The returned descriptor
		final List<IPropertyDescriptor> ret = new ArrayList<IPropertyDescriptor>(fields.length);
		
		// fields
		for (Field field : fields) {
			
			// If there is a getter/isser for the field we assume it is a model field.
			try {
				if (model.isModelField(field.getName())) {			
					ret.add(new OperationPropertyDescriptor(model, field.getName()));
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		return ret.toArray(new IPropertyDescriptor[ret.size()]);
		
	}

	@Override
	public Object getPropertyValue(Object id) {
		try {
			return model.get(id.toString());
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public boolean isPropertySet(Object id) {
		try {
			return model.get(id.toString())!=null;
		} catch (Exception e) {
			return false; // Why no boolean state for maybe? Discuss...
		}
	}

	@Override
	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		try {
			model.set((String)id, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}