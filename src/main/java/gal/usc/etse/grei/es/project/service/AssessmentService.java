package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AssessmentService {
    private final AssessmentRepository assessments;

    //Instancia
    @Autowired
    public AssessmentService(AssessmentRepository assessments) {
        this.assessments = assessments;
    }

    //inserta la valoración
    public Optional<Assessment> insert(Assessment assessment) {
        return Optional.of(assessments.insert(assessment));
    }
}
