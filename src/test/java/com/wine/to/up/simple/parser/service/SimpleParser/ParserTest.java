package com.wine.to.up.simple.parser.service.SimpleParser;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;


public class ParserTest {

    private static Parser parser;
    private static final String URL = "https://simplewine.ru";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void beforeTest() {
        parser = new Parser();
        ReflectionTestUtils.setField(parser, "URL", URL);
        ReflectionTestUtils.setField(parser, "HOME_URL", URL + "/catalog/vino/");
        ReflectionTestUtils.setField(parser, "WINE_URL", URL + "/catalog/vino/page");
    }

    @Test
    public void testParseNumberOfPagesIntegration() throws IOException {
        Document testCatalogPage = Parser.URLToDocument(Parser.HOME_URL);
        int numberOfPages = parser.parseNumberOfPages(testCatalogPage);
        assertTrue(numberOfPages > 0);
    }

    @Test
    public void testParseNumberOfPages107() throws IOException {
        File testCatalogPageFile = new File("src/test/test-resources/Catalog_107_pages.html");
        Document testCatalogPage = Jsoup.parse(testCatalogPageFile, "UTF-8");
        int numberOfPages = parser.parseNumberOfPages(testCatalogPage);
        assertEquals(107, numberOfPages);
        assertTrue(numberOfPages > 0);
    }

    @Test
    public void testParseNumberOfPages1() throws IOException {
        File testCatalogPageFile = new File("src/test/test-resources/Catalog_1_page.html");
        Document testCatalogPage = Jsoup.parse(testCatalogPageFile, "UTF-8");
        int numberOfPages = parser.parseNumberOfPages(testCatalogPage);
        assertEquals(1, numberOfPages);
    }

    @Test
    public void testParseNumberOfPages3() throws IOException {
        File testCatalogPageFile = new File("src/test/test-resources/Catalog_3_pages.html");
        Document testCatalogPage = Jsoup.parse(testCatalogPageFile, "UTF-8");
        int numberOfPages = parser.parseNumberOfPages(testCatalogPage);
        assertEquals(3, numberOfPages);
    }

    @Test
    public void testParseNumberOfPages5() throws IOException {
        File testCatalogPageFile = new File("src/test/test-resources/Catalog_5_pages.html");
        Document testCatalogPage = Jsoup.parse(testCatalogPageFile, "UTF-8");
        int numberOfPages = parser.parseNumberOfPages(testCatalogPage);
        assertEquals(5, numberOfPages);
    }

    @Test
    public void testParseNumberOfPagesNoPagesNavigation() throws IOException {
        File testCatalogPageFile = new File("src/test/test-resources/Wine_SimpleWine.html"); //wine page instead of catalog page
        Document testCatalogPage = Jsoup.parse(testCatalogPageFile, "UTF-8");
        thrown.expect(IndexOutOfBoundsException.class); //"pagination__navigation" in input file is absent, list in parseNumberOfPages() has length = 0
        int numberOfPages = parser.parseNumberOfPages(testCatalogPage);
        //assertEquals(0, numberOfPages);
    }

    @Test
    public void testParseWineHTML() throws IOException {
        File testWinePageFile = new File("src/test/test-resources/Wine_SimpleWine.html");
        Document testWinePage = Jsoup.parse(testWinePageFile, "UTF-8");
        SimpleWine testWine = SimpleWine.builder().name("Бин 50 Шираз").brandID("Lindeman's").countryID("Австралия").price((float) 952.0)
                .year(2018).volume((float) 0.75).abv((float) 13.0).colorType("красное").grapeType("шираз")
                .sugarType("полусухое").discount((float) 20.0).build();
        assertEquals(testWine.toString(), Parser.parseWine(testWinePage).toString());
    }

    @Test
    public void testParseWineWrongPageFormat() throws IOException {
        File testWinePageFile = new File("src/test/test-resources/Catalog_107_pages.html"); //catalog page instead of wine page
        Document testWinePage = Jsoup.parse(testWinePageFile, "UTF-8");
        SimpleWine testWine = SimpleWine.builder().name("Бин 50 Шираз").brandID("Lindeman's").countryID("Австралия").price((float) 952.0)
                .year(2018).volume((float) 0.75).abv((float) 13.0).colorType("красное").grapeType("шираз")
                .sugarType("полусухое").discount((float) 20.0).build();

        thrown.expect(IndexOutOfBoundsException.class); //"product__header-russian-name" in input file is absent, list in ParseWine() has length = 0
        assertEquals(testWine.toString(), Parser.parseWine(testWinePage).toString());
    }
}
