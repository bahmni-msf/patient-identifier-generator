package org.bahmni.module.patientidentifiergenerator;

import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.AdministrationServiceImpl;

import java.util.Collection;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class IdentifierEnhancementFactory {

    private AdministrationService administrationService;

    public IdentifierEnhancementFactory() {
        this.administrationService = new AdministrationServiceImpl();
    }

    public IdentifierEnhancementFactory(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    public static final String ABBREVIATION_CONCEPT_SOURCE = "Abbreviation";
    public static final String PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME = "bahmni.patientidentifier.prefixConceptName";


    public void enhanceIdentifier(Patient patient) {
        PatientIdentifier identifier = patient.getPatientIdentifier();
        StringBuilder enhancedId = new StringBuilder();
        enhancedId.append(getPrefix(patient)).append(identifier.getIdentifier()).append(getGender(patient));
        identifier.setIdentifier(enhancedId.toString());
    }

    private String getGender(Patient patient) {
        return patient.getGender();
    }

    private String getPrefix(Patient patient) {
        String personAttributeNameForPrefix = administrationService
                .getGlobalPropertyValue(PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME, "");
        if (isEmpty(personAttributeNameForPrefix)) return "";
        return getAbbreviationValueOfPersonAttribute(patient, personAttributeNameForPrefix);
    }

    private String getAbbreviationValueOfPersonAttribute(Patient patient, String personAttributeName) {
        PersonAttribute personAttribute = patient.getAttribute(personAttributeName);
        if (isNull(personAttribute)) {
            return "";
        }
        Concept concept = Context.getConceptService().getConcept(personAttribute.getValue());
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();
        return getValueFromConceptMappings(conceptMappings);
    }

    private String getValueFromConceptMappings(Collection<ConceptMap> conceptMappings) {
        for (ConceptMap conceptMapping : conceptMappings) {
            ConceptReferenceTerm conceptReferenceTerm = conceptMapping.getConceptReferenceTerm();
            if (!isNull(conceptReferenceTerm.getConceptSource())
                    && ABBREVIATION_CONCEPT_SOURCE.equals(conceptReferenceTerm.getConceptSource().getName())) {
                return conceptReferenceTerm.getName();
            }
        }
        return "";
    }
}
