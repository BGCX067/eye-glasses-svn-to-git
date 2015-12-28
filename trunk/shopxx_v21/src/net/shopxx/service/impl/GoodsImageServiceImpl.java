package net.shopxx.service.impl;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import net.shopxx.bean.GoodsImage;
import net.shopxx.bean.Setting;
import net.shopxx.service.GoodsImageService;
import net.shopxx.util.CommonUtil;
import net.shopxx.util.ImageUtil;
import net.shopxx.util.SettingUtil;

import org.apache.commons.io.FileUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

/**
 * Service实现类 - 商品图片
 * ============================================================================
 * 版权所有 2008-2010 长沙鼎诚软件有限公司,并保留所有权利。
 * ----------------------------------------------------------------------------
 * 提示：在未取得SHOP++商业授权之前,您不能将本软件应用于商业用途,否则SHOP++将保留追究的权力。
 * ----------------------------------------------------------------------------
 * 官方网站：http://www.shopxx.net
 * ----------------------------------------------------------------------------
 * KEY: SHOPXX69F2EBC7A6A837BABDA5487C75D38611
 * ============================================================================
 */

@Service("goodsImageServiceImpl")
public class GoodsImageServiceImpl implements GoodsImageService, ServletContextAware {
	
	@Resource(name = "taskExecutor")
	private TaskExecutor taskExecutor;
	
	private ServletContext servletContext;
	
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	/**
	 * 添加图片处理任务
	 * @param sourceFile 原图片文件
	 * @param bigFile 图片文件(大)
	 * @param smallFile 图片文件(小)
	 * @param thumbnailFile 图片文件(缩略)
	 * @param watermarkFile 水印文件
	 * @param setting 系统设置
	 */
	private void addBuildTask(final File sourceFile, final File bigFile, final File smallFile, final File thumbnailFile, final File watermarkFile, final Setting setting) {
		try {
			taskExecutor.execute(
				new Runnable() {
					public void run() {
						ImageUtil.zoom(sourceFile, bigFile, setting.getBigGoodsImageWidth(), setting.getBigGoodsImageHeight());
						ImageUtil.addWatermark(bigFile, bigFile, watermarkFile, setting.getWatermarkPosition(), setting.getWatermarkAlpha());
						ImageUtil.zoom(sourceFile, smallFile, setting.getSmallGoodsImageWidth(), setting.getSmallGoodsImageHeight());
						ImageUtil.addWatermark(smallFile, smallFile, watermarkFile, setting.getWatermarkPosition(), setting.getWatermarkAlpha());
						ImageUtil.zoom(sourceFile, thumbnailFile, setting.getThumbnailGoodsImageWidth(), setting.getThumbnailGoodsImageHeight());
					}
				}
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public GoodsImage buildGoodsImage(File goodsImageFile) {
		Setting setting = SettingUtil.getSetting();
		String uuid = CommonUtil.getUUID();
		
		GoodsImage goodsImage = new GoodsImage();
		goodsImage.setId(uuid);
		goodsImage.setPath(setting.getImageUploadRealPath());
		goodsImage.setSourceImageFormatName(ImageUtil.getFormatName(goodsImageFile));
		
		File sourceGoodsImageFile = new File(servletContext.getRealPath(goodsImage.getSourceImagePath()));
		File bigGoodsImageFile = new File(servletContext.getRealPath(goodsImage.getBigImagePath()));
		File smallGoodsImageFile = new File(servletContext.getRealPath(goodsImage.getSmallImagePath()));
		File thumbnailGoodsImageFile = new File(servletContext.getRealPath(goodsImage.getThumbnailImagePath()));
		File watermarkImageFile = new File(servletContext.getRealPath(setting.getWatermarkImagePath()));

		File sourceGoodsImageParentFile = sourceGoodsImageFile.getParentFile();
		File bigGoodsImageParentFile = bigGoodsImageFile.getParentFile();
		File smallGoodsImageParentFile = smallGoodsImageFile.getParentFile();
		File thumbnailGoodsImageParentFile = thumbnailGoodsImageFile.getParentFile();

		if (!sourceGoodsImageParentFile.isDirectory()) {
			sourceGoodsImageParentFile.mkdirs();
		}
		if (!bigGoodsImageParentFile.isDirectory()) {
			bigGoodsImageParentFile.mkdirs();
		}
		if (!smallGoodsImageParentFile.isDirectory()) {
			smallGoodsImageParentFile.mkdirs();
		}
		if (!thumbnailGoodsImageParentFile.isDirectory()) {
			thumbnailGoodsImageParentFile.mkdirs();
		}
		
		try {
			FileUtils.copyFile(goodsImageFile, sourceGoodsImageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		addBuildTask(sourceGoodsImageFile, bigGoodsImageFile, smallGoodsImageFile, thumbnailGoodsImageFile, watermarkImageFile, setting);
		return goodsImage;
	}

}