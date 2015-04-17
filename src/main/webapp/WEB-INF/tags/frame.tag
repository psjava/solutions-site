<%@tag pageEncoding="UTF-8"%>
<%@attribute name="title" required="true"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>${title}</title>
    <link rel="stylesheet" media="screen" href="/bootstrap-3.0.3-dist/dist/css/bootstrap.min.css">
    <link rel="stylesheet" media="screen" href="/stylesheets/main.css">
</head>
<body style="padding-top:60px;">
<div class="container">
    <nav class="navbar navbar-default navbar-fixed-top" role="banner">
        <div class="container">
            <div class="navbar-header">
                <a class="navbar-brand" href="/">Accepted Solution</a>
            </div>
            <div class="collapse navbar-collapse">
                <!--<ul class="nav navbar-nav">
                <li><a href="/list/poj">POJ</a></li>
                <li><a href="/list/codejam">Code Jam</a></li>
                <li><a href="/list/codejam">Hacker Cup</a></li>
            </ul>
                <form class="navbar-form navbar-right" role="search">
                    <div class="form-group">
                        <input type="text" class="form-control" placeholder="Search">
                    </div>
                    <button type="submit" class="btn btn-default">Submit</button>
                </form>-->
            </div><!-- /.navbar-collapse -->
        </div>
    </nav>
    <jsp:doBody/>
</div>

<script>
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    ga('create', 'UA-42967591-2', 'acceptedsolution.org');
    ga('send', 'pageview');

</script>
</body>
</html>