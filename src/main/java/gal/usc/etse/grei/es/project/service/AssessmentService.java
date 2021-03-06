package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssessmentService {
    private final AssessmentRepository assessments;
    private final FilmService films;
    private final UserService users;
    private final MongoTemplate mongo;

    //Instancias
    @Autowired
    public AssessmentService(AssessmentRepository assessments, FilmService films, UserService users, MongoTemplate mongo) {
        this.assessments = assessments;
        this.films = films;
        this.users = users;
        this.mongo = mongo;
    }

    //devuelve la valoración con el id correspondiente
    public Optional<Assessment> get(String id) {
        return assessments.findById(id);
    }

    //devuelve las valoraciones de la película correspondiente
    public Optional<List<Assessment>> getAssessments(String film, String user) {
        Criteria criteria = Criteria.where("_id").exists(true);
        criteria.and("film._id").is(film);
        criteria.and("user._id").is(user);
        Query query = Query.query(criteria);
        List<Assessment> result = mongo.find(query, Assessment.class);
        if (result.isEmpty())
            return Optional.empty();
        else
            return Optional.of(result);
    }

    //devuelve las valoraciones de la película correspondiente
    public Optional<List<Assessment>> getAssessmentsFilm(String film) {
        Criteria criteria = Criteria.where("_id").exists(true);
        criteria.and("film._id").is(film);
        Query query = Query.query(criteria);
        List<Assessment> result = mongo.find(query, Assessment.class);
        if (result.isEmpty())
            return Optional.empty();
        else
            return Optional.of(result);
    }

    //devuelve las valoraciones del usuario correspondiente
    public Optional<List<Assessment>> getAssessmentsUser(String user) {
        Criteria criteria = Criteria.where("_id").exists(true);
        criteria.and("user._id").is(user);
        Query query = Query.query(criteria);
        List<Assessment> result = mongo.find(query, Assessment.class);
        if (result.isEmpty())
            return Optional.empty();
        else
            return Optional.of(result);
    }

    //inserta la valoración
    public Optional<Assessment> insert(Assessment assessment) {
        //indicamos el título de la película
        assessment.getFilm().setTitle(films.get((assessment.getFilm().getId())).get().getTitle());
        //indicamos el nombre del usuario
        assessment.getUser().setName(users.get((assessment.getUser().getEmail())).get().getName());
        return Optional.of(assessments.insert(assessment));
    }

    //modifica la valoración
    public Optional<Assessment> put(String id, Assessment assessment) {
        //indicamos el id a modificar
        assessment.setId(id);
        //indicamos el título de la película
        assessment.getFilm().setTitle(films.get((assessment.getFilm().getId())).get().getTitle());
        //indicamos el nombre del usuario
        assessment.getUser().setName(users.get((assessment.getUser().getEmail())).get().getName());
        return Optional.of(assessments.save(assessment));
    }

    //elimina la valoración con el id correspondiente
    public void delete(String id) {
        assessments.deleteById(id);
    }
}
