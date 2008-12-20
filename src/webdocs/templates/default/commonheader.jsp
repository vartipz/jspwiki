<%@ taglib uri="http://jakarta.apache.org/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.*" %>
<%@ page import="com.ecyrd.jspwiki.util.*" %>
<%@ page import="com.ecyrd.jspwiki.preferences.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="com.ecyrd.jspwiki.action.WikiContextFactory" %>
<%--
   This file provides a common header which includes the important JSPWiki scripts and other files.
   You need to include this in your template, within <head> and </head>.  It is recommended that
   you don't change this file in your own template, as it is likely to change quite a lot between
   revisions.

   Any new functionality, scripts, etc, should be included using the TemplateManager resource
   include scheme (look below at the <wiki:IncludeResources> tags to see what kind of things
   can be included).
--%>
<%-- CSS stylesheet --%>
<link rel="stylesheet" media="screen, projection, print" type="text/css" href="<wiki:Link format='url' templatefile='jspwiki.css' />" />
<%-- put this at the top, to avoid double load when not yet cached --%>
<link rel="stylesheet" type="text/css" media="print" href="<wiki:Link format='url' templatefile='jspwiki_print.css' />" />
<wiki:IncludeResources type="stylesheet" />
<wiki:IncludeResources type="inlinecss" />

<%-- display the more-menu inside the leftmenu, when javascript is not avail --%>
<noscript>
<style type="text/css">
#hiddenmorepopup { display:block; }
</style>
</noscript>

<%-- JAVASCRIPT --%>
<script type="text/javascript" src="<wiki:Link format='url' jsp='scripts/mootools.js' />"></script>
<script type="text/javascript" src="<wiki:Link format='url' jsp='scripts/prettify.js' />"></script>
<script type="text/javascript" src="<wiki:Link format='url' jsp='scripts/jspwiki-common.js' />"></script>
<script type="text/javascript" src="<wiki:Link format='url' jsp='scripts/jspwiki-commonstyles.js' />"></script>
<wiki:IncludeResources type="script" />
<meta name="wikiContext" content='<wiki:Variable var="requestcontext" />' />
<meta name="wikiBaseUrl" content='<wiki:BaseURL/>' />
<meta name="wikiPageUrl" content='<wiki:Link format="url" page="#$%" />' />
<meta name="wikiEditUrl" content='<wiki:EditLink format="url" />' />
<meta name="wikiJsonUrl" content='<%=  WikiContextFactory.findContext(pageContext).getURL( WikiContext.NONE, "JSON-RPC" ) %>' /><%--unusual pagename--%>
<meta name="wikiPageName" content='<wiki:Variable var="pagename" />' /><%--pagename without blanks--%>
<meta name="wikiUserName" content='<wiki:UserName/>' />
<meta name="wikiTemplateUrl" content='<wiki:Link format="url" templatefile="" />' />
<meta name="wikiApplicationName" content='<wiki:Variable var="ApplicationName" />' />

<script type="text/javascript">//<![CDATA[
/* Localized javascript strings: LocalizedStrings[] */
<wiki:IncludeResources type="jslocalizedstrings"/>
<wiki:IncludeResources type="jsfunction"/>
//]]></script>

<meta http-equiv="Content-Type" content="text/html; charset=<wiki:ContentEncoding/>" />
<link rel="search" href="<wiki:LinkTo format='url' page='FindPage' />" title='Search <wiki:Variable var="ApplicationName" />' />
<link rel="help" href="<wiki:LinkTo format='url' page='TextFormattingRules' />" title="Help" />
<%
  WikiContext c = WikiContextFactory.findContext( pageContext );
  String frontpage = c.getEngine().getFrontPage();
 %>
 <link rel="start" href="<wiki:LinkTo format='url' page='<%=frontpage%>' />" title="Front page" />
<link rel="alternate stylesheet" type="text/css" href="<wiki:Link format='url' templatefile='jspwiki_print.css' />" title="Print friendly" />
<link rel="alternate stylesheet" type="text/css" href="<wiki:Link format='url' templatefile='jspwiki.css' />" title="Standard" />
<link rel="shortcut icon" type="image/x-icon" href="<wiki:Link format='url' jsp='images/favicon.ico' />" />
<%-- ie6 needs next line --%>
<link rel="icon" type="image/x-icon" href="<wiki:Link format='url' jsp='images/favicon.ico' />" />

<%-- Support for the universal edit button (www.universaleditbutton.org) --%>
<wiki:CheckRequestContext context='view|info|diff|upload'>
  <wiki:Permission permission="edit">
    <wiki:PageType type="page">
    <link rel="alternate" type="application/x-wiki" href="<wiki:EditLink format='url' />" title="<fmt:message key='actions.edit.title' />" />
    </wiki:PageType>
  </wiki:Permission>
</wiki:CheckRequestContext>

<wiki:FeedDiscovery/>

<%-- SKINS : extra stylesheets, extra javascript --%>
<c:if test='${(!empty prefs.SkinName) && (prefs.SkinName!="PlainVanilla") }'>
<link rel="stylesheet" type="text/css" media="screen, projection, print" href="<wiki:Link format='url' templatefile='skins/' /><c:out value='${prefs.SkinName}/skin.css' />" />
<%--
<link rel="stylesheet" type="text/css" media="print"
     href="<wiki:Link format='url' templatefile='skins/' /><c:out value='${prefs.SkinName}/print_skin.css' />" />
--%>
<script type="text/javascript" src="<wiki:Link format='url' templatefile='skins/' /><c:out value='${prefs.SkinName}/skin.js' />"></script>
</c:if>

<wiki:Include page="localheader.jsp" />