@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')
@Grab(group='oauth.signpost', module='signpost-core', version='1.2.1.2')
@Grab(group='oauth.signpost', module='signpost-commonshttp4', version='1.2.1.2')

@Grab(group='net.sourceforge.jtds', module='jtds', version='1.3.1')



import groovyx.net.http.RESTClient
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*
import groovy.json.*

import groovy.sql.Sql
Sql sql=Sql.newInstance( 'jdbc:sqlserver://forever.local', 'x', 'x', 'com.microsoft.sqlserver.jdbc.SQLServerDriver' )

sql.execute("use wl;")
//def x=sql.execute("insert warlight..map select 1,'armz'")

def c=new RESTClient('http://warlight.net/API/')
//def c=new HTTPBuilder('http://warlight.net/API/')
c.contentType=TEXT

//def r=c.get(path: 'Gamefeed.aspx?gameid=7742223&gethistory=true')
def r=c.post(
        path: 'Gamefeed.aspx',
        query: [gameid: 7742223,gethistory: true],
        body: [Email: 'dan.bellandi@gmail.com',APIToken: 'XXXX'],
        requestContentType : URLENC
)

def s=r.responseData.str

def t=s.replaceAll(',',' ,').split()

def w=[]

for (int i=0;i<t.size();i++) {

    if (t[i] ==~ /"turn\d+"/) {

        w << t[i]
        w << ':'
        w << '['
        w << '{'

        def bc=0
        i+=3

        while (! ((t[i]=='}') && (bc==0))) {
            if ((t[i]==',') && (bc==0)) {
                w << '}'
                w << ','
                w << '{'
            } else {
                if (t[i]=='{') bc++
                if (t[i]=='}') bc--
                w << t[i]
            }
            i++
        }

        w << '}'
        w << ']'

    } else {
        w << t[i]
    }
}

def j=w.join('\n')

def parser=new JsonSlurper()
def m=parser.parseText(j)

println r.status

r.responseData.map.territories.each {
        def id=it.id
        sql.execute("insert terr select ${id},${it.name}")
        it.connectedTo.each {
                sql.execute("insert map select ${id},${it}")
        }
}

r.responseData.map.bonuses.each { bonus ->
        sql.execute("insert bonus select ${bonus.id},${bonus.name},${bonus.value}")
        bonus.territoryIDs.each {
                sql.execute("insert bonusmap select ${bonus.id},$it")
        }

}