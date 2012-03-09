<%@ taglib uri="http://www.zkoss.org/dsp/web/core" prefix="c" %>
.tooltip {
	padding-top: 5px;
	position: absolute;
	top: 0;
	left: 0;
	z-index: 99999;
}

.tooltip .tooltip-anchor {
	<c:if test="${zk.ie <= 8}">
	border-bottom-color: rgb(129,195,253);
	</c:if>
	border-bottom-color: rgba(129,195,253,0.35);
	margin-left: 30px; margin-top: -12px;
}
.tooltip-content {
	font-size: 11px;
	color: #fff;
	text-shadow: 0 0 2px #000;
	padding: 10px 8px 8px;
	<c:if test="${zk.ie <= 8}">
	border: 1px solid rgb(197,219,229);
	opacity: .92;
	-moz-opacity: .92;
	filter: alpha(opacity=92);
	background-color: rgb(129,195,253);
	</c:if>	
	border: 1px solid rgba(129,195,253,0.25);
	background: url(data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiA/Pgo8c3ZnIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgdmlld0JveD0iMCAwIDEgMSIgcHJlc2VydmVBc3BlY3RSYXRpbz0ibm9uZSI+CiAgPGxpbmVhckdyYWRpZW50IGlkPSJncmFkLXVjZ2ctZ2VuZXJhdGVkIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeDE9IjAlIiB5MT0iMCUiIHgyPSIwJSIgeTI9IjEwMCUiPgogICAgPHN0b3Agb2Zmc2V0PSIwJSIgc3RvcC1jb2xvcj0iI2IyZTFmZiIgc3RvcC1vcGFjaXR5PSIwLjkyIi8+CiAgICA8c3RvcCBvZmZzZXQ9IjEwMCUiIHN0b3AtY29sb3I9IiM2NmI2ZmMiIHN0b3Atb3BhY2l0eT0iMC45MiIvPgogIDwvbGluZWFyR3JhZGllbnQ+CiAgPHJlY3QgeD0iMCIgeT0iMCIgd2lkdGg9IjEiIGhlaWdodD0iMSIgZmlsbD0idXJsKCNncmFkLXVjZ2ctZ2VuZXJhdGVkKSIgLz4KPC9zdmc+);
	background: -moz-linear-gradient(top,  rgba(178,225,255,0.92) 0%, rgba(102,182,252,0.92) 100%);
	background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,rgba(178,225,255,0.92)), color-stop(100%,rgba(102,182,252,0.92)));
	background: -webkit-linear-gradient(top,  rgba(178,225,255,0.92) 0%,rgba(102,182,252,0.92) 100%);
	background: -o-linear-gradient(top,  rgba(178,225,255,0.92) 0%,rgba(102,182,252,0.92) 100%);
	background: -ms-linear-gradient(top,  rgba(178,225,255,0.92) 0%,rgba(102,182,252,0.92) 100%);
	background: linear-gradient(top,  rgba(178,225,255,0.92) 0%,rgba(102,182,252,0.92) 100%);
	filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ebb2e1ff', endColorstr='#eb66b6fc',GradientType=0 );
	border-radius: 3px;
	-webkit-border-radius: 3px;
	-moz-border-radius: 3px;
	box-shadow: 0 0 3px #C5C5C5;
	-webkit-box-shadow: 0 0 3px #C5C5C5;
	-moz-box-shadow: 0 0 3px #C5C5C5;
}
.tooltip-anchor, .tooltip-anchor-inner {
	position: absolute;
	border-color: transparent;
	border-style: solid;
	border-width: 6px;
	height: 0;
	width: 0;
	<c:if test="${zk.ie <= 8}">
	border-bottom-color: rgb(129,195,253);
	</c:if>
	border-bottom-color: rgba(129,195,253, 0.92);
    margin-left: -6px;
    margin-top: -5px;
}
<%-- images --%>
.images_0, .images_1, .images_2, .images_3, .images_4, .images_5, .images_6,
.images_7, .images_8, .images_9, .images_10, .images_11, .images_12, .images_13,
.images_14, .images_15, .images_16, .images_17, .images_18, .images_19, .images_20,
.images_21, .images_22, .images_23, .images_24, .images_25, .images_26, .images_27 {
	padding-left: 18px;
	background: url(${c:encodeThemeURL('/images/aim_16.png')});
	background-position: 0 0;
    background-repeat: no-repeat;
}
.images_1 {
	background-image: url(${c:encodeThemeURL('/images/amazon_16.png')});
}
.images_2 {
	background-image: url(${c:encodeThemeURL('/images/android_16.png')});
}
.images_3 {
	background-image: url(${c:encodeThemeURL('/images/apple_16.png')});
}
.images_4 {
	background-image: url(${c:encodeThemeURL('/images/bebo_16.png')});
}
.images_5 {
	background-image: url(${c:encodeThemeURL('/images/bing_16.png')});
}
.images_6 {
	background-image: url(${c:encodeThemeURL('/images/blogger_16.png')});
}
.images_7 {
	background-image: url(${c:encodeThemeURL('/images/delicious_16.png')});
}
.images_8 {
	background-image: url(${c:encodeThemeURL('/images/digg_16.png')});
}
.images_9 {
	background-image: url(${c:encodeThemeURL('/images/facebook_16.png')});
}
.images_10 {
	background-image: url(${c:encodeThemeURL('/images/flickr_16.png')});
}
.images_11 {
	background-image: url(${c:encodeThemeURL('/images/friendfeed_16.png')});
}
.images_12 {
	background-image: url(${c:encodeThemeURL('/images/google_16.png')});
}
.images_13 {
	background-image: url(${c:encodeThemeURL('/images/linkedin_16.png')});
}
.images_14 {
	background-image: url(${c:encodeThemeURL('/images/netvibes_16.png')});
}
.images_15 {
	background-image: url(${c:encodeThemeURL('/images/newsvine_16.png')});
}
.images_16 {
	background-image: url(${c:encodeThemeURL('/images/reggit_16.png')});
}
.images_17 {
	background-image: url(${c:encodeThemeURL('/images/rss_16.png')});
}
.images_18 {
	background-image: url(${c:encodeThemeURL('/images/sharethis_16.png')});
}
.images_19 {
	background-image: url(${c:encodeThemeURL('/images/stumbleupon_16.png')});
}
.images_20 {
	background-image: url(${c:encodeThemeURL('/images/technorati_16.png')});
}
.images_21 {
	background-image: url(${c:encodeThemeURL('/images/twitter_16.png')});
}
.images_22 {
	background-image: url(${c:encodeThemeURL('/images/utorrent_16.png')});
}
.images_23 {
	background-image: url(${c:encodeThemeURL('/images/vimeo_16.png')});
}
.images_24 {
	background-image: url(${c:encodeThemeURL('/images/vkontakte_16.png')});
}
.images_25 {
	background-image: url(${c:encodeThemeURL('/images/wikipedia_16.png')});
}
.images_26 {
	background-image: url(${c:encodeThemeURL('/images/windows_16.png')});
}
.images_27 {
	background-image: url(${c:encodeThemeURL('/images/yahoo_16.png')});
}

.red {
	font-weight: blod;
	font-size: 14px;
	color: red;
	text-shadow: 0 0 2px #000;
}
.blue {
	font-weight: blod;
	font-size: 14px;
	color: blue;
	text-shadow: 0 0 2px #000;
}