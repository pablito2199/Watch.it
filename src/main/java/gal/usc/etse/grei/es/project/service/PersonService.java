package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Person;
import gal.usc.etse.grei.es.project.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersonService {
    private final PersonRepository people;

    //Instancias
    @Autowired
    public PersonService(PersonRepository people) {
        this.people = people;
    }

    public List<String> get() {
        List<String> listPeople = new ArrayList<>();
        for (Person p : people.findAll()) {
            listPeople.add(p.getId());
        }

        return listPeople;
    }
}