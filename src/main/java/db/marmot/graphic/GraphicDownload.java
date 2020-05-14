/*
 * Copyright (c) 2019. shaokang All Rights Reserved
 */

package db.marmot.graphic;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import db.marmot.enums.DownloadStatus;
import db.marmot.enums.GraphicType;
import db.marmot.repository.validate.Validators;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class GraphicDownload {
	
	/**
	 * 自增序列
	 */
	private long downloadId;
	
	/**
	 * 操作员
	 */
	@NotBlank
	private String founderId;
	
	/**
	 * 文件名称
	 */
	@NotBlank
	private String fileName;
	
	/**
	 * 图表ID
	 */
	private long graphicId;
	
	/**
	 * 数据集ID
	 */
	private long volumeId;
	
	/**
	 * 图表类型
	 */
	private GraphicType graphicType;
	
	/**
	 * 图表
	 */
	private Graphic graphic;
	
	/**
	 * 文件路径
	 */
	@NotBlank
	private String fileUrl;
	
	/**
	 * 下载路径
	 */
	@NotBlank
	private String downloadUrl;
	
	/**
	 * 文件状态
	 */
	@NotNull
	private DownloadStatus status;
	
	/**
	 * 说明
	 */
	@Size(max = 1024)
	private String memo;
	
	public GraphicDownload() {
		downloadWait();
	}
	
	/**
	 * 是否等待下载
	 * @return
	 */
	public boolean isDownloadWait() {
		return this.status == DownloadStatus.download_wait;
	}
	
	/**
	 * 是否下载中
	 * @return
	 */
	public boolean isDownloadIng() {
		return this.status == DownloadStatus.download_ing;
	}
	
	/**
	 * 等待下载
	 */
	public void downloadWait() {
		this.memo = "已加入下载队列";
		this.status = DownloadStatus.download_wait;
	}
	
	/**
	 * 等待下载
	 */
	public GraphicDownload downloadIng() {
		this.memo = "正在下载";
		this.status = DownloadStatus.download_ing;
		return this;
	}
	
	/**
	 * 下载成功
	 */
	public void downloadSuccess() {
		this.memo = "下载成功";
		this.status = DownloadStatus.download_success;
	}
	
	/**
	 * 下载失败
	 * @param memo
	 */
	public void downloadFail(String memo) {
		this.status = DownloadStatus.download_success;
		this.memo = StringUtils.isNotBlank(memo) ? memo.length() > 128 ? memo.substring(0, 128) : memo : "系统内部错误";
	}
	
	public void validateGraphicDownload() {
		Validators.assertJSR303(this);
		if (graphicId == 0) {
			Validators.notNull(this.graphicType, "graphicType不能为空");
			Validators.notNull(this.graphic, "graphic不能为空");
		}
	}
}
