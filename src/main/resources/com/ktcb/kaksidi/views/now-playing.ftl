<#-- @ftlvariable name="" type="com.ktcb.kaksidi.views.NowPlayingView" -->
<html>
<head>
    <script src="/assets/react.js"></script>
    <script src="/assets/JSXTransformer.js"></script>
</head>
<body>
<div id="mount-point"></div>
<script type="text/jsx">
    React.renderComponent(
            <h1>Hello, world!</h1>,
            document.getElementById('mount-pointx')
    );
</script>
<#--TODO won't need this later of course-->
<#list plays as play>
<h1>${play.title?html} by ${play.artist?html} was played on ${play.channelKey?html} at ${play.when}</h1>
</#list>
</body>
</html>