package db.marmot.volume.controller.request;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class DataVolumeRequest {
	
	/**
	 * 数据集ID
	 */
	private long volumeId;
	
	/**
	 * 数据集名称
	 */
	private String volumeName;
}
