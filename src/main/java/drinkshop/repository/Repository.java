package drinkshop.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<ID, E> {

    Optional<E> findOne(ID id);

    List<E> findAll();

    E save(E entity);

    E delete(ID id);

    E update(E entity);
}