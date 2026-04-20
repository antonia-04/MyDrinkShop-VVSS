package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.repository.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductService {

    private final Repository<Integer, Product> productRepo;

    public ProductService(Repository<Integer, Product> productRepo) {
        this.productRepo = productRepo;
    }

    public void addProduct(Product p) {
        if(p.getId()<=0){
            throw new IllegalArgumentException("Invalid id");
        }
        if (p.getNume() == null || p.getNume().trim().isEmpty()) {
            throw new IllegalArgumentException("Empty name");
        }
        productRepo.save(p);
    }

    public void updateProduct(int id, String name, double price, CategorieBautura categorie, TipBautura tip) {
        Product updated = new Product(id, name, price, categorie, tip);
        productRepo.update(updated);
    }

    public void deleteProduct(int id) {
        productRepo.delete(id);
    }

    public List<Product> getAllProducts() {
//        Iterable<Product> it=productRepo.findAll();
//        ArrayList<Product> products=new ArrayList<>();
//        it.forEach(products::add);
//        return products;

//        return StreamSupport.stream(productRepo.findAll().spliterator(), false)
//                    .collect(Collectors.toList());
        return productRepo.findAll();
    }

    public java.util.Optional<Product> findById(int id) {
        return productRepo.findOne(id);
    }
    public List<Product> filterByCategorie(CategorieBautura categorie) {
//        if (categorie == CategorieBautura.ALL) return getAllProducts();
//        return getAllProducts().stream()
//                .filter(p -> p.getCategorie() == categorie)
//                .collect(Collectors.toList());
        List<Product> matchingProducts = new ArrayList<>();
        if (categorie == null) {
        }
        else if (categorie == CategorieBautura.ALL) {
            matchingProducts = getAllProducts();
        } else { var allProducts = getAllProducts();
            var product = new Product(0, null, 0.0, null, null);
            while (!allProducts.isEmpty()){
                product = allProducts.removeFirst();
                if (product.getCategorie() == categorie)
                    matchingProducts.add(product);}
        }
        return matchingProducts;

    }



    public List<Product> filterByTip(TipBautura tip) {
        if (tip == TipBautura.ALL) return getAllProducts();
        return getAllProducts().stream()
                .filter(p -> p.getTip() == tip)
                .collect(Collectors.toList());
    }
}