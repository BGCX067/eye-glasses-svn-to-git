package net.shopxx.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import net.shopxx.bean.PageTemplateConfig;
import net.shopxx.util.TemplateConfigUtil;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * 实体类 - 文章
 * ============================================================================
 * 版权所有 2008-2010 长沙鼎诚软件有限公司,并保留所有权利。
 * ----------------------------------------------------------------------------
 * 提示：在未取得SHOP++商业授权之前,您不能将本软件应用于商业用途,否则SHOP++将保留追究的权力。
 * ----------------------------------------------------------------------------
 * 官方网站：http://www.shopxx.net
 * ----------------------------------------------------------------------------
 * KEY: SHOPXX5CAA6FDAF2A5662FADB5F15AD00B2070
 * ============================================================================
 */

@Indexed
@Entity
public class Article extends BaseEntity {

	private static final long serialVersionUID = 1475773294701585482L;

	public static final String ARTICLE_HITS_CACHE_KEY_PREFIX = "ARTICLE_HITS";// 文章点击数缓存Key前缀
	public static final int ARTICLE_HITS_CACHE_TIME = 600;// 文章点击数缓存有效期
	public static final int DEFAULT_ARTICLE_LIST_PAGE_SIZE = 20;// 文章列表默认每页显示数
	private static final int PAGE_CONTENT_LENGTH = 1000;// 内容分页长度
	public static final String NEXT_PAGE_SIGN = "{nextPage}";// 内容分页标记

	private String title;// 标题
	private String author;// 作者
	private String content;// 内容
	private String metaKeywords;// 页面关键词
	private String metaDescription;// 页面描述
	private Boolean isPublication;// 是否发布
	private Boolean isTop;// 是否置顶
	private Boolean isRecommend;// 是否为推荐文章
	private Integer pageCount;// 文章页数
	private String htmlPath;// HTML静态文件路径(首页)
	private Integer hits;// 点击数
	
	private ArticleCategory articleCategory;// 文章分类

	@Field(store = Store.YES, index = Index.TOKENIZED, analyzer = @Analyzer(impl = IKAnalyzer.class))
	@Column(nullable = false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Field(store = Store.YES, index = Index.NO)
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	@Field(store = Store.YES, index = Index.TOKENIZED, analyzer = @Analyzer(impl = IKAnalyzer.class))
	@Lob
	@Column(nullable = false)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(length = 3000)
	public String getMetaKeywords() {
		return metaKeywords;
	}

	public void setMetaKeywords(String metaKeywords) {
		this.metaKeywords = metaKeywords;
	}

	@Column(length = 3000)
	public String getMetaDescription() {
		return metaDescription;
	}

	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}

	@Field(store = Store.YES, index = Index.UN_TOKENIZED)
	@Column(nullable = false)
	public Boolean getIsPublication() {
		return isPublication;
	}

	public void setIsPublication(Boolean isPublication) {
		this.isPublication = isPublication;
	}

	@Field(store = Store.YES, index = Index.UN_TOKENIZED)
	@Column(nullable = false)
	public Boolean getIsTop() {
		return isTop;
	}

	public void setIsTop(Boolean isTop) {
		this.isTop = isTop;
	}

	@Field(store = Store.YES, index = Index.UN_TOKENIZED)
	@Column(nullable = false)
	public Boolean getIsRecommend() {
		return isRecommend;
	}

	public void setIsRecommend(Boolean isRecommend) {
		this.isRecommend = isRecommend;
	}

	@Column(nullable = false)
	public Integer getPageCount() {
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	@Field(store = Store.YES, index = Index.NO)
	@Column(nullable = false, updatable = false)
	public String getHtmlPath() {
		return htmlPath;
	}

	public void setHtmlPath (String htmlPath) {
		this.htmlPath = htmlPath;
	}
	
	@Column(nullable = false)
	public Integer getHits() {
		return hits;
	}

	public void setHits(Integer hits) {
		this.hits = hits;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@ForeignKey(name = "fk_article_article_category")
	public ArticleCategory getArticleCategory() {
		return articleCategory;
	}

	public void setArticleCategory(ArticleCategory articleCategory) {
		this.articleCategory = articleCategory;
	}
	
	// 保存处理
	@Override
	@Transient
	public void onSave() {
		if (StringUtils.isEmpty(author)) {
			author = null;
		}
		if (isPublication == null) {
			isPublication = false;
		}
		if (isTop == null) {
			isTop = false;
		}
		if (isRecommend == null) {
			isRecommend = false;
		}
		hits = 0;
		PageTemplateConfig pageTemplateConfig = TemplateConfigUtil.getPageTemplateConfig(PageTemplateConfig.ARTICLE_CONTENT);
		htmlPath = pageTemplateConfig.getHtmlRealPath();
		pageCount = getPageContentList().size();
	}
	
	// 更新处理
	@Override
	@Transient
	public void onUpdate() {
		if (StringUtils.isEmpty(author)) {
			author = null;
		}
		if (isPublication == null) {
			isPublication = false;
		}
		if (isTop == null) {
			isTop = false;
		}
		if (isRecommend == null) {
			isRecommend = false;
		}
		if (hits == null || hits < 0) {
			hits = 0;
		}
		pageCount = getPageContentList().size();
	}
	
	// 获取HTML静态文件路径
	@Transient
	public List<String> getHtmlPathList() {
		ArrayList<String> htmlPathList = new ArrayList<String>();
		String prefix = StringUtils.substringBeforeLast(htmlPath, ".");
		String extension = StringUtils.substringAfterLast(htmlPath, ".");
		htmlPathList.add(htmlPath);
		for (int i = 2; i <= pageCount; i++) {
			htmlPathList.add(prefix + "_" + i + "." + extension);
		}
		return htmlPathList;
	}

	/**
	 * 获取文本内容
	 */
	@Transient
	public String getContentText() {
		Document document = Jsoup.parse(content);
		return document.text();
	}

	/**
	 * 获取分页内容
	 */
	@Transient
	public List<String> getPageContentList() {
		if (content.contains(NEXT_PAGE_SIGN)) {
			return Arrays.asList(content.split(NEXT_PAGE_SIGN));
		} else {
			List<String> pageContentList = new ArrayList<String>();
			Document document = Jsoup.parse(content);
			List<Node> children = document.body().childNodes();
			if (children != null) {
				int textLength = 0;
				StringBuffer html = new StringBuffer();
				for (Node node : children) {
					if (node instanceof Element) {
						Element element = (Element) node;
						html.append(element.outerHtml());
						textLength += element.text().length();
						if (textLength >= PAGE_CONTENT_LENGTH) {
							pageContentList.add(html.toString());
							textLength = 0;
							html.setLength(0);
						}
					} else if (node instanceof TextNode) {
						TextNode textNode = (TextNode) node;
						String text = textNode.text();
						Pattern pattern = Pattern.compile("[,;\\.!?，；。！？]");
						String[] contents = pattern.split(text);
						Matcher matcher = pattern.matcher(text);
						for (String content : contents) {
							if (matcher.find()) {
								content += matcher.group();
							}
							html.append(content);
							textLength += content.length();
							if (textLength >= PAGE_CONTENT_LENGTH) {
								pageContentList.add(html.toString());
								textLength = 0;
								html.setLength(0);
							}
						}
					}
				}
				String pageContent = html.toString();
				if (StringUtils.isNotEmpty(pageContent)) {
					pageContentList.add(pageContent);
				}
			}
			return pageContentList;
		}
	}

}