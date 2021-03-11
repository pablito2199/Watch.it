package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private final UserRepository users;
    private final PatchMethod patchMethod;
    //private final PasswordEncoder encoder;

    //Instancias
    @Autowired
    public UserService(UserRepository people, PatchMethod patchMethod/*, PasswordEncoder encoder*/) {
        this.users = people;
        //this.encoder = encoder;
        this.patchMethod = patchMethod;
    }

    //devuelve el usuario con el email correspondiente
    public Optional<User> get(String email) {
        //devolvemos el usuario encontrado
        return users.findById(email);
    }

    //devuelve el usuario con el email correspondiente
    public Optional<User> getAllUserData(String email) {
        //devolvemos el usuario
        return users.findById(email);
    }

    //devuelve la lista de usuarios paginados
    public Optional<Page<User>> get(int page, int size, Sort sort, String email, String name) {
        Pageable request = PageRequest.of(page, size, sort);
        //el filtro deberá contener lo especificado debido al "CONTAINS"
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        //filtrado si se pasa un email o nombre
        Example<User> filter = Example.of(new User().setEmail(email).setName(name), matcher);
        //buscamos los usuarios según los filtros
        Page<User> result = users.findAll(filter, request);
        //debido a que el campo de email no se verá, lo ponemos a null
        for (User u : result) {
            u.setEmail(null);
        }

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    //inserta el usuario
    public Optional<User> insert(User user) {
        //devolvemos el usuario
        return Optional.of(users.insert(user));
    }

    //modifica la película
    public Optional<User> patch(String id, List<Map<String, Object>> updates) throws JsonPatchException {
        //si la película se encuentra presente en la base de datos
        if (this.get(id).isPresent()) {
            //obtenemos la película de la base de datos
            User user = this.get(id).get();
            //actualizamos los datos con el patch
            user = patchMethod.patch(user, updates);
            //actualizamos en la base de datos
            return Optional.of(users.save(user));
        }
        //devolvemos el objeto vacío
        return Optional.empty();
    }

    //elimina el usuario con el email correspondiente
    public void delete(String email) {
        users.deleteById(email);
    }

    public Boolean areFriends(String ... users) {
        return Arrays.stream(users).allMatch(it -> it.contains("@test.com"));
    }
}
