package net.shopxx.dao.impl;

import java.util.Set;

import net.shopxx.dao.MemberDao;
import net.shopxx.entity.Member;
import net.shopxx.entity.Order;
import net.shopxx.entity.Payment;

import org.springframework.stereotype.Repository;

/**
 * Dao实现类 - 会员
 * ============================================================================
 * 版权所有 2008-2010 长沙鼎诚软件有限公司,并保留所有权利。
 * ----------------------------------------------------------------------------
 * 提示：在未取得SHOP++商业授权之前,您不能将本软件应用于商业用途,否则SHOP++将保留追究的权力。
 * ----------------------------------------------------------------------------
 * 官方网站：http://www.shopxx.net
 * ----------------------------------------------------------------------------
 * KEY: SHOPXXAC8DDA6F41F51B7BA7B541180E9FE7F7
 * ============================================================================
 */

@Repository("memberDaoImpl")
public class MemberDaoImpl extends BaseDaoImpl<Member, String> implements MemberDao {

	public boolean isExistByUsername(String username) {
		String hql = "from Member as members where lower(members.username) = lower(:username)";
		Member member = (Member) getSession().createQuery(hql).setParameter("username", username).uniqueResult();
		if (member != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public Member getMemberByUsername(String username) {
		String hql = "from Member as members where lower(members.username) = lower(:username)";
		return (Member) getSession().createQuery(hql).setParameter("username", username).uniqueResult();
	}
	
	// 关联处理
	@Override
	public void delete(Member member) {
		Set<Order> orderSet = member.getOrderSet();
		if (orderSet != null) {
			for (Order order : orderSet) {
				order.setMember(null);
			}
		}
		
		Set<Payment> paymentSet = member.getPaymentSet();
		if (paymentSet != null) {
			for (Payment payment : paymentSet) {
				payment.setMember(null);
			}
		}
		
		super.delete(member);
	}

	// 关联处理
	@Override
	public void delete(String id) {
		Member member = load(id);
		this.delete(member);
	}

	// 关联处理
	@Override
	public void delete(String[] ids) {
		for (String id : ids) {
			Member member = load(id);
			this.delete(member);
		}
	}

}