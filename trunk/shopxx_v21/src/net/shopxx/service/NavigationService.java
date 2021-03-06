package net.shopxx.service;

import java.util.List;

import net.shopxx.entity.Navigation;
import net.shopxx.entity.Navigation.NavigationPosition;

/**
 * Service接口 - 导航
 * ============================================================================
 * 版权所有 2008-2010 长沙鼎诚软件有限公司,并保留所有权利。
 * ----------------------------------------------------------------------------
 * 提示：在未取得SHOP++商业授权之前,您不能将本软件应用于商业用途,否则SHOP++将保留追究的权力。
 * ----------------------------------------------------------------------------
 * 官方网站：http://www.shopxx.net
 * ----------------------------------------------------------------------------
 * KEY: SHOPXXE9613E05E492035FCAAF274063C63401
 * ============================================================================
 */

public interface NavigationService extends BaseService<Navigation, String> {

	/**
	 * 获取导航集合（只包含isVisible=true的对象）
	 * 
	 * @param navigationPosition
	 *            导航位置,null表示无限制
	 * 
	 * @param maxResults
	 *            最大结果数,null表示无限制
	 * 
	 * @return 顶级商品分类集合
	 * 
	 */
	public List<Navigation> getNavigationList(NavigationPosition navigationPosition, Integer maxResults);

}