package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService {
    private final FriendshipRepository friendships;
    private final MongoTemplate mongo;

    //Instancias
    @Autowired
    public FriendshipService(FriendshipRepository friendships, MongoTemplate mongo) {
        this.friendships = friendships;
        this.mongo = mongo;
    }

    //devuelve la amistad con el id correspondiente
    public Optional<Friendship> get(String id) {
        return friendships.findById(id);
    }

    //devuelve todas las amistades
    public List<Friendship> getAll() {
        return friendships.findAll();
    }

    //devuelve la lista de amigos
    public List<String> getAllFriends(String user) {
        Criteria criteria = Criteria.where("_id").exists(true);
        //indicamos que el usuario o friend debe ser user
        criteria.and("user").is(user);
        Query query = Query.query(criteria);
        List<Friendship> result = mongo.find(query, Friendship.class);
        criteria.and("friend").is(user);
        query = Query.query(criteria);
        //añadimos a continuación en los que se encuentre como friend
        result.addAll(mongo.find(query, Friendship.class));
        Criteria criteria1 = Criteria.where("_id").exists(true);
        criteria1.and("friend").is(user);
        query = Query.query(criteria1);
        //añadimos a continuación en los que se encuentre como friend
        result.addAll(mongo.find(query, Friendship.class));
        List<String> friends = new ArrayList<>();
        //añadimos todos los amigos obtenidos
        for (Friendship f : result) {
            if (user.equals(f.getUser())) {
                friends.add(f.getFriend());
            } else {
                friends.add(f.getUser());
            }
        }

        return friends;
    }

    //devuelve la lista de amigos
    public Optional<Page<String>> getFriends(int page, int size, String user) {
        Pageable request = PageRequest.of(page, size);
        Criteria criteria = Criteria.where("_id").exists(true);
        //indicamos que el usuario o friend debe ser user
        criteria.and("user").is(user);
        Query query = Query.query(criteria);
        List<Friendship> result = mongo.find(query, Friendship.class);
        Criteria criteria1 = Criteria.where("_id").exists(true);
        criteria1.and("friend").is(user);
        query = Query.query(criteria1);
        //añadimos a continuación en los que se encuentre como friend
        result.addAll(mongo.find(query, Friendship.class));
        List<String> friends = new ArrayList<>();
        //añadimos todos los amigos obtenidos
        for (Friendship f : result) {
            if (f.getConfirmed() != null) {
                if (user.equals(f.getUser())) {
                    friends.add(f.getFriend());
                } else {
                    friends.add(f.getUser());
                }
            }
        }

        if (result.isEmpty())
            return Optional.empty();
        else
            return Optional.of(PageableExecutionUtils.getPage(friends, request,
                    () -> mongo.count(Query.query(criteria), String.class)));
    }

    //modifica la amistad
    public Friendship put(String id) {
        //obtenemos la fecha actual
        LocalDate currentDate = LocalDate.now();
        Date since = new Date(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear());
        //si la amistad existe
        if (this.get(id).isPresent()) {
            //obtenemos la amistad de la base de datos
            Friendship friendship = this.get(id).get();
            //indicamos amistad aceptada y fecha actual
            friendship.setConfirmed(true).setSince(since);
            //actualizamos la amistad
            return friendships.save(friendship);
        }
        return null;

    }

    //inserta la amistad entre usuarios
    public Friendship insert(String user, String friend) {
        //actualizamos los campos de usuario que crea la amistad y su amigo
        Friendship friendship = new Friendship().setUser(user).setFriend(friend);
        //devolvemos el usuario
        return friendships.insert(friendship);
    }

    //elimina la valoración con el id correspondiente
    public void delete(String id) {
        friendships.deleteById(id);
    }

    //comprueba si dos usuarios son amigos
    public Boolean areFriends(String user, String friend) {
        //si está contenido en la lista, entonces son amigos
        return this.getAllFriends(user).contains(friend);
    }
}
