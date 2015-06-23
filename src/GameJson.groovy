
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')
@Grab(group='oauth.signpost', module='signpost-core', version='1.2.1.2')
@Grab(group='oauth.signpost', module='signpost-commonshttp4', version='1.2.1.2')

import groovy.json.*
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*



class GameJson {

    static load(def id) {

        def c=new RESTClient('http://warlight.net/API/')
        c.contentType=TEXT

        def r=c.post(
                path: 'Gamefeed.aspx',
                query: [gameid: id,gethistory: true],
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

        def j=w.join(' ')

        def parser=new JsonSlurper()
        def m=parser.parseText(j)

        def builder=new JsonBuilder(m)

        builder.toPrettyString()

    }

}
