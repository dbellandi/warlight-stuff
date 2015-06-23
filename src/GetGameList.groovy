@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')
@Grab(group='oauth.signpost', module='signpost-core', version='1.2.1.2')
@Grab(group='oauth.signpost', module='signpost-commonshttp4', version='1.2.1.2')

@Grab(group='net.sourceforge.jtds', module='jtds', version='1.3.1')



import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*

import groovy.sql.Sql
Sql sql=Sql.newInstance( 'jdbc:sqlserver://forever.local', 'x', 'x', 'com.microsoft.sqlserver.jdbc.SQLServerDriver' )

sql.execute("use wl;")
//def x=sql.execute("insert warlight..map select 1,'armz'")

def c=new RESTClient('http://warlight.net/API/')

//def r=c.get(path: 'Gamefeed.aspx?gameid=7742223&gethistory=true')
def r=c.post(
        path: 'GameIDfeed.aspx',
        query: [ladderid: 0],
        body: [Email: 'dan.bellandi@gmail.com',APIToken: 'XXXX'],
        requestContentType : URLENC
)


println r.status

r.responseData.gameIDs.each {
    sql.execute("insert gamelist (gid) select $it where $it not in (select gid from gamelist)")
}

