@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')
@Grab(group='oauth.signpost', module='signpost-core', version='1.2.1.2')
@Grab(group='oauth.signpost', module='signpost-commonshttp4', version='1.2.1.2')

@Grab(group='net.sourceforge.jtds', module='jtds', version='1.3.1')



import groovy.sql.Sql
import groovy.json.JsonSlurper


class GameParse {

    static Sql sql=Sql.newInstance( 'jdbc:sqlserver://forever.local', 'x', 'x', 'com.microsoft.sqlserver.jdbc.SQLServerDriver' )
    static parser=new JsonSlurper()

    static {
        sql.execute("use wl;")
    }

    static doStanding(gid,turn,x) {
        x.each {
            def pid=it.ownedBy
            if (pid=='Neutral') pid=null
            if (pid=='AvailableForDistribution') pid=0

            sql.execute("insert standing (gid,turn,tid,pid,armies) select $gid,$turn,${it.terrID},$pid,${it.armies}")
        }
    }

    static doStandingX(x) {
        def r=[:]
        x.each {
            def pid=it.ownedBy
            if (pid=='Neutral') pid=null
            if (pid=='AvailableForDistribution') pid=0

            r[it.terrID]=[pid: pid,armies: it.armies,c: true,lastpid: null,lastarmies: null]
        }

        r
    }

    static parse(def gid) {

        try {


            def j=sql.firstRow("select json from gamejson where gid=$gid").json
            def m=parser.parseText(j)

            def date


            println "$gid: ${m.name}"

            sql.execute("delete games where gid=$gid")
            sql.execute("insert games (gid,state,started,turns,parsedate) select $gid,state=${m.state},started=${m.turn0?.first()?.date},turns=${m.numberOfTurns},parsedate=getdate()")


            m.players.each { p ->
                def pid=p.id[2..-3]

                sql.execute("insert players (gid,pid,name,state,pidlong) select $gid,${pid},${p.name},${p.state},${p.id}")

            }

            m.picks.each { k, v ->

                def pid=k[7..-1]

                v.eachWithIndex { x, i ->
                    if (i<=5) {
                        sql.execute("insert picks (gid,pid,n,tid) select $gid,$pid,${i+1},$x")
                    }
                }


            }

            //doStanding(gid,-1,m.distributionStanding)

            def s=[]

            s << doStandingX(m.distributionStanding)

            m.findAll { it.key ==~ /standing\d+/ }.sort { it.key[8..-1].toInteger() }.each {
                def xs=doStandingX(it.value)
                def xp=s[-1]

                xs.each { k, v ->
                    //v.c=((xp[k].pid!=v.pid) || (xp[k].armies!=v.armies))
                    v.lastpid=xp[k].pid
                    v.lastarmies=xp[k].armies
                }

                s << xs

            }

            s.eachWithIndex { xs, i ->
                xs.each { k, v ->
                    //if (v.c) {
                        sql.execute("insert standing (gid,turn,tid,pid,armies,lastpid,lastarmies) select $gid,${i-1},$k,${v.pid},${v.armies},${v.lastpid},${v.lastarmies}")
                    //}
                }
            }


//            def xx
//
//            m.each { k, v ->
//                if (k ==~ /standing\d+/) {
//                    def n=k[8..-1]
//                    doStanding(gid,n,v)
//                }
//            }

            sql.execute("update gamelist set parsedate=getdate() where gid=$gid")


        } catch (Exception ex) {

        }



    }

}
