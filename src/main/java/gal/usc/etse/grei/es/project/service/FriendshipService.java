package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Frienship;
import gal.usc.etse.grei.es.project.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
    public Optional<Frienship> get(String id) {
        return friendships.findById(id);
    }

    //devuelve todas las valoraciones
    public List<Frienship> getAll() {
        return friendships.findAll();
    }

    //devuelve la lista de amigos
    public List<String> getFriends(String user) {
        Criteria criteria = Criteria.where("_id").exists(true);
        //indicamos que el usuario o friend debe ser user
        criteria.and("user").is(user);
        Query query = Query.query(criteria);
        //añadimos primero en los que se encuentre en user
        List<Frienship> result = mongo.find(query, Frienship.class);
        criteria.and("friend").is(user);
        query = Query.query(criteria);
        //añadimos a continuación en los que se encuentre como friend
        result.addAll(mongo.find(query, Frienship.class));
        List<String> friends = new ArrayList<>();
        //añadimos todos los amigos obtenidos
        for (Frienship f : result) {
            friends.add(f.getFriend());
        }

        return friends;
    }

    //modifica la amistad
    public Optional<Frienship> put(String id) {
        //obtenemos la fecha actual
        LocalDate currentDate = LocalDate.now();
        Date since = new Date(currentDate.getDayOfMonth(), currentDate.getMonthValue(), currentDate.getYear());
        //si la amistad se encuentra presente en la base de datos
        if (this.get(id).isPresent()) {
            //obtenemos la amistad de la base de datos
            Frienship frienship = this.get(id).get();
            //indicamos amistad aceptada y fecha actual
            frienship.setConfirmed(true).setSince(since);
            //actualizamos la amistad
            return Optional.of(friendships.save(frienship));
        }
        //devolvemos el objeto vacío
        return Optional.empty();
    }

    //inserta la amistad entre usuarios
    public Optional<Frienship> insert(String user, String friend) {
        //actualizamos los campos de usuario que crea la amistad y su amigo
        Frienship frienship = new Frienship().setUser(user).setFriend(friend);
        //devolvemos el usuario
        return Optional.of(friendships.insert(frienship));
    }

    //elimina la valoración con el id correspondiente
    public void delete(String id) {
        friendships.deleteById(id);
    }

    //comprueba si dos usuarios son amigos
    public Boolean areFriends(String user, String friend) {
        //si está contenido en la lista, entonces son amigos
        return this.getFriends(user).contains(friend);
    }
}