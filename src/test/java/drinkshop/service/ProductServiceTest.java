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
import java.util.Optional;

import org.junit.jupiter.api.*;

@DisplayName("Teste ECP pentru ProductService (File Repository)")
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
}