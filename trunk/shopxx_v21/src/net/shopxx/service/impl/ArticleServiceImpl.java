package net.shopxx.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import net.shopxx.bean.Pager;
import net.shopxx.dao.ArticleDao;
import net.shopxx.entity.Article;
import net.shopxx.entity.ArticleCategory;
import net.shopxx.service.ArticleService;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Version;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springmodules.cache.annotations.CacheFlush;
import org.springmodules.cache.annotations.Cacheable;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * Service实现类 - 文章
 * ============================================================================
 * 版权所有 2008-2010 长沙鼎诚软件有限公司,并保留所有权利。
 * ----------------------------------------------------------------------------
 * 提示：在未取得SHOP++商业授权之前,您不能将本软件应用于商业用途,否则SHOP++将保留追究的权力。
 * ----------------------------------------------------------------------------
 * 官方网站：http://www.shopxx.net
 * ----------------------------------------------------------------------------
 * KEY: SHOPXX6F9A5712DEB059F8AE03AC3C01C9A21B
 * ============================================================================
 */

@Service("articleServiceImpl")
public class ArticleServiceImpl extends BaseServiceImpl<Article, String> implements ArticleService {
	
	private long refreshTime = System.currentTimeMillis();

	@Resource(name = "cacheManager")
	private GeneralCacheAdministrator cacheManager;
	@Resource(name = "articleDaoImpl")
	private ArticleDao articleDao;

	@Resource(name = "articleDaoImpl")
	public void setBaseDao(ArticleDao articleDao) {
		super.setBaseDao(articleDao);
	}

	@Cacheable(modelId = "articleCaching")
	@Transactional(readOnly = true)
	public List<Article> getArticleList(ArticleCategory articleCategory, String type, boolean isContainChildren, Integer maxResults) {
		return articleDao.getArticleList(articleCategory, type, isContainChildren, maxResults);
	}
	
	@Transactional(readOnly = true)
	public List<Article> getArticleList(ArticleCategory articleCategory, Date beginDate, Date endDate, Integer firstResult, Integer maxResults) {
		return articleDao.getArticleList(articleCategory, beginDate, endDate, firstResult, maxResults);
	}
	
	@Transactional(readOnly = true)
	public Pager getArticlePager(ArticleCategory articleCategory, Pager pager) {
		return articleDao.getArticlePager(articleCategory, pager);
	}
	
	public Pager search(Pager pager) {
		String keyword = pager.getKeyword();
		int pageSize = pager.getPageSize();
		int pageNumber = pager.getPageNumber();
		try {
			Query keywordQuery = MultiFieldQueryParser.parse(Version.LUCENE_31, "\"" + QueryParser.escape(keyword) + "\"", new String[] {"title", "content"}, new Occur[] {Occur.SHOULD, Occur.SHOULD}, new IKAnalyzer());
			Query isPublicationQuery = new TermQuery(new Term("isPublication", "true"));
			BooleanQuery luceneQuery = new BooleanQuery();
			luceneQuery.add(keywordQuery, Occur.MUST);
			luceneQuery.add(isPublicationQuery, Occur.MUST);
			FullTextSession fullTextSession = Search.getFullTextSession(articleDao.getSession());
			FullTextQuery query = fullTextSession.createFullTextQuery(luceneQuery, Article.class);
			pager.setTotalCount(query.getResultSize());
			int pageCount = pager.getPageCount();
			if (pageNumber > pageCount) {
				if (pageCount == 0) {
					pageNumber = 1;
				} else {
					pageNumber = pageCount;
				}
				pager.setPageNumber(pageNumber);
			}
			query.setSort(new Sort(new SortField[] {new SortField("isTop", SortField.STRING, true), new SortField(null, SortField.SCORE), new SortField("createDate", SortField.LONG, true)}));
			query.setFirstResult((pageNumber - 1) * pageSize);
			query.setMaxResults(pageSize);
			pager.setResult(query.list());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pager;
	}
	
	public void index(Article article) {
		FullTextSession fullTextSession = Search.getFullTextSession(articleDao.getSession());
		fullTextSession.index(article);
	}
	
	public void index() {
		List<Article> articles = articleDao.getAllList();
		FullTextSession fullTextSession = Search.getFullTextSession(articleDao.getSession());
		for (Article article : articles) {
			fullTextSession.index(article);
		}
	}
	
	@Transactional
	public Integer viewHits(String id) {
		String cacheKey = Article.ARTICLE_HITS_CACHE_KEY_PREFIX + id;
		Integer hits = null;
		boolean updateSucceeded = false;
		try {
			hits = (Integer) cacheManager.getFromCache(cacheKey) + 1;
			cacheManager.putInCache(cacheKey, hits);
			updateSucceeded = true;
			
			long currentTime = System.currentTimeMillis();
			if (currentTime > refreshTime + Article.ARTICLE_HITS_CACHE_TIME * 1000) {
				Article article = articleDao.load(id);
				article.setHits(hits);
				articleDao.update(article);
				refreshTime = currentTime;
			}
		} catch (NeedsRefreshException needsRefreshException) {
			Article article = articleDao.load(id);
			hits = article.getHits() + 1;
			cacheManager.putInCache(cacheKey, hits);
			updateSucceeded = true;
		} finally {
            if (!updateSucceeded) {
            	cacheManager.cancelUpdate(cacheKey);
            }
        }
		return hits;
	}

	@Override
	@CacheFlush(modelId = "articleFlushing")
	public void delete(Article article) {
		super.delete(article);
	}

	@Override
	@CacheFlush(modelId = "articleFlushing")
	public void delete(String id) {
		super.delete(id);
	}

	@Override
	@CacheFlush(modelId = "articleFlushing")
	public void delete(String[] ids) {
		super.delete(ids);
	}

	@Override
	@CacheFlush(modelId = "articleFlushing")
	public String save(Article article) {
		return super.save(article);
	}

	@Override
	@CacheFlush(modelId = "articleFlushing")
	public void update(Article article) {
		super.update(article);
	}
	
}