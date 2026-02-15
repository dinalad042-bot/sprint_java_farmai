package tn.esprit.farmai.interfaces;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic CRUD interface for standardizing basic operations.
 * @param <T> The entity type
 */
public interface CRUD<T> {

    /**
     * Insert a new entity into the database.
     * @param t The entity to insert
     * @throws SQLException if a database error occurs
     */
    void insertOne(T t) throws SQLException;

    /**
     * Update an existing entity in the database.
     * @param t The entity to update
     * @throws SQLException if a database error occurs
     */
    void updateOne(T t) throws SQLException;

    /**
     * Delete an entity from the database by its ID.
     * @param id The ID of the entity to delete
     * @throws SQLException if a database error occurs
     */
    void deleteOne(int id) throws SQLException;

    /**
     * Retrieve all entities from the database.
     * @return A list of all entities
     * @throws SQLException if a database error occurs
     */
    List<T> selectAll() throws SQLException;
}
