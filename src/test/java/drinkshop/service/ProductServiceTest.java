package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.file.FileProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;

@DisplayName("Teste pentru ProductService (File Repository)")
class ProductServiceTest {

    private ProductService productService;
    private FileProductRepository productRepo;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {

        // 1. Definim calea
        Path filePath = tempDir.resolve("products_test.dat");

        // 2. CREĂM fișierul gol dacă nu există, pentru a nu crăpa la citire (loadFromFile)
        if (java.nio.file.Files.notExists(filePath)) {
            java.nio.file.Files.createFile(filePath);
        }

        // 3. Acum putem instanția repo-ul în siguranță
        productRepo = new FileProductRepository(filePath.toString());
        productService = new ProductService(productRepo);

    }

    @Test
    @Tag("Positive")
    @DisplayName("TC1: Adăugare produs valid (ID=1, Nume='cafea')")
    void testAddProduct_Success() {
        // 1. Arrange
        Product validProduct = new Product(1, "cafea", 12.5,
                CategorieBautura.CLASSIC_COFFEE,
                TipBautura.WATER_BASED);

        // 2. Act
        productService.addProduct(validProduct);

        // 3. Assert
        Optional<Product> found = productService.findById(1);
        assertNotNull(found, "Produsul ar fi trebuit să fie salvat în fișier.");
        assertEquals("cafea", found.get().getNume());
    }

    @ParameterizedTest
    @Tag("Negative")
    @CsvSource({
            "-10, 'bubble tea', 20.0, BUBBLE_TEA, POWDER, 'Invalid id'",
            "-5,  '',           17.5, TEA,        WATER_BASED, 'Invalid id'",
            "5,   '',           12.0, JUICE,      BASIC,       'Empty name'"
    })
    @DisplayName("TC2-TC4: Verificare constrângeri ID și Nume (Cazuri Invalide)")
    void testAddProduct_InvalidInputs(int id, String nume, double pret,
                                      CategorieBautura cat, TipBautura tip,
                                      String expectedError) {
        // 1. Arrange
        Product invalidProduct = new Product(id, nume, pret, cat, tip);

        // 2. Act & 3. Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.addProduct(invalidProduct);
        }, "Ar fi trebuit să arunce excepție pentru: " + expectedError);

        assertTrue(exception.getMessage().contains(expectedError));
    }

    // ==========================================
    // TESTE BVA (Boundary Value Analysis)
    // ==========================================

    @ParameterizedTest(name = "BVA Valid -> id={0}, nume='{1}', pret={2}")
    @Tag("Positive")
    @CsvSource({
            "1,  'suc',                   3.14, JUICE,          WATER_BASED", // Limita minimă ID (1)
            "2,  'ceai',                  4.5,  TEA,            WATER_BASED", // Lângă limita minimă ID (2)
            "82, 'cafea',                 0.01, SPECIAL_COFFEE, DAIRY"        // Limita minimă preț (> 0)
    })
    @DisplayName("TC5-TC9_BVA: Adăugare produse la limitele VALIDE")
    void testAddProduct_BVA_Valid(int id, String nume, double pret, CategorieBautura cat, TipBautura tip) {
        // Arrange
        Product validBoundaryProduct = new Product(id, nume, pret, cat, tip);

        // Act & Assert
        assertDoesNotThrow(() -> productService.addProduct(validBoundaryProduct),
                "Produsul cu valori la limită ar trebui adăugat cu succes.");
    }

    @ParameterizedTest(name = "BVA Invalid -> id={0}, nume='{1}', pret={2}")
    @Tag("Negative")
    @CsvSource({
            "0,  'latte',                12.0, TEA,         DAIRY,  'Invalid id'",    // Sub limita ID (0)
            "81, 'ceai',                 0.0,  SMOOTHIE,    DAIRY,  'Invalid price'"  // Sub limita preț (0.0)
    })
    @DisplayName("TC10-TC12_BVA: Respingere produse la limitele INVALIDE")
    void testAddProduct_BVA_Invalid(int id, String nume, double pret, CategorieBautura cat, TipBautura tip, String expectedError) {
        // Arrange
        Product invalidBoundaryProduct = new Product(id, nume, pret, cat, tip);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.addProduct(invalidBoundaryProduct);
        }, "Ar fi trebuit să pice validarea BVA la limită.");

        // Verificăm dacă mesajul de eroare aruncat corespunde cu ce așteptăm
        assertTrue(exception.getMessage().contains(expectedError),
                "Mesajul de eroare nu corespunde. Așteptat să conțină: " + expectedError);
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("WB-TC1: Acoperire ramura 'categorie == null'")
    void testFilterByCategorie_NullBranch() {
        // Arrange
        Product p1 = new Product(10, "Smoothie de capsuni", 15, CategorieBautura.SMOOTHIE, TipBautura.PLANT_BASED);
        Product p2 = new Product(11, "Ceai de ghimbir", 2.0, CategorieBautura.TEA, TipBautura.WATER_BASED);
        productService.addProduct(p1);
        productService.addProduct(p2);

        // Act
        List<Product> result = productService.filterByCategorie(null);

        // Assert
        assertTrue(result.isEmpty(), "Ramura null ar trebui sa returneze o lista goala.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("WB-TC2: Acoperire ramura 'ALL' (Path Coverage)")
    void testFilterByCategorie_AllBranch() {
        // Arrange: Adaugam cateva produse în fișier
        productService.addProduct(new Product(1, "Suc de portocale", 10.0, CategorieBautura.JUICE, TipBautura.PLANT_BASED));
        productService.addProduct(new Product(2, "Ceai de menta", 15.0, CategorieBautura.TEA, TipBautura.WATER_BASED));

        // Act
        List<Product> result = productService.filterByCategorie(CategorieBautura.ALL);

        // Assert
        assertEquals(2, result.size(), "Ramura ALL ar trebui sa returneze toate produsele.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("WB-TC3: Lista este fara produse, filtrare dupa o categorie specifica (JUICE)")
    void testFilterByCategorie_FilteringBranch() {
        // Act: Filtrare dupa JUICE
        List<Product> result = productService.filterByCategorie(CategorieBautura.JUICE);

        // Assert
        assertEquals(0, result.size());
    }
    @Test
    @Tag("WhiteBox")
    @DisplayName("WB-TC4: Lista cu produse, niciunul nu se potriveste")
    void testFilterByCategorie_NoMatches() {
        // 1. Arrange: Adaugam produse dintr-o categorie diferita
        productService.addProduct(new Product(10, "Smoothie de capsuni", 2.5,
                CategorieBautura.SMOOTHIE, TipBautura.PLANT_BASED));
        productService.addProduct(new Product(11, "Ceai de ghimbir", 2.0,
                CategorieBautura.TEA, TipBautura.WATER_BASED));

        // 2. Act: Cautam ceva ce nu exista
        List<Product> result = productService.filterByCategorie(CategorieBautura.JUICE);

        // 3. Assert
        assertTrue(result.isEmpty(), "Lista ar trebui sa fie goala, nu exista nicio potrivire.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("WB-TC5: Toate produsele din lista se potrivesc")
    void testFilterByCategorie_AllMatch() {
        // 1. Arrange: Adaugam doar cafele
        productService.addProduct(new Product(20, "Suc de mere", 15.0, CategorieBautura.JUICE, TipBautura.PLANT_BASED));
        productService.addProduct(new Product(21, "Suc de portocale", 12.0, CategorieBautura.JUICE, TipBautura.PLANT_BASED));
        productService.addProduct(new Product(22, "Suc de visine", 10.0, CategorieBautura.JUICE, TipBautura.PLANT_BASED));
        productService.addProduct(new Product(23, "Suc de pere", 15.0, CategorieBautura.JUICE, TipBautura.PLANT_BASED));
        productService.addProduct(new Product(24, "Suc de struguri", 12.0, CategorieBautura.JUICE, TipBautura.PLANT_BASED));
        // 2. Act
        List<Product> result = productService.filterByCategorie(CategorieBautura.JUICE);

        // 3. Assert
        assertEquals(5, result.size(), "Ar fi trebuit sa gaseasca toate cele 5 produse.");
    }

    @Test
    @Tag("WhiteBox")
    @DisplayName("WB-TC6: Mix de produse (Match & No-Match)")
    void testFilterByCategorie_MixedResults() {
        // 1. Arrange: Un mix de categorii
        Product p1 = new Product(30, "Latte", 15.0, CategorieBautura.MILK_COFFEE, TipBautura.DAIRY);
        Product p2 = new Product(31, "Cola", 7.0, CategorieBautura.JUICE, TipBautura.BASIC);

        productService.addProduct(p1);
        productService.addProduct(p2);

        // 2. Act: Filtram dupa MILK_COFFEE, doar Latte ar trebui sa se potriveasca
        List<Product> result = productService.filterByCategorie(CategorieBautura.JUICE);

        // 3. Assert
        assertEquals(1, result.size(), "Ar fi trebuit sa gaseasca doar un produs.");
        assertNotEquals("Latte", result.get(0).getNume());
        assertEquals("Cola", result.get(0).getNume());
    }
}