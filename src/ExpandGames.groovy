@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')
@Grab(group='oauth.signpost', module='signpost-core', version='1.2.1.2')
@Grab(group='oauth.signpost', module='signpost-commonshttp4', version='1.2.1.2')

@Grab(group='net.sourceforge.jtds', module='jtds', version='1.3.1')



import static groovyx.net.http.ContentType.*
import groovy.sql.Sql


Sql sql=Sql.newInstance( 'jdbc:sqlserver://forever.local', 'x', 'x', 'com.microsoft.sqlserver.jdbc.SQLServerDriver' )

sql.execute("use wl;")

//def c=new RESTClient('http://warlight.net/API/')

sql.eachRow("select top(100000) gid from gamelist g where parsedate is null order by gid desc") { row ->

    GameParse.parse(row.gid)



}



