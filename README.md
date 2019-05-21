# Patient Identifier Generator

This repository helps in generating patient identifier dynamically

Currently through openmrs we can set static prefix and suffix. With this module we are allowing the user to have dynamic prefix and suffix

<b>Prefix:</b> To configure dynamic prefix we need a have a global property `bahmni.patientidentifier.prefixConceptName` set with a personAttribute and answers of that person attribute(i.e Concept) should have abbreviation in concept_reference_map table
<br>
<b>Suffix:</b> Suffix would be always gender

If we configure static prefix and also set the global property say nationality the we get nationality+static prefix+number+gender 

#Sample sql to  add abbreviation to concepts 

<pre>
SELECT concept_source_id FROM concept_reference_source WHERE name = "Abbreviation";
SELECT concept_map_type_id INTO @concept_map_type_id FROM concept_map_type WHERE name='SAME-AS';

SELECT uuid() INTO @uuid;
SELECT now() INTO @time_now;
INSERT INTO concept_reference_term (name, code, creator, date_created,concept_source_id, retired, uuid)
                            VALUES ('EG', 'EG', 1, @time_now, @concept_source_id, 0, @uuid);

SELECT concept_reference_term_id INTO @concept_reference_term_id FROM concept_reference_term WHERE name='EG';

SELECT concept_id INTO @concept_id FROM concept_name WHERE name = 'Egyptian' AND concept_name_type = 'FULLY_SPECIFIED';
SELECT uuid() INTO @uuid;
INSERT INTO concept_reference_map (concept_reference_term_id, concept_map_type_id, concept_id, date_created, creator, uuid)
                            VALUES(@concept_reference_term_id , @concept_map_type_id, @concept_id, @time_now,1, @uuid);
</pre>
