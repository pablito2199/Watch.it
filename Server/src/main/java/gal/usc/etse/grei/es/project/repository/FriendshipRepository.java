package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FriendshipRepository extends MongoRepository<Friendship, String> {
    Optional<Page<Friendship>> getAllByUserOrFriend(String user, String friend, Pageable request);

    void deleteAllByUserOrFriend(String userMail, String userMail1);
}
