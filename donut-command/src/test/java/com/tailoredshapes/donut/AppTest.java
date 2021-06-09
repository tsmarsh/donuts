package com.tailoredshapes.donut;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class AppTest {

    private static App app;
    static int port = 8068;

    @BeforeClass
    public static void setUp() throws Exception {
        app = new App(port);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        app.service.stop();
    }

    @Test
    public void testOK() {
        given().port(port).when().get("/").then().body(equalTo("OK")).statusCode(200);
    }
}