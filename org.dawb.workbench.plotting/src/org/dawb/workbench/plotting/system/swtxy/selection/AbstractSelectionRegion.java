package org.dawb.workbench.plotting.system.swtxy.selection;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.IAxisListener;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.dawb.common.ui.plot.region.AbstractRegion;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.system.swtxy.IMobileFigure;
import org.dawb.workbench.plotting.system.swtxy.RegionBean;
import org.dawb.workbench.plotting.system.swtxy.translate.TranslationEvent;
import org.dawb.workbench.plotting.system.swtxy.translate.TranslationListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * An AbstractSelectionRegion has two purposes:
 * 1. To draw the 2d shapes for selection in the diagram.
 * 2. To return the selection for the region in the real world coordinates.
 * 
 * Shape used for ROIs which has bounds fixed to the graph area.
 * Links regions to the specifics of the XY Plotting system.
 * 
 * NOTE you may implement this class two ways:
 * 1. by creating contents in the createContents(...) method and adding them to the parent.
 * 2. by adding contents to this figure.
 * 
 * If doing method 2. remember to add this figure to the parent. If doing this the children can be drawn in the local coordinates for the figure.
 * However note in that case when firing selection events in the axis coordinates parent figure location
 * will also need to be used. If using 1. the contents of the figure are directly added to the graph figure
 * and therefore their location can be used directly also there are no bounds of this figure to deal with.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractSelectionRegion extends AbstractRegion implements IAxisListener {


	private RegionBean bean;
    private ISelectionProvider selectionProvider;
    private IFigure[] regionObjects;
    private int lineWidth=0;

	public AbstractSelectionRegion(String name, Axis xAxis, Axis yAxis) {
		super();
		setEnabled(false); // No mouse events.
		setOpaque(false);
		setCursor(null);
		this.bean = new RegionBean();
		bean.setName(name);
		bean.setxAxis(xAxis);
		bean.setyAxis(yAxis);
		xAxis.addListener(this);
		yAxis.addListener(this);
	}

	/**
	 * Creates the contents of the selection ie the figure(s)
	 * which make up the selection. You may add the children directly to the parent here.
	 * Otherwise add children to this figure, set its bounds and add this figure to the
	 * parent.
	 * 
	 * @param parent
	 */
	public abstract void createContents(final Figure parent);

	/**
	 * Return the type of region which this provides UI for.
	 * @return
	 */
	public abstract RegionType getRegionType();
	
	/**
	 * If there is a fill figure, this method may be called to 
	 * refill the fill.
	 */
	protected abstract void updateConnectionBounds();
	
	/**
	 * Paint the regions before it is created during the first click
	 * and drag of the user.
	 * 
	 * @param g
	 * @param start
	 * @param end
	 */
	public abstract void paintBeforeAdded(final Graphics g, Point firstClick, Point dragLocation, Rectangle parentBounds);

	/**
	 * Sets the local of the region in local coordinates not axis ones.
	 * @param bounds
	 */
	public abstract void setLocalBounds(Point firstClick, Point dragLocation, Rectangle parentBounds);
	
	/**
	 * This method should be implemented to fire a StructuredSelection
	 * whose first object is an object extending ROIBase. It will be called
	 * when the user has finished clicking and dragging a selection.
	 * 
	 * To implement live updates the user should add an IRegionBoundsListener
	 * which will be notified on drag.
	 * 
	 */
	protected abstract void fireRoiSelection();

	
	/**
	 * This cursor is used after the region is created and
	 * before the user has clicked to create it. This is the
	 * path to the cursor, for instance "icons/Cursor-box.png"
	 * @return
	 */
	protected abstract String getCursorPath();

	public void sync(RegionBean bean) {
		setName(bean.getName());
		setShowPosition(bean.isShowPosition());
		setxAxis(bean.getxAxis());
		setyAxis(bean.getyAxis());
		setXyGraph(bean.getXyGraph());
		setRegionColor(bean.getRegionColor());
		setAlpha(bean.getAlpha());
		setVisible(bean.isVisible());
		setMobile(bean.isMobile());
		setShowLabel(bean.isShowLabel());
	}
	
	private Cursor cursor;
	/**
	 * A new cursor is created on each call.
	 */
	public Cursor getRegionCursor() {
		if (cursor==null)  {
			Image image = Activator.getImage(getCursorPath());
			cursor = new Cursor(Display.getDefault(), image.getImageData(), 8, 8);
		}
		return cursor;
	}

	protected void drawLabel(Graphics gc, Rectangle size) {
		if (isShowLabel()&&getName()!=null) {
			gc.setAlpha(255);
			gc.setForegroundColor(ColorConstants.black);
			gc.drawText(getName(), size.getCenter());
		}
	}
	
	public void axisRevalidated(Axis axis) {
		updateRegionBounds();
	}
	public void axisRangeChanged(Axis axis, Range old_range, Range new_range) {
		updateRegionBounds();
	}
			
	protected void setRegionObjects(IFigure... objects) {
		this.regionObjects = objects;
	}
	
	public String getName() {
		return bean.getName();
	}
	
	/**
	 * Remove from graph and remove all RegionBoundsListeners.
	 * 
	 */
	public void remove() {
		clearListeners();
		if (getParent()!=null) getParent().remove(this);
		if (regionObjects!=null)for (IFigure ob : regionObjects) {
			if (ob.getParent()!=null) ob.getParent().remove(ob);
		}
		if (cursor!=null) cursor.dispose();
	}

	protected void clearListeners() {
        getxAxis().removeListener(this);
        getyAxis().removeListener(this);
		super.clearListeners();
	}
	
	public void setName(String name) {
		bean.setName(name);
	}


	public XYGraph getXyGraph() {
		return bean.getXyGraph();
	}


	public void setXyGraph(XYGraph xyGraph) {
		bean.setXyGraph(xyGraph);
	}

	public Color getRegionColor() {
		return bean.getRegionColor();
	}


	public void setRegionColor(Color regionColor) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob!=null) {
				ob.setForegroundColor(regionColor);
				ob.setBackgroundColor(regionColor);
			}
		}
		bean.setRegionColor(regionColor);
	}


	public boolean isShowPosition() {
		return bean.isShowPosition();
	}


	public void setShowPosition(boolean showPosition) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setShowPosition(showPosition);
			}
		}
		bean.setShowPosition(showPosition);
	}

	public void setAlpha(int alpha) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setAlpha(alpha);
			}
			if (ob instanceof Shape) {
				((Shape)ob).setAlpha(alpha);
			}
		}
		bean.setAlpha(alpha);
	}
	public int getAlpha() {
		return bean.getAlpha();
	}

	public RegionBean getBean() {
		return bean;
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public void setSelectionProvider(ISelectionProvider selectionProvider) {
		this.selectionProvider = selectionProvider;
	}

	public boolean isVisible() {
		return bean.isVisible();
	}

	public void setVisible(boolean visible) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof IMobileFigure) {
				((IMobileFigure)ob).setVisible(visible&&(isMobile()||isTrackMouse()));
			} else {
			    if (ob!=null) ob.setVisible(visible);
			}
		}
		bean.setVisible(visible);
	}

	public boolean isMobile() {
		return bean.isMobile();
	}

	@Override
	public void setMobile(boolean mobile) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof IMobileFigure) {
				((IMobileFigure)ob).setVisible(mobile);
			} else if (ob instanceof RegionFillFigure) {
				((RegionFillFigure)ob).setMobile(mobile);
			}
		}
		bean.setMobile(mobile);
	}


	public void repaint() {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob!=null) ob.repaint();
		}
	}

	public Axis getxAxis() {
		return bean.getxAxis();
	}

	public void setxAxis(Axis xAxis) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setxAxis(xAxis);
			}
		}
		getxAxis().removeListener(this);
		xAxis.addListener(this);
		bean.setxAxis(xAxis);
	}

	public Axis getyAxis() {
		return bean.getyAxis();
	}

	public void setyAxis(Axis yAxis) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setyAxis(yAxis);
			}
		}
		getyAxis().removeListener(this);
		yAxis.addListener(this);
		bean.setyAxis(yAxis);
	}
	
	public TranslationListener createRegionNotifier() {
		return new TranslationListener() {
			@Override
			public void translateBefore(TranslationEvent evt) {
				
			}
			@Override
			public void translationAfter(TranslationEvent evt) {
				updateConnectionBounds();
				fireRegionBoundsDragged(createRegionBounds(false));
			}
			@Override
			public void translationCompleted(TranslationEvent evt) {
				fireRegionBoundsChanged(createRegionBounds(true));
				fireRoiSelection();
			}
			
		};
	}

	public boolean isShowLabel() {
		return bean.isShowLabel();
	}

	public void setShowLabel(boolean showLabel) {
		bean.setShowLabel(showLabel);
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}
}
