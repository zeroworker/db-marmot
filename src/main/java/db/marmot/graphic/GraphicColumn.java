package db.marmot.graphic;

import java.io.Serializable;

import db.marmot.volume.DataVolume;

/**
 * @author shaokang
 */
public interface GraphicColumn extends Serializable {
	
	/**
	 * 校验维度字段
	 * @param dataVolume
	 */
	void validateDimenColumn(DataVolume dataVolume, boolean aggregateDimen);
	
	/**
	 * 校验维度字段
	 * @param dataVolume
	 * @param aggregateMeasure
	 */
	void validateMeasureColumn(DataVolume dataVolume, boolean aggregateMeasure);
	
	/**
	 * 校验过滤字段
	 * @param dataVolume
	 */
	void validateFilterColumn(DataVolume dataVolume);
	
}
