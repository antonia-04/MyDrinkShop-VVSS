package drinkshop.repository.file;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;
import drinkshop.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class FileOrderRepository extends FileAbstractRepository<Integer, Order> {

    private Repository<Integer, Product> productRepository;

    public FileOrderRepository(String fileName, Repository<Integer, Product> productRepository) {
        super(fileName);
        if (productRepository == null) {
            throw new IllegalArgumentException("Product repository must not be null");
        }
        this.productRepository = productRepository;
        loadFromFile();
    }

    @Override
    protected Integer getId(Order entity) {
        return entity.getId();
    }

    @Override
    protected Order extractEntity(String line) {
        String[] parts = line.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Corrupted file line, missing parts: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        List<OrderItem> items = new ArrayList<>();

        if (!parts[1].isEmpty()) {
            String[] products = parts[1].split("\\|");
            for (String product : products) {
                String[] prodParts = product.split(":");
                if (prodParts.length < 2) {
                    throw new IllegalArgumentException("Corrupted product data: " + product);
                }

                int productId = Integer.parseInt(prodParts[0]);
                int quantity = Integer.parseInt(prodParts[1]);

                Product foundProduct = productRepository.findOne(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

                items.add(new OrderItem(foundProduct, quantity));
            }
        }

        double totalPrice = Double.parseDouble(parts[2]);
        return new Order(id, items, totalPrice);
    }

    @Override
    protected String createEntityAsString(Order entity) {
        StringBuilder sb = new StringBuilder();

        if (entity.getItems() != null) {
            for (OrderItem item : entity.getItems()) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(item.getProduct().getId())
                        .append(":")
                        .append(item.getQuantity());
            }
        }

        return entity.getId() + "," +
                sb.toString() + "," +
                entity.getTotalPrice();
    }
}