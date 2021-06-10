package com.tailoredshapes.donut;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.tailoredshapes.stash.Stash;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;

import static com.tailoredshapes.stash.Stash.stash;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class AppTest {

    private static App app;
    static int port = 8068;

    static MemoryBackend backend;
    private static MongoServer server;
    private static MongoDatabase db;

    @BeforeClass
    public static void before() throws Exception {
        backend = new MemoryBackend();
        server = new MongoServer(backend);
        InetSocketAddress serverAddress = server.bind();

        MongoClient client = new MongoClient(new ServerAddress(serverAddress));
        db = client.getDatabase("testdb");


        app = new App(port, "/test", new Routes(new MongoRepository(db, "test")));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        app.service.stop();
        server.shutdown();
    }

    @Before
    public void after() throws Exception {
        backend.dropDatabase("testdb");
    }

    @Test
    public void testReadandWrite() {
        Stash doc = stash("name", "Tom");
        given().port(port).body(doc.toJSONString())
                .when().put("/test/1010").then().statusCode(200);

        given().port(port).when().get("/test/1010")
                .then().body("name", equalTo("Tom")).statusCode(200);
    }

    @Test
    public void testUpdate() {
        Stash doc = stash("name", "Tom");
        given().port(port).body(doc.toJSONString())
                .when().put("/test/1010").then().statusCode(200);

        Stash doc2 = stash("name", "Bob");
        given().body(doc2.toJSONString()).port(port).when().post("/test/1010")
                .then().body("name", equalTo("Bob")).statusCode(200);

        given().port(port).when().get("/test/1010")
                .then().body("name", equalTo("Bob")).statusCode(200);
    }

    @Test
    public void testDelete() {
        Stash doc = stash("name", "Tom");
        given().port(port).body(doc.toJSONString())
                .when().put("/test/1010").then().statusCode(200);

        given().port(port).when().delete("/test/1010").then().statusCode(200);
        given().port(port).when().get("/test/1010")
                .then().statusCode(404);
    }
}