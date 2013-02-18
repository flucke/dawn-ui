package org.dawnsci.plotting.tools.diffraction;

import javax.vecmath.Vector3d;


import org.dawnsci.plotting.Activator;
import org.dawnsci.plotting.preference.DiffractionToolConstants;
import org.dawnsci.plotting.preference.detector.DiffractionDetectorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;

public class DiffractionDefaultMetadata {
	
	private static Logger logger = LoggerFactory.getLogger(DiffractionDefaultMetadata.class);
	
	/**
	 * Static method to produce a Detector properties object populated with persisted values
	 * from the preferences store
	 * 
	 * @param shape
	 *            shape from the AbstractDataset the detector properties are created for
	 *            Used to produce the initial detector origin
	 *            
	 */
	public static DetectorProperties getPersistedDetectorProperties(int[] shape) {
		
		int heightInPixels = shape[0];
		int widthInPixels = shape[1];
		
		// Get the default values from the preference store
		double pixelSizeX  = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.PIXEL_SIZE_X);
		double pixelSizeY  = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.PIXEL_SIZE_Y);
		double distance = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.DISTANCE);
		
		//Guess pixel Size from the shape of the image
		double[] pixelXY = DiffractionDetectorHelper.getXYPixelSizeMM(shape);
		
		if (pixelXY != null) {
			pixelSizeX = pixelXY[0];
			pixelSizeY = pixelXY[1];
		}
		
		// Create the detector origin vector based on the above
		double[] detectorOrigin = { (widthInPixels - widthInPixels/2d) * pixelSizeX, (heightInPixels - heightInPixels/2d) * pixelSizeY, distance };
		
		// The rotation of the detector relative to the reference frame - assume no rotation
		double detectorRotationX = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROTATION_X);
		double detectorRotationY = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROTATION_Y);
		double detectorRotationZ = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROTATION_Z);

		return new DetectorProperties(new Vector3d(detectorOrigin), heightInPixels, widthInPixels, 
				pixelSizeX, pixelSizeY, detectorRotationX, detectorRotationY, detectorRotationZ);
	}

	/**
	 * Static method to produce a DiffractionCrystalEnvironment properties object populated with persisted values
	 * from the preferences store
	 */
	public static DiffractionCrystalEnvironment getPersistedDiffractionCrystalEnvironment() {
		double lambda = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.LAMBDA);
		double startOmega = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.START_OMEGA);
		double rangeOmega = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.RANGE_OMEGA);
		double exposureTime = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.EXPOSURE_TIME);
		
		return new DiffractionCrystalEnvironment(lambda, startOmega, rangeOmega, exposureTime);
	}
	
	/**
	 * Static method to replace the values in the old DiffractionCrystalEnvironment with the new
	 */
	public static void copyNewOverOld(DiffractionCrystalEnvironment newDCE, DiffractionCrystalEnvironment oldDCE) {
		
		oldDCE.setExposureTime(newDCE.getExposureTime());
		oldDCE.setPhiRange(newDCE.getPhiRange());
		oldDCE.setPhiStart(newDCE.getPhiStart());
		oldDCE.setWavelength(newDCE.getWavelength());

	}
	
	/**
	 * Static method to replace the values in the old DetectorProperties with the new
	 */
	public static void copyNewOverOld(DetectorProperties newDP, DetectorProperties oldDP) {
		
		oldDP.setOrigin(new Vector3d(newDP.getOrigin()));
		oldDP.setBeamVector(new Vector3d(newDP.getBeamVector()));
		oldDP.setPx(newDP.getPx());
		oldDP.setPy(newDP.getPy());
		oldDP.setVPxSize(newDP.getVPxSize());
		oldDP.setHPxSize(newDP.getHPxSize());
		oldDP.setOrientation(newDP.getOrientation());

	}
	
	/**
	 * Static method to replace the values in the DiffractionCrystalEnvironment and DetectorProperties
	 *  of an old IDiffractionmetaData with a new
	 */
	public static void copyNewOverOld(IDiffractionMetadata newDM, IDiffractionMetadata oldDM){
		copyNewOverOld(newDM.getDetector2DProperties(), oldDM.getDetector2DProperties());
		copyNewOverOld(newDM.getDiffractionCrystalEnvironment(), oldDM.getDiffractionCrystalEnvironment());
	}
	
	
	/**
	 * Static method to produce a Detector properties object populated with default values
	 * from the preferences store
	 * 
	 * @param shape
	 *            shape from the AbstractDataset the detector properties are created for
	 *            Used to produce the initial detector origin
	 *            
	 */
	public static DetectorProperties getDefaultDetectorProperties(int[] shape) {
		
		int heightInPixels = shape[0];
		int widthInPixels = shape[1];
		
		// Get the default values from the preference store
		double pixelSizeX  = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.PIXEL_SIZE_X);
		double pixelSizeY  = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.PIXEL_SIZE_Y);
		double distance = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.DISTANCE);
		
		// Create the detector origin vector based on the above
		double[] detectorOrigin = { (widthInPixels - widthInPixels/2d) * pixelSizeX, (heightInPixels - heightInPixels/2d) * pixelSizeY, distance };
		
		// The rotation of the detector relative to the reference frame - assume no rotation
		double detectorRotationX = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROTATION_X);
		double detectorRotationY = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROTATION_Y);
		double detectorRotationZ = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROTATION_Z);

		return new DetectorProperties(new Vector3d(detectorOrigin), heightInPixels, widthInPixels, 
				pixelSizeX, pixelSizeY, detectorRotationX, detectorRotationY, detectorRotationZ);
	}
	
	/**
	 * Static method to produce a DiffractionCrystalEnvironment properties object populated with default values
	 * from the preferences store
	 */
	public static DiffractionCrystalEnvironment getDefaultDiffractionCrystalEnvironment() {
		double lambda = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.LAMBDA);
		double startOmega = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.START_OMEGA);
		double rangeOmega = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.RANGE_OMEGA);
		double exposureTime = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.EXPOSURE_TIME);
		
		return new DiffractionCrystalEnvironment(lambda, startOmega, rangeOmega, exposureTime);
	}
	
	/**
	 * Static method to set the default DiffractionCrystalEnvironment values in the 
	 * from the preferences store
	 */
	public static void setPersistedDiffractionCrystalEnvironmentValues(DiffractionCrystalEnvironment dce){
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.LAMBDA, dce.getWavelength());
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.START_OMEGA, dce.getPhiStart());
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.RANGE_OMEGA, dce.getPhiRange());
	}
	
	/**
	 * Static method to set the default DetectorProperties values in the 
	 * from the preferences store
	 */
	public static void setPersistedDetectorPropertieValues(DetectorProperties detprop) {
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.PIXEL_SIZE_X, detprop.getVPxSize());
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.PIXEL_SIZE_Y, detprop.getHPxSize());
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.DISTANCE, detprop.getOrigin().z);
		
	}
	
	/**
	 * Static method to obtain a DiffractionMetaDataAdapter populated with default values
	 * from the preferences store to act as a starting point for images without metadata
	 */
	public static IDiffractionMetadata getDiffractionMetadata(String filePath, int[] shape) {
		
		final DetectorProperties detprop = getPersistedDetectorProperties(shape);
		final DiffractionCrystalEnvironment diffenv = getPersistedDiffractionCrystalEnvironment();
		
		logger.debug("Meta read from preferences");
		
		return new DiffractionMetadata(filePath, detprop, diffenv);
		
	}
	
	public static IDiffractionMetadata getDiffractionMetadata(int[] shape) {
		
		final DetectorProperties detprop = getPersistedDetectorProperties(shape);
		final DiffractionCrystalEnvironment diffenv = getPersistedDiffractionCrystalEnvironment();
		
		logger.debug("Meta read from preferences");
		
		return new DiffractionMetadata(null, detprop, diffenv);
		
	}
}