package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Person;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonRepository extends MongoRepository<Person, String> {
}
