package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private final UserRepository users;
    private final PatchMethod patchMethod;
    private final PasswordEncoder encoder;

    //Instancias
    @Autowired
    public UserService(UserRepository people, PatchMethod patchMethod, PasswordEncoder encoder) {
        this.users = people;
        this.encoder = encoder;
        this.patchMethod = patchMethod;
    }

    //devuelve el usuario con el email correspondiente
    public Optional<User> get(String email) {
        Optional<User> user = users.findById(email);
        if (user.isPresent()) {
            //borramos la contraseña para que no se muestre
            user.get().setPassword(null);
            //devolvemos el usuario encontrado
            return users.findById(email);
        }
        return Optional.empty();
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
            u.setEmail(null).setPassword(null);
        }

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    //inserta el usuario
    public Optional<User> insert(User user) {
        //codificamos la contraseña
        user.setPassword(encoder.encode(user.getPassword()));
        //insertamos el usuario
        user = users.insert(user);
        //borramos la contraseña para que no se muestre
        user.setPassword(null);
        //devolvemos el usuario
        return Optional.of(user);
    }

    //modifica el usuario
    public Optional<User> patch(String id, List<Map<String, Object>> updates) throws JsonPatchException {
        //si el usuario se encuentra presente en la base de datos
        if (this.get(id).isPresent()) {
            //obtenemos el usuario de la base de datos
            User user = this.get(id).get();
            //actualizamos los datos con el patch
            user = patchMethod.patch(user, updates);
            //si se modifica la contraseña, se encripta, en caso contrario no
            if (updates.get(0).containsValue("replace") && updates.get(0).containsValue("/password")) {
                //codificamos la contraseña
                user.setPassword(encoder.encode(user.getPassword()));
            }
            //actualizamos en la base de datos
            user = users.save(user);
            //borramos la contraseña para que no se muestre
            user.setPassword(null);
            //actualizamos en la base de datos y retornamos el usuario
            return Optional.of(user);
        }
        //devolvemos el objeto vacío
        return Optional.empty();
    }

    //elimina el usuario con el email correspondiente
    public void delete(String email) {
        users.deleteById(email);
    }
}