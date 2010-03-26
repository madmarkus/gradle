<div id="homeContent">
<h1 id="homeTitle">A better way to build.</h1>

<div id="about">

    <p>Project automation is essential to the success of software projects. It should be straight-forward, easy and fun
    to implement.</p>
    <p>There is no one-size-fits-all process for builds. Therefore Gradle does not impose a rigid process over people.
    Yet we think finding and describing YOUR process is very important. And so, Gradle has the very best support for
    describing it.</p>
    <p>We don't believe in tools that save people from themselves. Gradle gives you all the freedom you need. Gradle
    enables you to create declarative, maintainable, concise and highly-performing builds.
    </p>

    <div class="more"><a href="overview.html" title="Learn more"><img src="img/learnMore.png"/></a></div>

</div>

<div id="rightcolumn">
<div id="gettingstarted">
    <h2>Getting started</h2>
    <ol>
        <li>Download <a href="http://dist.codehaus.org/gradle/gradle-${currentRelease.version}-all.zip" title="gradle-${currentRelease.version}-all.zip">${currentRelease.version}</a></li>
        <li>Try the <a href="tutorials.html">tutorials</a></li>
        <li>Read the <a href="documentation.html">documentation</a></li>
        <li>Join the <a href="community.html">community</a></li>
    </ol>
</div>
<div id="news" class="homeList">
    <h2>Recent news</h2>
    <ul>${php.indexNews}</ul>
    <div class="more"><a href="news.php">more news</a></div>
</div>
</div>

<div id="homeColumns">

<div id="training" class="homeList">
    <h2>Upcoming training</h2>
    <ul>
        <li><a href="http://gradle.biz/training.html">April 29, London</a></li>
        <li><a href="http://gradle.biz/training.html">May 25, Paris</a></li>
        <li><a href="http://gradle.biz/training.html">July 22, London</a></li>
        <li><a href="http://gradle.biz/training.html">August 2, Paris</a></li>
        <li><a href="http://gradle.biz/training.html">October 25, London</a></li>
    </ul>
    <div class="more"><a href="http://gradle.biz/training.html">learn more</a></div>
</div>

<div id="who_uses" class="homeList">
    <h2>Who uses Gradle?</h2>
    <table>
        <tr>
            <td>
                <ul>
                    <li><a href="http://www.grails.org">Grails</a></li>
                    <li><a href="http://code.google.com/p/spock/">Spock</a></li>
                    <li><a href="http://code.google.com/p/gmock/">GMock</a></li>
                    <li><a href="http://www.zeppelin.com">Zeppelin</a></li>
                </ul>
            </td>
            <td>
                <ul>
                    <li><a href="http://gpars.codehaus.org/">GPars</a></li>
                    <li><a href="http://gant.codehaus.org/">Gant</a></li>
                    <li><a href="http://www.corp.carrier.com">Carrier</a></li>
                    <li><a href="http://www.fcc-fac.ca/">Farm Credit Canada</a></li>
                </ul>
            </td>
        </tr>
    </table>
</div>

</div>
</div>
<script type="text/javascript">
    function bottomY(element) {
        var bottom = element.offsetHeight;
        while (element.offsetParent) {
            bottom += element.offsetTop;
            element = element.offsetParent;
        }
        return bottom;
    }
    
    function adjustHeight() {
        var training = bottomY(document.getElementById('training'));
        var whoUses = bottomY(document.getElementById('who_uses'));
        var news = bottomY(document.getElementById('news'));
        var content = document.getElementById('homeColumns');
        var contentBottom = bottomY(content);
        var diff = 0;
        if (contentBottom < training) {
            var d = training - contentBottom;
            diff = Math.max(diff, d);
        }
        if (diff < whoUses) {
            diff = Math.max(diff, whoUses - contentBottom);
        }
        if (diff < news) {
            diff = Math.max(diff, news - contentBottom);
        }
        content.style.height = content.offsetHeight + diff + 'px';
    }
    window.onload = adjustHeight;
</script>
