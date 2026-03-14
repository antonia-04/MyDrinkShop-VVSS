package drinkshop.receipt;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;

import java.util.List;

public class ReceiptGenerator {
    public static String generate(Order o, List<Product> products) {
        if (o == null || products == null || o.getItems() == null) {
            throw new IllegalArgumentException("Invalid input for receipt generation");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("===== BON FISCAL =====\n")
                .append("Comanda #").append(o.getId()).append("\n");

        for (OrderItem i : o.getItems()) {
            Product p = products.stream()
                    .filter(p1 -> i.getProduct().getId() == p1.getId())
                    .findFirst()
                    .orElse(null);

            String nume = (p != null && p.getNume() != null) ? p.getNume() : "Unknown";
            double pret = (p != null) ? p.getPret() : 0.0;

            sb.append(nume).append(": ")
                    .append(pret).append(" x ")
                    .append(i.getQuantity()).append(" = ")
                    .append(i.getTotal()).append(" RON\n");
        }

        sb.append("---------------------\nTOTAL: ")
                .append(o.getTotalPrice()).append(" RON\n")
                .append("=====================\n");

        return sb.toString();
    }
}