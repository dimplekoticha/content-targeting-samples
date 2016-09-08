<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %>
<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@ page import="com.liferay.portal.kernel.util.GetterUtil" %>

<%@ page import="java.util.Map" %>

<liferay-frontend:defineObjects />

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
Map<String, Object> context = (Map<String, Object>)request.getAttribute("context");
%>

<aui:fieldset-group markupView="lexicon">
	<aui:fieldset>
		<div class="button-holder">
			<liferay-ui:asset-tags-selector
				allowAddEntry="<%= false %>"
				curTags='<%= GetterUtil.getString(context.get("tagName"), "") %>'
				hiddenInput="tagName"
				id="assetTagsSelector"
			/>
		</div>
	</aui:fieldset>
</aui:fieldset-group>