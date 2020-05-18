package db.marmot.volume.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.StringJoiner;

/**
 * @author shaokang
 */
@Setter
@Getter
public class DataVolumeRequest {
	
	/**
	 * 数据集ID
	 */
	private String volumeCode;
	
	/**
	 * 数据集名称
	 */
	private String volumeName;

	@Override
	public String toString() {
		return new StringJoiner(", ", DataVolumeRequest.class.getSimpleName() + "[", "]")
				.add("volumeCode='" + volumeCode + "'")
				.add("volumeName='" + volumeName + "'")
				.toString();
	}
}
