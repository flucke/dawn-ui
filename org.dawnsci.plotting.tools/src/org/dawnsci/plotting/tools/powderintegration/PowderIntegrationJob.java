/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powderintegration;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration1D;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.AbstractPixelIntegration2D;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.NonPixelSplittingIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.NonPixelSplittingIntegration2D;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegrationUtils;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegrationUtils.IntegrationMode;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelSplittingIntegration;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelSplittingIntegration2D;

public class PowderIntegrationJob extends Job {

	private final static Logger logger = LoggerFactory.getLogger(PowderIntegrationJob.class);
	
	IPlottingSystem system;
	IDiffractionMetadata md;
	QSpace qSpace;
	AbstractPixelIntegration integrator;
//	XAxis xAxis = XAxis.Q;
//	IntegrationMode mode = IntegrationMode.NONSPLITTING;
	Dataset data;
	Dataset mask;
	Dataset correction;
	PowderIntegrationModel model;
	PowderCorrectionModel corModel;
	IROI roi;
	int nBins;
	
	public PowderIntegrationJob(IDiffractionMetadata md, IPlottingSystem system) {
		super("Integration");
		this.md = md;
		this.system = system;
		this.qSpace = new QSpace(md.getDetector2DProperties(), md.getDiffractionCrystalEnvironment());
	}
	
	public void setData(Dataset data, Dataset mask, IROI roi) {
		this.data = data;
		this.mask = mask;
		this.roi = roi;
		this.nBins = AbstractPixelIntegration.calculateNumberOfBins(md.getDetector2DProperties().getBeamCentreCoords(),
				data.getShape());
	}
	
	public void clearCorrectionArray() {
		correction = null;
	}

	
	public void setModels(PowderIntegrationModel model, PowderCorrectionModel corModel) {
		
		if (this.model == null) this.model = model;
		if (!isCompatibleIntegrator(integrator, model.getIntegrationMode())) integrator = null;
		this.corModel = corModel;
		updateIntegratorFromModel();
	}
	
	public void updateIntegratorFromModel() {
		if (integrator == null) {
			updateIntegrator();
		}
		if (model != null) {
			//clone incase they get nulled
			
			integrator.setAxisType(model.getAxisType());
			
			if (model.getRadialRange() == null) integrator.setRadialRange(null);
			else integrator.setRadialRange(model.getRadialRange().clone());
			
			if (model.getAzimuthalRange() == null) integrator.setAzimuthalRange(null);
			else integrator.setAzimuthalRange(model.getAzimuthalRange().clone());
			
			integrator.setNumberOfBins(model.getNumberOfPrimaryBins());
			
			if (integrator instanceof AbstractPixelIntegration2D) {
				((AbstractPixelIntegration2D)integrator).setNumberOfAzimuthalBins(model.getNumberOfSecondaryBins());
			}
			
			if (integrator instanceof AbstractPixelIntegration1D) {
				((AbstractPixelIntegration1D)integrator).setAzimuthalIntegration(model.isAzimuthal());
			}
		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (integrator != null) {
			if (!isCompatibleIntegrator(integrator, model.getIntegrationMode())) integrator = null;
		} 
		
		updateIntegratorFromModel();
		
		if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
		integrator.setMask(mask);
//		integrator.setROI(roi);
//		integrator.setAxisType(model.axisType);
		
		if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
		//all accept 2d no splitting should be fast
		if (model.getIntegrationMode() == IntegrationMode.SPLITTING2D) system.setEnabled(false);

		Dataset processed = applyCorrections(data);
		
		if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		
		double maxd = data.max().doubleValue();
		double max = processed.max().doubleValue();
		max = max + 0;
		maxd = maxd + 0;
		
		final List<Dataset> out;
		
		try {
			out = integrator.integrate(processed);
		} catch (Exception e) {
			logger.error("Someones probably just toggling buttons to quickly, but if something looks wrong check here!");
			return Status.CANCEL_STATUS;
		}
		
		system.setEnabled(true);
		
		if (model.getIntegrationMode() == IntegrationMode.NONSPLITTING || model.getIntegrationMode() ==IntegrationMode.SPLITTING) {
			if (system.is2D()) system.reset();
			out.get(1).setName("Intensity");

			system.updatePlot1D(out.get(0), Arrays.asList(new IDataset[]{out.get(1)}), null);
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					system.getSelectedXAxis().setTitle(out.get(0).getName());
					system.getSelectedYAxis().setTitle(out.get(1).getName());

				}
			});
		} else {
			out.get(1).setName("Intensity");
			system.createPlot2D(out.remove(1), out, null);
		}
		
		system.repaint();
		
		
		return Status.OK_STATUS;
	}
	
	public List<Dataset> process(Dataset data) {
		
		if (integrator == null) {
			updateIntegratorFromModel();
			integrator.setMask(mask);
//			integrator.setROI(roi);
//			integrator.setAxisType(xAxis);
		}
		
		Dataset processed = applyCorrections(data);
		
		List<Dataset> out = integrator.integrate(processed);
		
		return out;
		
	}
	
	private Dataset applyCorrections(Dataset data) {
		
		if (corModel == null) return data;
		
		if (!corModel.isApplyPolarisationCorrection() &&
				!corModel.isApplySolidAngleCorrection() &&
				!corModel.isAppyDetectorTransmissionCorrection()) return data;
		
		Dataset localRef;
		
		if (correction == null) {
			correction = DatasetFactory.ones(data, Dataset.FLOAT32);
			
			//incase correction gets nulled while job is running
			localRef = correction;
			Dataset tth = PixelIntegrationUtils.generate2ThetaArrayRadians(data.getShape(), md);
			
			if (corModel.isApplySolidAngleCorrection()) {
				PixelIntegrationUtils.solidAngleCorrection(localRef,tth);
			}
			
			if (corModel.isApplyPolarisationCorrection()) {
				Dataset az = PixelIntegrationUtils.generateAzimuthalArray(data.getShape(), md, true);
				az.iadd(Math.toRadians(corModel.getPolarisationAngularOffset()));
				PixelIntegrationUtils.polarisationCorrection(localRef, tth, az, corModel.getPolarisationFactor());
			}
			
			if (corModel.isAppyDetectorTransmissionCorrection()) {
				PixelIntegrationUtils.detectorTranmissionCorrection(localRef, tth, corModel.getTransmittedFraction());
			}
			
		} else {
			localRef = correction;
		}
		
		return Maths.multiply(data,localRef);
	}
	
	private void updateIntegrator() {
		switch (model.getIntegrationMode()) {
		case NONSPLITTING:
			integrator = new NonPixelSplittingIntegration(md, nBins);
			break;
		case SPLITTING:
			integrator = new PixelSplittingIntegration(md, nBins);
			break;
		case SPLITTING2D:
			integrator = new PixelSplittingIntegration2D(md, nBins,nBins);
			break;
		case NONSPLITTING2D:
			integrator = new NonPixelSplittingIntegration2D(md, nBins,nBins);
			break;
		}
		
		integrator.setAxisType(model.getAxisType());
	}
	
	private boolean isCompatibleIntegrator(AbstractPixelIntegration integrator, IntegrationMode mode) {
		
		if (integrator == null) return false;
		
		switch (mode) {
		case NONSPLITTING:
			return integrator instanceof NonPixelSplittingIntegration;
		case SPLITTING:
			return integrator instanceof PixelSplittingIntegration;
		case SPLITTING2D:
			return integrator instanceof PixelSplittingIntegration2D;
		case NONSPLITTING2D:
			return integrator instanceof NonPixelSplittingIntegration2D;
		}
		return false;
		
	}

}
