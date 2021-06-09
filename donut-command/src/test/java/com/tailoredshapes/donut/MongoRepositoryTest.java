package com.tailoredshapes.donut;

import com.mongodb.ServerAddress;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.tailoredshapes.stash.Stash;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import netscape.javascript.JSObject;
import org.bson.Document;
import org.junit.*;

import java.net.InetSocketAddress;
import java.time.Instant;

import static com.tailoredshapes.stash.Stash.stash;
import static org.junit.Assert.*;

public class MongoRepositoryTest {

    static MemoryBackend backend;
    private static MongoServer server;
    private static MongoDatabase db;

    @BeforeClass
    public static void init() {
        backend = new MemoryBackend();
        server = new MongoServer(backend);
        InetSocketAddress serverAddress = server.bind();

        MongoClient client = new MongoClient(new ServerAddress(serverAddress));
        db = client.getDatabase("testdb");
    }

    @Before
    public void setUp() throws Exception {
        backend.dropDatabase("testdb");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void canWriteADocument() {
        Stash stash = stash("name", "Tom");

        MongoRepository mr = new MongoRepository(db, "test");
        mr.writeDoc(1010, stash);
        Document first = db.getCollection("test").find(new Document().append("id", 1010)).first();
        assertEquals("Tom", first.get("name"));
    }

    @Test
    public void canReadADocument() {
        Stash stash = stash("name", "Tom");

        MongoRepository mr = new MongoRepository(db, "test");
        mr.writeDoc(1010, stash);
        Stash result = mr.readDoc(1010);

        assertEquals("Tom", result.get("name"));
    }

    @Test
    public void cannotWriteADocumentIfItExists(){
        Stash stash = stash("name", "Tom");

        MongoRepository mr = new MongoRepository(db, "test");
        mr.writeDoc(1010, stash);
        mr.writeDoc(1010, stash.assoc("name", "Bob"));

        Stash result = mr.readDoc(1010);

        assertEquals("Tom", result.get("name"));
    }

    @Test
    public void canUpdateADocument() {
        Stash stash = stash("name", "Tom");

        MongoRepository mr = new MongoRepository(db, "test");
        mr.writeDoc(1010, stash);
        mr.changeDoc(1010, stash.assoc("name", "Bob"));

        Stash result = mr.readDoc(1010);

        assertEquals("Bob", result.get("name"));
    }

    @Test
    public void canReadTheOldVersionOfADoc() {
        Stash stash = stash("name", "Tom");

        MongoRepository mr = new MongoRepository(db, "test");
        mr.writeDoc(1010, stash);
        Instant then = Instant.now();

        mr.changeDoc(1010, stash.assoc("name", "Bob"));

        Instant now = Instant.now();

        Stash result = mr.readDoc(1010);
        assertEquals("Bob", result.get("name"));
        Stash oldResult = mr.readDoc(1010, then.toEpochMilli());

        assertEquals("Tom", oldResult.get("name"));

        Stash newerResult = mr.readDoc(1010, now.toEpochMilli());
        assertEquals("Bob", result.get("name"));
    }

    @Test
    public void canDeleteADocument() {
        Stash stash = stash("name", "Tom");

        MongoRepository mr = new MongoRepository(db, "test");
        mr.writeDoc(1010, stash);
        mr.deleteDoc(1010);

        Stash result = mr.readDoc(1010);

        assertFalse(result.contains("name"));
    }
}