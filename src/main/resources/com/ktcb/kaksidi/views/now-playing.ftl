<#-- @ftlvariable name="" type="com.ktcb.kaksidi.views.NowPlayingView" -->
<html>
<head>
    <script src="/assets/react.js"></script>
    <script src="/assets/JSXTransformer.js"></script>
</head>
<body>
<#list plays as play>
<h1>${play.title?html} by ${play.artist?html} was played on ${play.channelKey?html} at ${play.when}</h1>
</#list>
</body>
</html>