package drinkshop.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

public abstract class AbstractRepository<ID, E>
        implements Repository<ID, E> {

    protected Map<ID, E> entities = new HashMap<>();

    @Override
    public Optional<E> findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public List<E> findAll() {
        return (List<E>)StreamSupport.stream(entities.values().spliterator(), false).toList();
//                    .collect(Collectors.toList());
        // return (List<E>) entities.values();
    }

    @Override
    public E save(E entity) {
        entities.put(getId(entity), entity);
        return entity;
    }

    @Override
    public E delete(ID id) {
        return entities.remove(id);
    }

    @Override
    public E update(E entity) {
        entities.put(getId(entity), entity);
        return entity;
    }

    protected abstract ID getId(E entity);
}
