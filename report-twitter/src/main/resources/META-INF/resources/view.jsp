<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>

<%@ page import="com.liferay.portal.kernel.model.*" %>
<%@ page import="com.liferay.portal.kernel.model.impl.*" %>
<%@ page import="com.liferay.portal.kernel.util.GetterUtil" %>
<%@ page import="com.liferay.portal.kernel.util.HtmlUtil" %>
<%@ page import="com.liferay.portal.kernel.util.PortalUtil" %>

<%@ page import="java.util.Map" %>

<liferay-frontend:defineObjects />

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
Map<String, Object> context = (Map<String, Object>)request.getAttribute("context");

String tagName = GetterUtil.getString(context.get("tagName"));
int tagNegativeCount = GetterUtil.getInteger(context.get("tagNegativeCount"));
int tagPositiveCount = GetterUtil.getInteger(context.get("tagPositiveCount"));
long tagUsageCount = GetterUtil.getLong(context.get("tagUsageCount"));
int tagViewCount = GetterUtil.getInteger(context.get("tagViewCount"));
%>

<aui:nav-bar markupView="lexicon">
	<aui:nav cssClass="navbar-nav">
		<aui:nav-item href="<%= currentURL %>" label="<%= tagName %>" selected="<%= true %>" />
	</aui:nav>
</aui:nav-bar>

<section class="container-fluid-1280">
	<div class="col-md-6">
		<canvas height="400" id="barChart" width="400"></canvas>
	</div>
	<div class="col-md-6">
		<canvas height="400" id="radarChart" width="400"></canvas>
	</div>
</section>

<script data-senna-track="temporary" type="text/javascript">
	Loader.addModule({
		name: 'chart',
		fullPath: '<%= HtmlUtil.escape(PortalUtil.getStaticResourceURL(request, themeDisplay.getCDNHost() + "/o/content-targeting-report-twitter/js/Chart.js")) %>',
		dependencies: [],
		anonymous: true
	});

	require('chart', function(Chart) {
		var data = {
			labels: ["Tag Usage", "Tag Views", "Tag Positive", "Tag Negative"],
			datasets: [
				{
					backgroundColor: [
						'rgba(255, 99, 132, 0.2)',
						'rgba(54, 162, 235, 0.2)',
						'rgba(255, 206, 86, 0.2)',
						'rgba(75, 192, 192, 0.2)'
					],
					borderColor: [
						'rgba(255,99,132,1)',
						'rgba(54, 162, 235, 1)',
						'rgba(255, 206, 86, 1)',
						'rgba(75, 192, 192, 1)'
					],
					borderWidth: 1,
					pointBackgroundColor: "rgba(179,181,198,1)",
					pointBorderColor: "#fff",
					pointHoverBackgroundColor: "#fff",
					pointHoverBorderColor: "rgba(179,181,198,1)",
					data: [<%= tagUsageCount %>, <%= tagViewCount %>, <%= tagPositiveCount %>, <%= tagNegativeCount %>]
				}
			]
		};

		var ctx = document.getElementById("barChart");
		var myChart = new Chart(ctx, {
			type: 'bar',
			data: data,
			options: {
				legend: {display: false},
				scales: {
					yAxes: [{
						ticks: {
							beginAtZero:true
						}
					}]
				}
			}
		});

		var ctx = document.getElementById("radarChart");

		var myRadarChart = new Chart(ctx, {
			type: 'radar',
			data: data,
			options: {
				legend: {display: false},
				scale: {
					reverse: false,
					ticks: {
						beginAtZero: true
					}
				}
			}
		});

	});
</script>