package drinkshop.service.validator;

import drinkshop.domain.Product;

public class ProductValidator implements Validator<Product> {

    @Override
    public void validate(Product product) {

        // Verificăm ID-ul
        if (product.getId() <= 0) {
            throw new IllegalArgumentException("Invalid id");
        }

        // Verificăm Numele (gol)
        if (product.getNume() == null || product.getNume().trim().isEmpty()) {
            throw new IllegalArgumentException("Empty name");
        }

        // Verificăm Prețul
        if (product.getPret() <= 0) {
            throw new IllegalArgumentException("Invalid price");
        }
    }
}