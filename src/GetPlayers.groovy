@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')
@Grab(group='oauth.signpost', module='signpost-core', version='1.2.1.2')
@Grab(group='oauth.signpost', module='signpost-commonshttp4', version='1.2.1.2')



import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.GET
import groovy.sql.Sql

Sql sql=Sql.newInstance( 'jdbc:sqlserver://forever.local', 'x', 'x', 'com.microsoft.sqlserver.jdbc.SQLServerDriver' )

sql.execute("use wl;")
sql.execute("truncate table pgroup;")


def http=new HTTPBuilder()

def s

def lids=[]
def pids=[]

for (int off=0;off<4;off++) {

    println "r: $off"

    http.request("https://www.warlight.net", groovyx.net.http.Method.GET, groovyx.net.http.ContentType.TEXT) { req ->
        uri.path = "/LadderTeams"
        uri.query = [ID: 0, Offset: off * 50]
        response.success = { resp, reader ->

            println "Got response: ${resp.statusLine}"
            println "Content-Type: ${resp.headers.'Content-Type'}"
            //println reader.text
            s = reader.text
        }

    }


    def m = (s =~ /\/LadderTeam\?LadderTeamID=(\d+)/)

    m.each { lids << it[1] }
}


lids.each { lid ->

    http.request("https://www.warlight.net", groovyx.net.http.Method.GET, groovyx.net.http.ContentType.TEXT) { req ->
        uri.path = "/LadderTeam"
        uri.query = [LadderTeamID: lid]
        response.success = { resp, reader ->

            println "Got response: ${resp.statusLine}"
            println "Content-Type: ${resp.headers.'Content-Type'}"
            //println reader.text
            s = reader.text
        }

    }

    def m = (s =~ /"Profile\?p=(\d+)/)

    pids << m[0][1]


}

pids.each {

    sql.execute("insert pgroup (pidlong) select $it")
}



//def s=r.responseData.str



