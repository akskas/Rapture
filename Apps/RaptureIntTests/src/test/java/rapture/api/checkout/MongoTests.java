/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2016 Incapture Technologies LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rapture.api.checkout;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.client.MongoCollection;

import rapture.audit.AuditLog;
import rapture.audit.AuditLogFactory;
import rapture.common.CallingContext;
import rapture.common.RaptureFolderInfo;
import rapture.common.RaptureScript;
import rapture.common.RaptureScriptLanguage;
import rapture.common.RaptureScriptPurpose;
import rapture.common.RaptureURI;
import rapture.common.Scheme;
import rapture.common.SeriesPoint;
import rapture.common.api.DocApi;
import rapture.common.api.ScriptApi;
import rapture.common.api.SeriesApi;
import rapture.common.client.HttpDocApi;
import rapture.common.client.HttpEventApi;
import rapture.common.client.HttpIdGenApi;
import rapture.common.client.HttpLoginApi;
import rapture.common.client.HttpScriptApi;
import rapture.common.client.HttpSeriesApi;
import rapture.common.exception.RaptureException;
import rapture.common.model.AuditLogEntry;
import rapture.dsl.idgen.IdGenFactory;
import rapture.dsl.idgen.RaptureIdGen;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;
import rapture.mongodb.MongoDBFactory;

/**
 * Tests to exercise the Mongo repo to check for breakages in migrating to Mongo
 * 3.0
 */

public class MongoTests {

    private HttpLoginApi raptureLogin = null;
    private HttpSeriesApi series = null;
    private HttpDocApi document = null;
    private HttpScriptApi script = null;
    private HttpEventApi event = null;
    private HttpIdGenApi fountain = null;

    /**
     * Setup TestNG method to create Rapture login object and objects.
     *
     * @param RaptureURL
     *            Passed in from <env>_testng.xml suite file
     * @param RaptureUser
     *            Passed in from <env>_testng.xml suite file
     * @param RapturePassword
     *            Passed in from <env>_testng.xml suite file
     * @return none
     */
    @BeforeMethod
    @BeforeClass(groups = { "mongo" })
    @Parameters({ "RaptureURL", "RaptureUser", "RapturePassword" })
    public void setUp(@Optional("http://localhost:8665/rapture") String url, @Optional("rapture") String username, @Optional("rapture") String password) {

        // //If running from eclipse set env var -Penv=docker or use the
        // following url variable settings:
        // //url="http://192.168.99.101:8665/rapture"; //docker
        // //url="http://localhost:8665/rapture";
        // System.out.println("Using url " + url);
        // raptureLogin = new HttpLoginApi(url, new
        // SimpleCredentialsProvider(username, password));
        // raptureLogin.login();
        // series = new HttpSeriesApi(raptureLogin);
        // document = new HttpDocApi(raptureLogin);
        // script = new HttpScriptApi(raptureLogin);
        // event = new HttpEventApi(raptureLogin);
        // fountain = new HttpIdGenApi(raptureLogin);
    }

    /**
     * TestNG method to cleanup.
     *
     * @param none
     * @return none
     */
    @AfterClass(groups = { "mongo" })
    public void afterTest() {
        raptureLogin = null;
    }

    @Test(groups = { "mongo" }, enabled = true, description = "Simple Mongo Audit log test")
    public void testSimpleAuditGen() {
        UUID message = UUID.randomUUID();
        AuditLog log = AuditLogFactory.getLog("test", "LOG {} using MONGODB {prefix=\"mongotest\"}");
        List<AuditLogEntry> entries = log.getRecentEntries(Integer.MAX_VALUE);
        System.out.println("Found " + entries.size() + " log entries");
        log.writeLog("category", 0, message.toString(), "user");
        List<AuditLogEntry> newEntries = log.getRecentEntries(Integer.MAX_VALUE);
        assertEquals(1 + entries.size(), newEntries.size());
        assertEquals(message.toString(), newEntries.get(entries.size()).getMessage());
    }

    @Test(groups = { "mongo" }, enabled = true, description = "Simple Mongo document read/write test")
    public void testSimpleDocument() {
        UUID uuid = UUID.randomUUID();
        CallingContext context = ContextFactory.getKernelUser();
        DocApi docApi = Kernel.getDoc();
        RaptureURI docUri = new RaptureURI("//" + uuid, Scheme.DOCUMENT);

        docApi.createDocRepo(context, docUri.toString(), "NREP {} using MONGODB {prefix=\"mongotest\"}");
        String uri = docUri.toString() + "/test";
        String content = "{\"foo\" : \"bar\", \"baz\" : 1}";
        String content2 = "{\"foo\" : \"FOO\", \"bar\" : 2}";
        docApi.putDoc(context, uri, content);
        String get = docApi.getDoc(context, uri);
        Assert.assertEquals(content, get);
        docApi.putDoc(context, uri, content2);
        get = docApi.getDoc(context, uri);
        Assert.assertEquals(content2, get);
        docApi.deleteDoc(context, uri);
        get = docApi.getDoc(context, uri);
        Assert.assertNull(get, "Document was deleted");
        docApi.deleteDocRepo(context, docUri.toString());
    }

    @Test(groups = { "mongo" }, enabled = true, description = "Simple Mongo script read/write test")
    public void testSimpleScript() {
        UUID uuid = UUID.randomUUID();
        CallingContext context = ContextFactory.getKernelUser();
        ScriptApi scriptApi = Kernel.getScript();
        RaptureURI scriptUri = new RaptureURI("//" + uuid, Scheme.SCRIPT);
        String uri = scriptUri.toString() + "/test";

        RaptureScript script = new RaptureScript();
        script.setLanguage(RaptureScriptLanguage.REFLEX);
        script.setPurpose(RaptureScriptPurpose.PROGRAM);
        script.setName("test");
        script.setScript("println('Success');");
        script.setAuthority(scriptUri.getAuthority());

        scriptApi.putScript(context, uri, script);
        RaptureScript get = scriptApi.getScript(context, uri);
        assertEquals(script, get);
        scriptApi.deleteScript(context, uri);
        get = scriptApi.getScript(context, uri);
        Assert.assertNull(get, "Script has been deleted");
    }

    @Test(groups = { "mongo" }, enabled = true, description = "IDGEN test")
    public void testIdGen() {
        UUID uuid = UUID.randomUUID();
        RaptureIdGen f = IdGenFactory.getIdGen("IDGEN { initial=\"143\", base=\"36\", length=\"8\", prefix=\"FOO\" } USING MONGO { prefix=\"" + uuid + "\"}");
        String result = f.incrementIdGen(14500L);
        Assert.assertEquals("FOO00000BAR", result);
        result = f.incrementIdGen(2L);
        Assert.assertEquals("FOO00000BAT", result);
        f.invalidate();
        try {
            result = f.incrementIdGen(2L);
            Assert.fail("ID Generator was invalidated");
        } catch (RaptureException e) {
            assertEquals("IdGenerator has been deleted", e.getMessage());
        }
    }

    @Test(groups = { "mongo" }, enabled = true, description = "Index Handler test")
    public void testIndexHandler() {

        UUID uuid = UUID.randomUUID();
        CallingContext context = ContextFactory.getKernelUser();
        DocApi docApi = Kernel.getDoc();
        RaptureURI docUri = new RaptureURI("//" + uuid, Scheme.DOCUMENT);

        docApi.createDocRepo(context, docUri.toString(), "VREP {} using MONGODB {prefix=\"mongotest\"}");
        String uri = docUri.toString() + "/test";
        String content = "{\"foo\" : \"bar\", \"baz\" : 1}";
        String content2 = "{\"foo\" : \"FOO\", \"bar\" : 2}";
        docApi.putDoc(context, uri, content);
        String get = docApi.getDoc(context, uri);
        Assert.assertEquals(content, get);
        docApi.putDoc(context, uri, content2);
        get = docApi.getDoc(context, uri);
        Assert.assertEquals(content2, get);
        docApi.deleteDoc(context, uri);
        get = docApi.getDoc(context, uri);
        Assert.assertNull(get, "Document was deleted");
        docApi.deleteDocRepo(context, docUri.toString());
    }

    @Test(groups = { "mongo" }, enabled = true, description = "Simple Mongo series test")
    public void testSimpleSeries() {
        UUID uuid = UUID.randomUUID();
        RaptureURI seriesUri = new RaptureURI("//" + uuid, Scheme.SERIES);

        SeriesApi serApi = Kernel.getSeries();
        CallingContext context = ContextFactory.getKernelUser();

        serApi.createSeriesRepo(context, seriesUri.toString(), "SREP {} using MONGODB {prefix=\"" + uuid + "\"}");

        String uri = seriesUri.toString() + "/test";
        serApi.createSeries(context, uri);
        List<String> points = new ArrayList<>();
        List<String> pointVals = new ArrayList<>();
        points.add("foo");
        points.add("bar");
        pointVals.add("Foo");
        pointVals.add("Bar");

        serApi.addStringsToSeries(context, uri, points, pointVals);
        List<SeriesPoint> check = serApi.getPoints(context, uri);
        assertEquals(2, check.size());

        for (SeriesPoint p : check) {
            Assert.assertTrue(points.contains(p.getColumn()));
            Assert.assertTrue(pointVals.contains(p.getValue().substring(1)));
        }

        List<String> points2 = new ArrayList<>();
        List<String> pointVals2 = new ArrayList<>();
        points2.add("Doctor");
        pointVals2.add("Foo");
        points2.add("River");
        pointVals2.add("Song");
        points.addAll(points2);
        pointVals.addAll(pointVals2);

        serApi.addStringsToSeries(context, uri, points2, pointVals2);
        check = serApi.getPoints(context, uri);
        assertEquals(4, check.size());

        for (SeriesPoint p : check) {
            Assert.assertTrue(points.contains(p.getColumn()));
            Assert.assertTrue(pointVals.contains(p.getValue().substring(1)));
        }

        serApi.deletePointsFromSeries(context, uri);
        check = serApi.getPoints(context, uri);
        Assert.assertTrue(check.isEmpty());
        serApi.deleteSeries(context, uri);
        serApi.deleteSeriesRepo(context, seriesUri.toString());
    }

    @Test
    public void compatibility() {
        UUID uuid = UUID.randomUUID();
        RaptureIdGen f = IdGenFactory.getIdGen("IDGEN { initial=\"143\", base=\"36\", length=\"8\", prefix=\"FOO\" } USING MONGO { prefix=\"" + uuid + "\"}");
        DB db = MongoDBFactory.getDB("default");
        DBCollection dc = db.getCollection(uuid.toString());
        MongoCollection<Document> mc = MongoDBFactory.getCollection("default", uuid.toString());

        Assert.assertEquals(dc.getFullName(), "RaptureMongoDB." + uuid.toString());
        Assert.assertEquals(mc.getNamespace().getFullName(), "RaptureMongoDB." + uuid.toString());

    }

    @Test
    public void RAP4038() {
        DocApi docApi = Kernel.getDoc();
        CallingContext context = ContextFactory.getKernelUser();
        UUID uuid = UUID.randomUUID();
        RaptureURI authUri = new RaptureURI("//" + uuid, Scheme.DOCUMENT);
        docApi.createDocRepo(context, authUri.toString(), "NREP {} using MONGODB {prefix=\"" + uuid + "\"}");
        String docUri = RaptureURI.builder(authUri).docPath("test/this/please").build().toString();
        String content = "{\"foo\":\"bar\"}";

        docApi.putDoc(context, docUri, content);
        Assert.assertEquals(content, docApi.getDoc(context, docUri));

        Map<String, RaptureFolderInfo> allChildrenMap = docApi.listDocsByUriPrefix(context, authUri.toString(), 10);
        System.out.println("size of childrenMap: " + allChildrenMap.size());
        Assert.assertEquals(allChildrenMap.size(), 3);

    }
}
