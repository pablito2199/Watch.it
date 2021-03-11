package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Frienship;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FriendshipRepository extends MongoRepository<Frienship, String> {
}
