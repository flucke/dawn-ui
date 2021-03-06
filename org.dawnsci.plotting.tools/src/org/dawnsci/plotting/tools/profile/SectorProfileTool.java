/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.region.ILockableRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public abstract class SectorProfileTool extends ProfileTool {


	private   MenuAction      center;
	protected Action          combineSymmetry;
	private   IRegionListener sectorRegionListener;

	@Override
	protected void configurePlottingSystem(IPlottingSystem plotter) {
		
		final Action downsample = new Action("Always downsample (useful for high update rates)", IAction.AS_CHECK_BOX) {
			public void run() {
				alwaysDownsample = isChecked();
				Activator.getLocalPreferenceStore().setValue(PlottingConstants.ALWAYS_DOWNSAMPLE_PROFILES, alwaysDownsample);
			}
		};
		downsample.setChecked(alwaysDownsample);
		downsample.setImageDescriptor(Activator.getImageDescriptor("icons/navigation-270.png"));
		getSite().getActionBars().getToolBarManager().add(downsample);
		getSite().getActionBars().getMenuManager().add(downsample);
		getSite().getActionBars().getToolBarManager().add(new Separator());
	

		// We will add an action here for centring the sector.
		this.center = new MenuAction("Centre selection");
		center.setImageDescriptor(Activator.getImageDescriptor("icons/sector-center-menu.png"));
		
		getSite().getActionBars().getToolBarManager().add(center);
		getSite().getActionBars().getMenuManager().add(center);
		updateSectors();
		
		this.sectorRegionListener  = new IRegionListener.Stub() {
			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				updateSectors();
			}
			
			@Override
			public void regionRemoved(RegionEvent evt) {
				if (evt.getRegion()!=null && evt.getRegion().isUserRegion()) { 
					updateSectors();
				};
			}
			@Override
			public void regionAdded(RegionEvent evt) {
				if (evt.getRegion()!=null && evt.getRegion().isUserRegion()) { 
					updateSectors();
				}
					
				if (evt.getRegion()!=null && evt.getRegion().getRegionType()==RegionType.SECTOR) {
					SectorROI sroi = (SectorROI) evt.getRegion().getROI().copy();
					sroi.setSymmetry(preferredSymmetry);
					sroi.setCombineSymmetry(preferredCombine);
					evt.getRegion().setROI(sroi);
				}
			}
		};
		
		final MenuAction symmetry = new MenuAction("Symmetry setting");
		symmetry.setImageDescriptor(Activator.getImageDescriptor("icons/sector-symmetry-menu.png"));
		getSite().getActionBars().getToolBarManager().add(symmetry);
		getSite().getActionBars().getMenuManager().add(symmetry);
		
		addSymmetryActions(symmetry);
		
	}
	
	private int lastSymmetry = 0;
	@Override
	public void roiChanged(ROIEvent evt) {
		
		if (evt.getROI()!=null && evt.getROI() instanceof SectorROI) {
			SectorROI sroi = (SectorROI)evt.getROI();
			if (sroi.getSymmetry()!=lastSymmetry) { // New plots required
				profilePlottingSystem.clear();
			}
			lastSymmetry = sroi.getSymmetry();
		}

        super.roiChanged(evt);
	}
	
	private int     preferredSymmetry = SectorROI.NONE;
	private boolean preferredCombine  = false;
	
	private void addSymmetryActions(final MenuAction symmetry) {
		
		final CheckableActionGroup group = new CheckableActionGroup();
		for (int isymmetry = 0; isymmetry < 7; isymmetry++) {

			final int finalSym = isymmetry;
			
			final Action action = new Action(SectorROI.getSymmetryText(isymmetry), IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					
					preferredSymmetry = finalSym;
					final Collection<IRegion> regions = getPlottingSystem().getRegions(RegionType.SECTOR);
					
					Collection<IRegion> notPossible = new ArrayList<IRegion>(3);
					if (regions!=null) for (IRegion iRegion : regions) {
						SectorROI roi = (SectorROI)iRegion.getROI();
						
						//if (roi.checkSymmetry(finalSym)) {
							roi = roi.copy();
							roi.setSymmetry(finalSym);
							iRegion.setROI(roi);
						//} else {						
						//	notPossible.add(iRegion);
						//}
					}
					
					update(null, null, false);

					if (notPossible.size()>0) {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot use symmetry",
								                "The region"+(notPossible.size()==1?": '":"s: '")+notPossible.toString().substring(1,notPossible.toString().length()-2)+"'\n"+
								                "cannot have "+(notPossible.size()==1?"its":"their")+" symmetry set to "+SectorROI.getSymmetryText(finalSym)+
								                "\n\nPlease try a different symmetry for "+(notPossible.size()==1?"it.":"them."));
					}
				}
			};	
            group.add(action);
			symmetry.add(action);
		}
	
		symmetry.getAction(0).setChecked(true);
		
		this.combineSymmetry = new Action("Combine symmetry", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				preferredCombine = isChecked();
				
				final Collection<IRegion> regions = getPlottingSystem().getRegions(RegionType.SECTOR);
				if (regions!=null) for (IRegion iRegion : regions) {
					SectorROI roi = (SectorROI)iRegion.getROI();
					roi = roi.copy();
					roi.setCombineSymmetry(isChecked());
					iRegion.setROI(roi);
				}
				//clear plotting system to make sure uncombined traces are cleared
				profilePlottingSystem.clear();
				update(null, null, false);
			    
			}
		};
		combineSymmetry.setImageDescriptor(Activator.getImageDescriptor("icons/sector-symmetry-combine.png"));
		getSite().getActionBars().getToolBarManager().add(combineSymmetry);
		getSite().getActionBars().getMenuManager().add(combineSymmetry);
		
		
		final Action lock = new Action("Lock center of all current sectors", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				final boolean centerMovable = !isChecked();
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions!=null) for (IRegion iRegion : regions) {
					if (iRegion instanceof ILockableRegion) {
						ILockableRegion lr = (ILockableRegion)iRegion;
						lr.setCentreMovable(centerMovable);
					}
				}
			}
		};
		
		lock.setId("org.dawb.workbench.plotting.tools.profile.lockSectorCenters");
		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));

		getSite().getActionBars().getToolBarManager().add(new Separator());
    	getSite().getActionBars().getToolBarManager().add(lock);
		getSite().getActionBars().getMenuManager().add(new Separator());
    	getSite().getActionBars().getMenuManager().add(lock);
		
	}

	public void activate() {
		super.activate();
		
		if (getPlottingSystem()!=null && sectorRegionListener!=null) {
			getPlottingSystem().addRegionListener(sectorRegionListener);
		}
	}
	
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(sectorRegionListener);
		}
	}
	
	protected void updateSectors() {
		
		if (getPlottingSystem()==null) return;
		
		center.clear();
		
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions!=null) for (final IRegion region : regions) {
			if (isRegionTypeSupported(region.getRegionType())) {
				
				final Action centerRegion = new Action("Center sector '"+region.getName()+"'") {
					public void run() {
						center.setSelectedAction(this);
						final double[] cen = getBeamCenter();
						if (cen!=null) {
							final SectorROI sroi = (SectorROI)region.getROI();
							sroi.setPoint(cen);
							region.setROI(sroi);
							
							center.setSelectedAction(this);
						}
					}
				};
				centerRegion.setImageDescriptor(Activator.getImageDescriptor("icons/sector-center-action.png"));
			
				center.add(centerRegion);
			}
		}
		
		if (center.size()>0) center.setSelectedAction(0);
			
		// TODO likely to cause flicker
		getSite().getActionBars().getToolBarManager().update(true);
		getSite().getActionBars().getMenuManager().update(true);
	}

	protected double[] getBeamCenter() {
		
        IMetadata meta = getMetaData();
        if (meta==null || !(meta instanceof IDiffractionMetadata)) {
        	return getImageCenter();
        }
        
        IDiffractionMetadata dm = (IDiffractionMetadata)meta;
        
        if (dm.getDetector2DProperties()==null) return getImageCenter();
        try {
        	return dm.getDetector2DProperties().getBeamCentreCoords();
        } catch (NullPointerException npe) {
        	return getImageCenter();
        }
 	}

	private double[] getImageCenter() {
    	final Dataset image = (Dataset)getImageTrace().getData();
    	return new double[]{image.getShape()[1]/2d, image.getShape()[0]/2d};
	}

	protected abstract Dataset[] getXAxis(final SectorROI sroi, final Dataset[] integrals);
	
	/**
	 * Please name the integral the same as the name you would like to plot.
	 * 
	 * @param data
	 * @param mask
	 * @param sroi
	 * @param region
	 * @param isDrag
	 * @return either array size 1 or 2. If 2, 2 plots are created on the profile
	 */
	protected abstract Dataset[] getIntegral( final Dataset data,
										              final Dataset mask, 
										              final SectorROI       sroi, 
						                              final IRegion         region,
										              final boolean         isDrag,
										              final int             downsample);


	@Override
	protected void createProfile(IImageTrace  image, 
			                     IRegion      region, 
			                     IROI      rbs, 
			                     boolean      tryUpdate,
			                     boolean      isDrag,
			                     IProgressMonitor monitor) {
        
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final SectorROI sroi = (SectorROI) (rbs==null ? region.getROI() : rbs);
		if (sroi==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		final Dataset data = isDrag ? (Dataset)image.getDownsampled()     : (Dataset)image.getData();
		final Dataset mask = isDrag ? (Dataset)image.getDownsampledMask() : (Dataset)image.getMask();
		
		SectorROI downsroi = null;
		if (isDrag) {
			downsroi = sroi.copy();
			downsroi.downsample(image.getDownsampleBin());
		}
			
		final Dataset[] integrals = getIntegral(data, mask, isDrag ? downsroi : sroi, region, isDrag, isDrag ? image.getDownsampleBin() : 1);	
        if (integrals==null) return;
				
		final Dataset[] xis = getXAxis(sroi, integrals);

		for (int i = 0; i < 2; i++) {
			if (integrals[i] != null) {
				final Dataset integral = integrals[i];
				final Dataset xi = xis[i];

				if (integral != null) {
					final ILineTrace x_trace = (ILineTrace) profilePlottingSystem.getTrace(integral.getName());
					if (tryUpdate && x_trace != null) {
						getControl().getDisplay().syncExec(new Runnable() {
							public void run() {
								x_trace.setData(xi, integral);
							}
						});
					} else {
						Collection<ITrace> plotted = profilePlottingSystem.createPlot1D(xi, Arrays.asList(new IDataset[] { integral }), monitor);
						registerTraces(region, plotted);
					}
				}
			}
		}
	}
	
	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return type==RegionType.SECTOR || type==RegionType.RING;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.SECTOR;
	}

}
