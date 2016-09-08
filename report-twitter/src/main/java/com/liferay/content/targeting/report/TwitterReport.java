package com.liferay.content.targeting.report;

import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.content.targeting.analytics.service.AnalyticsEventLocalService;
import com.liferay.content.targeting.api.model.BaseJSPReport;
import com.liferay.content.targeting.api.model.Report;
import com.liferay.content.targeting.model.Campaign;
import com.liferay.content.targeting.model.ReportInstance;
import com.liferay.content.targeting.service.ReportInstanceLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.Date;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import javax.servlet.ServletContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Eduardo Garcia
 */
@Component(immediate = true, service = Report.class)
public class TwitterReport extends BaseJSPReport {

	@Activate
	@Override
	public void activate() {
		super.activate();
	}

	@Deactivate
	@Override
	public void deActivate() {
		super.deActivate();
	}

	@Override
	public String getReportType() {
		return Campaign.class.getName();
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	public String processEditReport(
			PortletRequest portletRequest, PortletResponse portletResponse,
			ReportInstance reportInstance)
		throws Exception {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		String tagName = ParamUtil.getString(portletRequest, "tagName");

		jsonObject.put("tagName", tagName);

		return jsonObject.toString();
	}

	@Reference(unbind = "-")
	public void setReportInstanceLocalService(
		ReportInstanceLocalService reportInstanceLocalService) {

		_reportInstanceLocalService = reportInstanceLocalService;
	}

	@Override
	@Reference(target = "(osgi.web.symbolicname=com.liferay.content.targeting.report.twitter)", unbind = "-")
	public void setServletContext(ServletContext servletContext) {
		super.setServletContext(servletContext);
	}

	@Override
	public void updateReport(ReportInstance reportInstance) {
		try {
			if (reportInstance != null) {
				reportInstance.setModifiedDate(new Date());

				_reportInstanceLocalService.updateReportInstance(
					reportInstance);
			}
		}
		catch (Exception e) {
			_log.error("Unable to update report", e);
		}
	}

	@Override
	protected void populateContext(
		ReportInstance reportInstance, Map<String, Object> context) {

		String tagName = null;
		long tagUsageCount = 0;
		int tagViewCount = 0;
		int tagPositiveCount = 0;
		int tagNegativeCount = 0;

		if (reportInstance != null) {
			try {
				JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
					reportInstance.getTypeSettings());

				tagName = jsonObject.getString("tagName");

				tagViewCount = _getTagViewCount(tagName);

				tagUsageCount = _getTagUsageCount(
					reportInstance.getCompanyId(), reportInstance.getGroupId(), 
					reportInstance.getUserId(), tagName);

				tagPositiveCount = _getTagTwitterCount("#" + tagName + " :)");

				tagNegativeCount = _getTagTwitterCount("#" + tagName + " :(");
			}
			catch (Exception e) {
				_log.error(e);
			}
		}

		context.put("tagName", tagName);
		context.put("tagNegativeCount", tagNegativeCount);
		context.put("tagPositiveCount", tagPositiveCount);
		context.put("tagUsageCount", tagUsageCount);
		context.put("tagViewCount", tagViewCount);
	}

	@Override
	protected void populateEditContext(
		ReportInstance reportInstance, Map<String, Object> context) {

		populateContext(reportInstance, context);
	}

	@Reference(unbind = "-")
	protected void setAnalyticsEventLocalService(
		AnalyticsEventLocalService analyticsEventLocalService) {

		_analyticsEventLocalService = analyticsEventLocalService;
	}

	@Reference(unbind = "-")
	protected void setAssetEntryLocalService(
		AssetEntryLocalService assetEntryLocalService) {

		_assetEntryLocalService = assetEntryLocalService;
	}
	
	@Reference(unbind = "-")
	protected void setAssetTagLocalService(
		AssetTagLocalService assetTagLocalService) {

		_assetTagLocalService = assetTagLocalService;
	}

	private int _getTagTwitterCount(String tagSearchQuery) {
		int count = 0;

		ConfigurationBuilder cb = new ConfigurationBuilder();

		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(_CONSUMER_KEY);
		cb.setOAuthConsumerSecret(_CONSUMER_SECRET);
		cb.setOAuthAccessToken(_ACCESS_KEY);
		cb.setOAuthAccessTokenSecret(_ACCESS_SECRET);

		try {
			TwitterFactory twitterFactory = new TwitterFactory(cb.build());

			Twitter twitter = twitterFactory.getInstance();

			Query query = new Query(tagSearchQuery);
			
			query.setCount(100);

			QueryResult result = twitter.search(query);
			
			count = result.getTweets().size();
		}
		catch (TwitterException te) {
			_log.error("Cannot retrieve data from Twitter");
		}

		return count;
	}

	private long _getTagUsageCount(
		long companyId, long groupId, long userId, String tagName) {
		
		if (_assetTagLocalService.fetchTag(groupId, tagName) == null) {
			return 0;
		}

		int[] statuses = new int[] {
			WorkflowConstants.STATUS_APPROVED, WorkflowConstants.STATUS_PENDING,
			WorkflowConstants.STATUS_SCHEDULED
		};

		return _assetEntryLocalService.searchCount(
			companyId, null, userId, null, 0, null, null, null, null, tagName,
			true, true, statuses, false);
	}

	private int _getTagViewCount(String tagName) {
		try {
			return _analyticsEventLocalService.getAnalyticsEventsCount(
				tagName, "view-tag", new Date(0));
		}
		catch (PortalException pe) {
			_log.error(pe);

			return 0;
		}
	}

	private static final String _ACCESS_KEY = "";

	private static final String _ACCESS_SECRET = "";

	private static final String _CONSUMER_KEY = "";

	private static final String _CONSUMER_SECRET = "";

	private static final Log _log = LogFactoryUtil.getLog(TwitterReport.class);

	private AnalyticsEventLocalService _analyticsEventLocalService;
	private AssetEntryLocalService _assetEntryLocalService;
	private AssetTagLocalService _assetTagLocalService;
	private ReportInstanceLocalService _reportInstanceLocalService;

}