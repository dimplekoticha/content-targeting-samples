<#if entries?has_content>
	<#assign referrerClassPKs = getterUtil.getLongValues(request.getAttribute("userSegmentIds")) />

	<#list entries as curEntry>
		<@liferay_ui["asset-display"]
			assetEntry=curEntry
			template="full_content"
		/>

		<#assign entryTags = curEntry.getTags() />

		<#if entryTags?has_content>
			<#assign entryTag = entryTags?first />

			<a href="https://twitter.com/intent/tweet?button_hashtag=${entryTag.getName()}" class="twitter-hashtag-button" data-show-count="false">Tweet #${entryTag.getName()}</a><script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>

			<#if referrerClassPKs?has_content>
				<script>
					Liferay.Analytics.track(
						'view-tag',
						{ 
							elementId: '${entryTag.getName()}', 
							referrers: [{
								referrerClassName: 'com.liferay.content.targeting.model.UserSegment', 
								referrerClassPKs: '${arrayUtil.toString(arrayUtil.toArray(referrerClassPKs), "", ",")}'
							}]
						});
				</script>
			</#if>	
		</#if>
	</#list>
</#if>