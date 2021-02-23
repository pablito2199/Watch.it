package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, String> {
    //recuperamos todos los campos de los usuarios salvo email y amigos
    @Query(value = "{}", fields = "{email: 0, friends: 0}")
    Page<User> findAllUsers(Pageable request);
}
