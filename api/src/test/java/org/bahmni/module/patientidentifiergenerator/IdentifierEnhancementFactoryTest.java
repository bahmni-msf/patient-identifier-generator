package org.bahmni.module.patientidentifiergenerator;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class IdentifierEnhancementFactoryTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private ConceptService conceptService;

    private IdentifierEnhancementFactory identifierEnhancementFactory;

    public static final String PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME = "bahmnimsf.patientidentifier.prefixConceptName";

    @Before
    public void setUp() {
        identifierEnhancementFactory = new IdentifierEnhancementFactory(administrationService);
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }

    @Test
    public void shouldAddGlobalPropertyPrefixToPatientIdentifierAndGenderAsSuffix() {
        Patient patient = setUpPatientData();
        Concept concept = setUpConceptData();
        setupConceptSource("Abbreviation", concept);
        when(conceptService.getConcept("100")).thenReturn(concept);
        when(administrationService.getGlobalPropertyValue(PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME, ""))
                .thenReturn("personAttribute");

        identifierEnhancementFactory.enhanceIdentifier(patient);

        verify(administrationService).getGlobalPropertyValue(PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME, "");
        assertEquals("PA100002M", patient.getPatientIdentifier().getIdentifier());
    }

    @Test
    public void shouldReturnOnlyPatientIdentifierAndGenderWhenThereIsNoPatientIdentifierPrefixGlobalProperty() {
        Patient patient = setUpPatientData();
        Concept concept = setUpConceptData();
        setupConceptSource("Abbreviation", concept);
        when(conceptService.getConcept("100")).thenReturn(concept);
        when(administrationService.getGlobalPropertyValue(PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME, ""))
                .thenReturn("");

        identifierEnhancementFactory.enhanceIdentifier(patient);

        verify(administrationService).getGlobalPropertyValue(PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME, "");
        assertEquals("100002M", patient.getPatientIdentifier().getIdentifier());
    }

    @Test
    public void shouldReturnOnlyPatientIdentifierAndGenderWhenThatPersonAttributeIsNotPresent() {
        Patient patient = setUpPatientData();
        patient.setGender("F");
        patient.setAttributes(null);
        Concept concept = setUpConceptData();
        setupConceptSource("Abbreviation", concept);
        when(conceptService.getConcept("100")).thenReturn(concept);
        when(administrationService.getGlobalPropertyValue(PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME, ""))
                .thenReturn("randomAttribute");

        identifierEnhancementFactory.enhanceIdentifier(patient);

        verify(administrationService).getGlobalPropertyValue(PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME, "");
        assertEquals("100002F", patient.getPatientIdentifier().getIdentifier());
    }

    @Test
    public void shouldReturnOnlyPatientIdentifierAndGenderWhenThereIsNoConceptMappingsToPatientIdentifierPrefix() {
        Patient patient = setUpPatientData();
        Concept concept = setUpConceptData();
        setupConceptSource("Abbreviation", concept);
        concept.setConceptMappings(null);
        when(conceptService.getConcept("100")).thenReturn(concept);
        when(administrationService.getGlobalPropertyValue(PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME, ""))
                .thenReturn("personAttribute");

        identifierEnhancementFactory.enhanceIdentifier(patient);

        assertEquals("100002M", patient.getPatientIdentifier().getIdentifier());
    }

    @Test
    public void shouldReturnOnlyPatientIdentifierAndGenderWhenThereIsNoConceptReferenceTerm() {
        Patient patient = setUpPatientData();
        Concept concept = setUpConceptData();
        setupConceptSource("Abbreviation", concept);
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();
        conceptMappings.stream().forEach(conceptMap -> conceptMap.setConceptReferenceTerm(null));
        when(conceptService.getConcept("100")).thenReturn(concept);
        when(administrationService.getGlobalPropertyValue(PATIENT_IDENTIFIER_PREFIX_CONCEPT_NAME, ""))
                .thenReturn("personAttribute");

        identifierEnhancementFactory.enhanceIdentifier(patient);

        assertEquals("100002M", patient.getPatientIdentifier().getIdentifier());
    }

    private Concept setUpConceptData() {
        Concept concept = new Concept();
        concept.setId(100);
        ConceptName conceptName = new ConceptName();
        conceptName.setName("PersonAttributeValue");
        conceptName.setLocale(new Locale("en", "GB"));
        concept.setNames(Arrays.asList(conceptName));
        return concept;
    }

    private void setupConceptSource(String conceptSourceDictionary, Concept concept) {
        ConceptSource source = new ConceptSource();
        source.setName(conceptSourceDictionary);
        ConceptMap conceptMap = new ConceptMap(new ConceptReferenceTerm(source, "PA", "PA"), new ConceptMapType());
        concept.setConceptMappings(Arrays.asList(conceptMap));
    }

    private Patient setUpPatientData() {
        Patient patient = new Patient();
        patient.setGender("M");
        PatientIdentifier patientIdentifier =
                new PatientIdentifier("100002", new PatientIdentifierType(), new Location());
        HashSet<PatientIdentifier> patientIdentifiers = new HashSet<>();
        patientIdentifiers.add(patientIdentifier);
        patient.setIdentifiers(patientIdentifiers);
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("personAttribute");
        PersonAttribute personAttribute = new PersonAttribute(personAttributeType, "100");
        HashSet<PersonAttribute> personAttributes = new HashSet<>();
        personAttributes.add(personAttribute);
        patient.setAttributes(personAttributes);
        return patient;
    }
}
