package org.bahmni.module.patientidentifiergenerator.advice;

import org.bahmni.module.patientidentifiergenerator.IdentifierEnhancementFactory;
import org.openmrs.Patient;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

public class BeforeSaveAdvice implements MethodBeforeAdvice {

    private static final String methodToIntercept = "savePatient";

    public void before(Method method, Object[] objects, Object o) {
        if (method.getName().equalsIgnoreCase(methodToIntercept)) {
            Patient patient = (Patient) objects[0];
            if (patient.getPatientId() == null) {
                new IdentifierEnhancementFactory().enhanceIdentifier(patient);
            }
        }
    }
}
